#!/usr/bin/env python3
"""Fetch upstream adblock lists, extract Amazon-scoped cosmetic selectors,
merge with hand-curated manual list, and write output files.

Subcommands:
    update   — fetch, parse, merge, write all outputs
    validate — check output files for correctness (standalone or CI gate)

Stdlib-only. No external dependencies.
"""

from __future__ import annotations

import hashlib
import json
import re
import sys
from dataclasses import dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parent.parent
LISTS_DIR = ROOT / "lists"
GEN_DIR = LISTS_DIR / "generated"
PAYLOAD_DIR = ROOT / "app" / "src" / "main" / "resources" / "payload"

SOURCES_JSON = LISTS_DIR / "sources.json"
LOCK_FILE = LISTS_DIR / "sources.lock.json"
MANUAL_FILE = LISTS_DIR / "manual.txt"
DENYLIST_FILE = LISTS_DIR / "manual_denylist.txt"

UPSTREAM_OUT = GEN_DIR / "upstream.txt"
MERGED_OUT = GEN_DIR / "merged.txt"
METADATA_OUT = GEN_DIR / "metadata.json"

SELECTORS_REMOTE = PAYLOAD_DIR / "selectors-remote.css"
EMBEDDED_CSS = PAYLOAD_DIR / "embedded.css"

_AMAZON_RE = re.compile(
    r"^(?:www\.|smile\.|m\.)?"
    r"amazon"
    r"(?:\.\*|\.[a-z]{2,3}(?:\.[a-z]{2})?)$"
)

# Procedural pseudo-classes that are NOT valid CSS — reject selectors containing these
_PROCEDURAL = (
    ":has-text(",
    ":matches-css(",
    ":xpath(",
    ":upward(",
    ":remove(",
    ":matches-path(",
    ":matches-media(",
    ":min-text-length(",
    ":watch-attr(",
    ":others(",
)

FETCH_TIMEOUT = 30


# ---------------------------------------------------------------------------
# Data types
# ---------------------------------------------------------------------------

@dataclass
class SourceStats:
    name: str
    url: str
    checksum: str = ""
    selectors_extracted: int = 0
    exceptions_extracted: int = 0
    dropped_negated: int = 0
    dropped_procedural: int = 0
    dropped_non_amazon: int = 0
    dropped_no_domain: int = 0


@dataclass
class ParseResult:
    positive: set[str] = field(default_factory=set)
    negative: set[str] = field(default_factory=set)
    stats: list[SourceStats] = field(default_factory=list)


# ---------------------------------------------------------------------------
# Parsing helpers
# ---------------------------------------------------------------------------

def is_amazon_domain(domain: str) -> bool:
    return bool(_AMAZON_RE.match(domain))


def has_procedural(selector: str) -> bool:
    lower = selector.lower()
    return any(p in lower for p in _PROCEDURAL)


def split_toplevel_commas(selector: str) -> list[str]:
    """Split a CSS selector on top-level commas (not inside brackets/parens)."""
    parts: list[str] = []
    depth = 0
    current: list[str] = []
    for ch in selector:
        if ch in ("(", "["):
            depth += 1
            current.append(ch)
        elif ch in (")", "]"):
            depth = max(0, depth - 1)
            current.append(ch)
        elif ch == "," and depth == 0:
            parts.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
    tail = "".join(current).strip()
    if tail:
        parts.append(tail)
    return parts


def find_separator(line: str) -> tuple[str, int] | None:
    """Find the first cosmetic filter separator in a line.

    Returns (separator_type, index) or None.
    Checks in order: #@#, #?#, ##+js(, ##^, ##
    """
    idx = line.find("#@#")
    if idx != -1:
        return ("#@#", idx)

    idx = line.find("#?#")
    if idx != -1:
        return ("#?#", idx)

    idx = line.find("##+js(")
    if idx != -1:
        return ("##+js(", idx)

    idx = line.find("##^")
    if idx != -1:
        return ("##^", idx)

    idx = line.find("##")
    if idx != -1:
        return ("##", idx)

    return None


def parse_domains(domain_str: str) -> list[tuple[str, bool]]:
    if not domain_str.strip():
        return []
    result = []
    for d in domain_str.split(","):
        d = d.strip().lower()
        if not d:
            continue
        negated = d.startswith("~")
        if negated:
            d = d[1:]
        result.append((d, negated))
    return result


