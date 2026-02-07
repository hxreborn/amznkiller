import fs from "node:fs";
import path from "node:path";
import process from "node:process";

import puppeteer from "puppeteer-core";

function usage() {
  // eslint-disable-next-line no-console
  console.error(
    "Usage: node validate.mjs [--strict] <selector-file> [selector-file ...]"
  );
}

function findChromeExecutable() {
  const candidates = [
    process.env.CHROME_BIN,
    process.env.PUPPETEER_EXECUTABLE_PATH,
    "/usr/bin/google-chrome",
    "/usr/bin/google-chrome-stable",
    "/usr/bin/chromium",
    "/usr/bin/chromium-browser",
  ].filter(Boolean);

  for (const p of candidates) {
    if (fs.existsSync(p)) return p;
  }
  return null;
}

function loadSelectors(filePath) {
  const raw = fs.readFileSync(filePath, "utf8");
  return raw
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l.length > 0 && !l.startsWith("!"));
}

async function validateSelectorsInBrowser(page, selectors) {
  return await page.evaluate((sels) => {
    const bad = [];

    const style = document.createElement("style");
    document.head.appendChild(style);
    const sheet = style.sheet;

    for (let i = 0; i < sels.length; i++) {
      const sel = sels[i];
      try {
        sheet.insertRule(`${sel}{display:none!important}`, sheet.cssRules.length);
      } catch (e) {
        bad.push({ selector: sel, error: String(e) });
        if (bad.length >= 50) break;
      }
    }

    return {
      total: sels.length,
      badCount: bad.length,
      bad,
    };
  }, selectors);
}

async function main() {
  const args = process.argv.slice(2);
  let strict = false;
  const files = [];

  for (const a of args) {
    if (a === "--strict") strict = true;
    else files.push(a);
  }

  if (files.length === 0) {
    usage();
    process.exit(2);
  }

  const resolved = [];
  for (const f of files) {
    const p = path.resolve(process.cwd(), f);
    if (!fs.existsSync(p)) {
      if (strict) {
        // eslint-disable-next-line no-console
        console.error(`Missing selector file: ${f}`);
        process.exit(2);
      }
      // eslint-disable-next-line no-console
      console.warn(`Skipping missing selector file: ${f}`);
      continue;
    }
    resolved.push({ arg: f, path: p });
  }

  if (resolved.length === 0) {
    // eslint-disable-next-line no-console
    console.error("No selector files to validate.");
    process.exit(2);
  }

  const executablePath = findChromeExecutable();
  if (!executablePath) {
    // eslint-disable-next-line no-console
    console.error(
      "Chrome/Chromium not found. Set CHROME_BIN or PUPPETEER_EXECUTABLE_PATH."
    );
    process.exit(2);
  }

  const browser = await puppeteer.launch({
    executablePath,
    headless: "new",
    args: ["--no-sandbox", "--disable-setuid-sandbox"],
  });

  try {
    const page = await browser.newPage();
    await page.setContent("<!doctype html><html><head></head><body></body></html>");

    let failed = false;

    for (const f of resolved) {
      const selectors = loadSelectors(f.path);
      const result = await validateSelectorsInBrowser(page, selectors);

      // eslint-disable-next-line no-console
      console.log(`${f.arg}: ${result.total} selectors, ${result.badCount} invalid`);

      if (result.badCount > 0) {
        failed = true;
        for (const b of result.bad) {
          // eslint-disable-next-line no-console
          console.error(`  invalid: ${b.selector}`);
          // eslint-disable-next-line no-console
          console.error(`    error: ${b.error}`);
        }
      }
    }

    process.exit(failed ? 1 : 0);
  } finally {
    await browser.close();
  }
}

main().catch((e) => {
  // eslint-disable-next-line no-console
  console.error(e?.stack || String(e));
  process.exit(2);
});

