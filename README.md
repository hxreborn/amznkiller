# AmznKiller

Xposed module that hides ads and sponsored content in Amazon Shopping via CSS injection.

![Update Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/update-selectors.yml/badge.svg)
![Validate Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/validate-selectors.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/API-28%2B-3DDC84?logo=android&logoColor=white)

## Features

- Hides sponsored cards, video carousels, and promotional components inside Amazon WebViews
- Remote selector lists with embedded fallback. Custom URL support for self-maintained lists
- Selector sanitizer rejects unsafe input (CSS injection primitives, adblock syntax)
- Companion app with dashboard status, manual refresh, and settings
- Works alongside Private DNS, hosts files, and network-level blockers
- Cosmetic only for now. No network-level blocking
- It's FOSS

## Requirements

- Android 9 (API 28) or higher
- [LSPosed](https://github.com/JingMatrix/LSPosed) (JingMatrix fork recommended)
- Amazon Shopping app (`com.amazon.mShop.android.shopping`)

## Installation

1. Download the APK:

   <a href="../../releases"><img src=".github/assets/badge_github.png" height="60" alt="Get it on GitHub" /></a>
   <a href="http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22eu.hxreborn.amznkiller%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fhxreborn%2Famznkiller%22%2C%22author%22%3A%22rafareborn%22%2C%22name%22%3A%22AmznKiller%22%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Afalse%7D%22%7D"><img src=".github/assets/badge_obtainium.png" height="60" alt="Get it on Obtainium" /></a>

2. Enable the module in LSPosed and scope it to **Amazon Shopping**
3. Open the AmznKiller companion app to verify the module is active and optionally fetch updated
   lists (built-in filters work out of the box)
4. Launch Amazon Shopping and browse ad-free

## Screenshots

<details>
<summary>Before vs after (placeholders)</summary>

<p>
  <img src="docs/screenshots/compare_before.svg" width="360" alt="Before (placeholder)" />
  <img src="docs/screenshots/compare_after.svg" width="360" alt="After (placeholder)" />
</p>

</details>

<details>
<summary>Companion app (placeholders)</summary>

<p>
  <img src="docs/screenshots/app_dashboard.svg" width="360" alt="Dashboard (placeholder)" />
  <img src="docs/screenshots/app_settings.svg" width="360" alt="Settings (placeholder)" />
</p>

</details>

## FAQ

**Nothing changes in Amazon**

See [Troubleshooting](#troubleshooting).

**Some products or sections are missing (blank lists, missing tiles)**

1. Disable CSS injection in AmznKiller settings to confirm selectors are the cause.
2. Update selectors from the dashboard (tap refresh).
3. If it persists, open an issue with: AmznKiller version, Amazon app version, WebView version,
   selector count.

**Sync says "remote failed" or "embedded only"**

Embedded selectors are still applied. Check connectivity, reset the selector URL in settings,
and refresh again.

**Does this block network requests or just hide elements?**

Cosmetic only. It injects CSS to hide ad elements. Network requests still happen.
Works alongside DNS-based blockers (AdGuard, NextDNS, Private DNS, hosts files).

**Does this work on Amazon Lite or other Amazon apps?**

No. The module is scoped to `com.amazon.mShop.android.shopping` only for now.

**Do I need to reboot after updating selectors?**

No. Updated selectors apply on the next page load inside Amazon Shopping.

## Troubleshooting

1. Confirm the module is enabled in LSPosed and scoped to Amazon Shopping.
2. Force stop Amazon Shopping, then reopen (or reboot).
3. Open AmznKiller. Verify Xposed is active and selector count is above 0.
4. Tap refresh on the dashboard. If it fails, reset the selector URL in settings and retry.
5. If pages look broken, disable CSS injection temporarily, reopen Amazon Shopping, update
   selectors, then re-enable.

## Build

```bash
git clone --recurse-submodules https://github.com/hxreborn/amznkiller.git
cd amznkiller
./gradlew :app:assembleRelease
```

Requires JDK 21. Configure `local.properties`:

```properties
sdk.dir=/path/to/android/sdk
```

## Contributing

Pull requests are welcome. For bugs or feature requests, [open an issue](https://github.com/hxreborn/amznkiller/issues/new/choose).

## License

<a href="LICENSE"><img src=".github/assets/gplv3.svg" height="90" alt="GPLv3"></a>

This project is licensed under the GNU General Public License v3.0 – see the [LICENSE](LICENSE) file
for details.