def parse_source(raw: str, stats: SourceStats) -> tuple[set[str], set[str]]:
    positive: set[str] = set()
    negative: set[str] = set()

    for line in raw.splitlines():
        line = line.strip()

        if not line or line.startswith("!") or line.startswith("["):
            continue
        if line.startswith("||") or line.startswith("@@"):
            continue

        sep = find_separator(line)
        if sep is None:
            continue

        sep_type, sep_idx = sep

        if sep_type in ("#?#", "##+js(", "##^"):
            continue

        domain_str = line[:sep_idx]
        selector = line[sep_idx + len(sep_type):]

        if not domain_str.strip():
            stats.dropped_no_domain += 1
            continue

        domains = parse_domains(domain_str)

        # Can't represent ~domain negation in a flat selector list
        if any(neg for _, neg in domains):
            stats.dropped_negated += 1
            continue

        positive_domains = [d for d, neg in domains if not neg]
        if not positive_domains:
            continue
        if not all(is_amazon_domain(d) for d in positive_domains):
            stats.dropped_non_amazon += 1
            continue

        if has_procedural(selector):
            stats.dropped_procedural += 1
            continue

        parts = split_toplevel_commas(selector)

        is_exception = sep_type == "#@#"
        target = negative if is_exception else positive

        for part in parts:
            part = part.strip()
            if part:
                target.add(part)

        if is_exception:
            stats.exceptions_extracted += len(parts)
        else:
            stats.selectors_extracted += len(parts)

    return positive, negative


# ---------------------------------------------------------------------------
# I/O helpers
# ---------------------------------------------------------------------------

def fetch_list(url: str) -> str:
    req = Request(url, headers={"User-Agent": "amznkiller/1.0 (selector-updater)"})
    with urlopen(req, timeout=FETCH_TIMEOUT) as resp:
        return resp.read().decode("utf-8", errors="replace")


def load_lines(path: Path) -> set[str]:
    """Read non-blank, non-comment lines. Comment prefix is ! (not # — CSS IDs start with #)."""
    if not path.exists():
        return set()
    lines = set()
    for line in path.read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("!"):
            lines.add(line)
    return lines


def write_sorted(path: Path, selectors: set[str] | frozenset[str]) -> None:
    sorted_sels = sorted(selectors)
    path.write_text("\n".join(sorted_sels) + "\n" if sorted_sels else "")


# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

def validate_output(selectors: set[str]) -> list[str]:
    errors = []
    for s in selectors:
        if "##" in s:
            errors.append(f"contains '##': {s}")
        if "#@#" in s:
            errors.append(f"contains '#@#': {s}")
        if not s.strip():
            errors.append("whitespace-only selector")
    return errors


def validate_file(path: Path) -> list[str]:
    """Validate a selector file on disk."""
    if not path.exists():
        return [f"file not found: {path}"]
    selectors = load_lines(path)
    return validate_output(selectors)


# ---------------------------------------------------------------------------
# Subcommands
# ---------------------------------------------------------------------------

