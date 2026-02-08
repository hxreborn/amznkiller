# AmznKiller

AmznKiller is an Xposed module built on the modern LSPosed API. It currently does cosmetic ad blocking
via CSS injection: it hides ads, "Sponsored" products, video carousels, and other promotional junk
inside the Amazon Android app.

![Update Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/update-selectors.yml/badge.svg)
![Validate Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/validate-selectors.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/API-28%2B-3DDC84?logo=android&logoColor=white)

## Features

- Cosmetic ad blocking inside Amazon Shopping, CSS injection into WebViews
- Hides sponsored cards, carousels, promo components
- De-prioritizes paid placements, cleaner search relevance
- Selector sources, remote list support, embedded fallback, custom selector URL, supports your own maintained lists
- Selector sanitizer, rejects unsafe selector input
- Companion app, status and configuration, manual refresh
- Compatible with Private DNS and hosts-based blocking, complements network blockers
- FOSS
- No network-level blocking, for now

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

- **Nothing changes in Amazon**
  Try: confirm the module is enabled in LSPosed and scoped to `com.amazon.mShop.android.shopping`; force stop
  Amazon Shopping and open it again (sometimes a reboot is needed); open AmznKiller and check the dashboard
  shows Xposed as active and a selector count above 0.

- **Some products or sections do not appear (blank lists, missing tiles)**
  Try: disable `CSS injection` in AmznKiller settings to confirm it is rule-related; update selectors from the
  dashboard (tap refresh); if it still happens, open an issue and include AmznKiller version, Amazon app version,
  WebView version, and selector count.

- **Updater says remote failed / embedded only**
  The embedded rules are still applied. Check connectivity, then reset the selector URL in settings and refresh again.

- **Does this block ads at the network level?**
  No. For now it is cosmetic only: it hides elements inside Amazon WebViews by injecting CSS.

- **Compatible with Private DNS / hosts files / AdGuard / NextDNS?**
  Yes. AmznKiller is cosmetic (CSS injection) and doesn’t replace network-level blocking. Using both is fine and recommended.
  If Amazon pages fail to load with a DNS/hosts setup, that’s a network-blocking issue: allowlist the required domains.

## Troubleshooting

1. LSPosed: module enabled, scoped to Amazon Shopping.
2. Restart: force stop Amazon Shopping, then reopen (or reboot).
3. Sanity check (AmznKiller dashboard): Xposed active; selectors > 0.
4. Refresh rules: tap refresh on the dashboard; if needed, reset the selector URL in settings and refresh again.
5. If pages look broken: temporarily disable `CSS injection`, reopen Amazon Shopping, then re-enable after updating rules.

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

## License

<a href="LICENSE"><img src=".github/assets/gplv3.svg" height="90" alt="GPLv3"></a>

This project is licensed under the GNU General Public License v3.0 – see the [LICENSE](LICENSE) file
for details.
