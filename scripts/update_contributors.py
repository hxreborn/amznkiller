#!/usr/bin/env python3
"""Snapshot GitHub contributors into contributors/ for the About screen.

Writes contributors/contributors.json, contributors/avatars/<login>.webp and
contributors/.avatar-urls.json. The app reads them as assets and never calls GitHub.
"""

from __future__ import annotations

import argparse
import io
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "contributors"
AVATAR_DIR = OUT_DIR / "avatars"
JSON_OUT = OUT_DIR / "contributors.json"
AVATAR_URLS_OUT = OUT_DIR / ".avatar-urls.json"

API = "https://api.github.com"
AVATAR_SIZE = 96
STATS_ATTEMPTS = 5
STATS_BACKOFF_SECONDS = 4
LOGIN_RE = re.compile(r"^[A-Za-z0-9](?:[A-Za-z0-9-]{0,38})$")


def token() -> str | None:
    value = os.environ.get("GITHUB_TOKEN")
    if value:
        return value
    try:
        result = subprocess.run(["gh", "auth", "token"], capture_output=True, text=True, check=True)
    except (OSError, subprocess.CalledProcessError):
        return None
    return result.stdout.strip() or None


def repo_slug() -> str:
    url = subprocess.run(
        ["git", "-C", str(ROOT), "remote", "get-url", "origin"],
        capture_output=True,
        text=True,
        check=True,
    ).stdout.strip()
    match = re.search(r"github\.com[:/]([^/]+/[^/]+?)(?:\.git)?$", url)
    if not match:
        raise SystemExit(f"origin is not a GitHub remote: {url}")
    return match.group(1)


def request(url: str, auth: str | None) -> tuple[int, bytes]:
    headers = {"User-Agent": "amznkiller-contributors", "Accept": "application/vnd.github+json"}
    if auth and url.startswith(API):
        headers["Authorization"] = f"Bearer {auth}"
    try:
        with urlopen(Request(url, headers=headers), timeout=30) as response:
            return response.status, response.read()
    except HTTPError as error:
        return error.code, error.read()


def fetch_stats(slug: str, auth: str | None) -> list[dict]:
    url = f"{API}/repos/{slug}/stats/contributors"
    for attempt in range(1, STATS_ATTEMPTS + 1):
        status, body = request(url, auth)
        if status == 200:
            data = json.loads(body)
            if isinstance(data, list):
                return data
            raise SystemExit(f"unexpected stats payload: {body[:200]!r}")
        if status != 202:
            raise SystemExit(f"stats/contributors answered {status}: {body[:200]!r}")
        print(f"stats not ready (202), attempt {attempt}/{STATS_ATTEMPTS}", file=sys.stderr)
        if attempt < STATS_ATTEMPTS:
            time.sleep(STATS_BACKOFF_SECONDS * attempt)
    raise SystemExit("stats/contributors still computing after retries, snapshot left untouched")


def is_bot(author: dict) -> bool:
    login = author["login"].lower()
    return author.get("type") == "Bot" or login.endswith("[bot]") or login.endswith("-bot")


def collect(stats: list[dict], slug: str) -> list[dict]:
    owner = slug.split("/")[0].lower()
    people = []
    for entry in stats:
        author = entry.get("author")
        if not author or is_bot(author):
            continue
        login = author["login"]
        if not LOGIN_RE.match(login):
            raise SystemExit(f"unexpected login {login!r}")
        people.append(
            {
                "login": login,
                "owner": login.lower() == owner,
                "added": sum(week["a"] for week in entry["weeks"]),
                "removed": sum(week["d"] for week in entry["weeks"]),
                "url": f"https://github.com/{login}",
                "avatar": f"avatars/{login}.webp",
                "_avatar_url": author["avatar_url"],
            }
        )
    people.sort(key=lambda person: (-person["added"], person["login"].lower()))
    return people


def load_avatar_urls() -> dict[str, str]:
    if not AVATAR_URLS_OUT.exists():
        return {}
    return json.loads(AVATAR_URLS_OUT.read_text())


def download_avatar(person: dict, known: dict[str, str], auth: str | None) -> None:
    target = AVATAR_DIR / f"{person['login']}.webp"
    source = person["_avatar_url"]
    if target.exists() and known.get(person["login"]) == source:
        return
    separator = "&" if "?" in source else "?"
    status, body = request(f"{source}{separator}s={AVATAR_SIZE}", auth)
    if status != 200:
        raise SystemExit(f"avatar for {person['login']} answered {status}")
    image = Image.open(io.BytesIO(body)).convert("RGBA")
    image.save(target, "WEBP", lossless=True, quality=100, method=6)


def write_json(path: Path, value: object) -> None:
    path.write_text(json.dumps(value, indent=2, sort_keys=True, ensure_ascii=False) + "\n")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", help="owner/name, default: parsed from the origin remote")
    args = parser.parse_args()

    slug = args.repo or repo_slug()
    auth = token()
    people = collect(fetch_stats(slug, auth), slug)
    if not people:
        raise SystemExit("refusing to write an empty contributor list")

    AVATAR_DIR.mkdir(parents=True, exist_ok=True)
    known = load_avatar_urls()
    for person in people:
        download_avatar(person, known, auth)

    listed = {person["login"] for person in people}
    for stale in AVATAR_DIR.glob("*.webp"):
        if stale.stem not in listed:
            stale.unlink()

    write_json(AVATAR_URLS_OUT, {person["login"]: person["_avatar_url"] for person in people})
    write_json(JSON_OUT, [{key: value for key, value in person.items() if not key.startswith("_")} for person in people])
    print(f"wrote {len(people)} contributors for {slug}")


if __name__ == "__main__":
    try:
        main()
    except URLError as error:
        raise SystemExit(f"network error: {error}")