def cmd_update() -> int:
    GEN_DIR.mkdir(parents=True, exist_ok=True)
    PAYLOAD_DIR.mkdir(parents=True, exist_ok=True)

    sources_cfg = json.loads(SOURCES_JSON.read_text())["sources"]
    result = ParseResult()

    for src in sources_cfg:
        if not src.get("enabled", True):
            continue

        name = src["name"]
        url = src["url"]
        stats = SourceStats(name=name, url=url)

        print(f"Fetching {name}...")
        try:
            raw = fetch_list(url)
        except Exception as e:
            print(f"  FAILED: {e}", file=sys.stderr)
            result.stats.append(stats)
            continue

        stats.checksum = f"sha256:{hashlib.sha256(raw.encode()).hexdigest()[:16]}"
        print(f"  Downloaded {len(raw)} bytes ({stats.checksum})")

        pos, neg = parse_source(raw, stats)
        result.positive |= pos
        result.negative |= neg
        result.stats.append(stats)

        print(
            f"  Extracted: {stats.selectors_extracted} selectors, "
            f"{stats.exceptions_extracted} exceptions"
        )
        print(
            f"  Dropped: {stats.dropped_no_domain} no-domain, "
            f"{stats.dropped_non_amazon} non-amazon, "
            f"{stats.dropped_negated} negated, "
            f"{stats.dropped_procedural} procedural"
        )

    upstream = result.positive - result.negative
    manual = load_lines(MANUAL_FILE)
    denylist = load_lines(DENYLIST_FILE)

    upstream_filtered = upstream - denylist
    merged = manual | upstream_filtered

    errors = validate_output(merged)
    if errors:
        print(f"\nValidation FAILED ({len(errors)} errors):", file=sys.stderr)
        for e in errors:
            print(f"  {e}", file=sys.stderr)
        return 1

    write_sorted(UPSTREAM_OUT, upstream_filtered)
    write_sorted(MERGED_OUT, merged)

    # APK fallback uses manual selectors only; remote fetch gets full merged set
    write_sorted(EMBEDDED_CSS, manual)
    write_sorted(SELECTORS_REMOTE, merged)

    now = datetime.now(timezone.utc).isoformat(timespec="seconds")
    lock = {
        "generated": now,
        "sources": {},
        "total_upstream": len(upstream_filtered),
        "total_manual": len(manual),
        "total_denylisted": len(denylist & upstream),
        "total_exceptions_applied": len(result.positive & result.negative),
        "total_merged": len(merged),
    }
    for s in result.stats:
        lock["sources"][s.name] = {
            "url": s.url,
            "checksum": s.checksum,
            "selectors_extracted": s.selectors_extracted,
            "exceptions_extracted": s.exceptions_extracted,
            "dropped_negated": s.dropped_negated,
            "dropped_procedural": s.dropped_procedural,
            "dropped_non_amazon": s.dropped_non_amazon,
            "dropped_no_domain": s.dropped_no_domain,
        }
    LOCK_FILE.write_text(json.dumps(lock, indent=2) + "\n")

    new_from_upstream = upstream_filtered - manual
    metadata = {
        "generated": now,
        "total_merged": len(merged),
        "from_manual": len(manual),
        "from_upstream_only": len(new_from_upstream),
        "overlap": len(manual & upstream_filtered),
        "denylisted": len(denylist & upstream),
        "exceptions_applied": len(result.positive & result.negative),
    }
    METADATA_OUT.write_text(json.dumps(metadata, indent=2) + "\n")

    print(f"\nDone: {len(merged)} merged selectors")
    print(f"  Manual: {len(manual)}")
    print(f"  Upstream-only: {len(new_from_upstream)}")
    print(f"  Overlap: {len(manual & upstream_filtered)}")
    print(f"  Denylisted: {len(denylist & upstream)}")
    print(f"  Exceptions applied: {len(result.positive & result.negative)}")
    print(f"\nWrote:")
    print(f"  {UPSTREAM_OUT}")
    print(f"  {MERGED_OUT}")
    print(f"  {LOCK_FILE}")
    print(f"  {METADATA_OUT}")
    print(f"  {SELECTORS_REMOTE}")
    print(f"  {EMBEDDED_CSS}")

    return 0


def cmd_validate() -> int:
    """Validate all output files for correctness."""
    all_errors: list[str] = []

    for path in (MERGED_OUT, UPSTREAM_OUT, SELECTORS_REMOTE, EMBEDDED_CSS):
        errors = validate_file(path)
        if errors:
            all_errors.extend(f"{path.name}: {e}" for e in errors)

    # Check for duplicates in merged output
    if MERGED_OUT.exists():
        lines = [l.strip() for l in MERGED_OUT.read_text().splitlines() if l.strip()]
        if len(lines) != len(set(lines)):
            dupes = [l for l in lines if lines.count(l) > 1]
            all_errors.append(f"merged.txt: {len(set(dupes))} duplicate selectors")

    # Embedded must be subset of merged
    if EMBEDDED_CSS.exists() and MERGED_OUT.exists():
        embedded = load_lines(EMBEDDED_CSS)
        merged = load_lines(MERGED_OUT)
        extra = embedded - merged
        if extra:
            all_errors.append(
                f"embedded.css has {len(extra)} selectors not in merged.txt"
            )

    if all_errors:
        print(f"Validation FAILED ({len(all_errors)} errors):", file=sys.stderr)
        for e in all_errors:
            print(f"  {e}", file=sys.stderr)
        return 1

    print("Validation passed.")
    return 0


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def main() -> int:
    if len(sys.argv) < 2:
        print("Usage: update_selectors.py <update|validate>", file=sys.stderr)
        return 2

    cmd = sys.argv[1]
    if cmd == "update":
        rc = cmd_update()
        if rc != 0:
            return rc
        return cmd_validate()
    elif cmd == "validate":
        return cmd_validate()
    else:
        print(f"Unknown command: {cmd}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
