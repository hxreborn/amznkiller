# AmznKiller

Xposed module built on the modern LSPosed API that hides ads and sponsored content inside the Amazon Android app.

<p align="center">
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-10+-3DDC84?style=flat&logo=android&logoColor=white" alt="Android 10+" /></a>
  <img src="https://img.shields.io/badge/LSPosed_API-101-8F00FF?style=flat" alt="LSPosed API 101" />
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/compose"><img src="https://img.shields.io/badge/Compose_BOM-2026.01.01-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Compose BOM" /></a>
  <a href="https://gradle.org"><img src="https://img.shields.io/badge/Gradle-9.4-02303A?style=flat&logo=gradle&logoColor=white" alt="Gradle" /></a>
  <a href="https://developer.android.com/build"><img src="https://img.shields.io/badge/AGP-9.1.0-02303A?style=flat&logo=android&logoColor=white" alt="AGP" /></a>
</p>

<p align="center">
  <a href="https://github.com/hxreborn/amznkiller/actions/workflows/android-ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/hxreborn/amznkiller/android-ci.yml?label=build&style=flat&logo=githubactions&logoColor=white" alt="Build" /></a>
  <a href="https://github.com/hxreborn/amznkiller/actions/workflows/update-selectors.yml"><img src="https://img.shields.io/github/actions/workflow/status/hxreborn/amznkiller/update-selectors.yml?label=selector%20sync&style=flat&logo=githubactions&logoColor=white" alt="Selector Sync" /></a>
  <a href="https://github.com/hxreborn/amznkiller/actions/workflows/validate-selectors.yml"><img src="https://img.shields.io/github/actions/workflow/status/hxreborn/amznkiller/validate-selectors.yml?label=validate&style=flat&logo=githubactions&logoColor=white" alt="Selector Validate" /></a>
</p>

<p align="center">
  <a href="https://github.com/hxreborn/amznkiller/releases/latest"><img src="https://img.shields.io/github/v/release/hxreborn/amznkiller?style=flat&logo=github" alt="Release" /></a>
  <a href="https://github.com/hxreborn/amznkiller/releases"><img src="https://img.shields.io/github/downloads/hxreborn/amznkiller/total?style=flat&logo=github" alt="Downloads" /></a>
  <a href="https://github.com/Xposed-Modules-Repo/eu.hxreborn.amznkiller/releases/latest"><img src="https://img.shields.io/github/v/release/Xposed-Modules-Repo/eu.hxreborn.amznkiller?label=xposed%20repo&style=flat&logo=xdadevelopers" alt="Xposed Repo" /></a>
  <a href="https://github.com/Xposed-Modules-Repo/eu.hxreborn.amznkiller/releases"><img src="https://img.shields.io/github/downloads/Xposed-Modules-Repo/eu.hxreborn.amznkiller/total?label=xposed%20downloads&style=flat&logo=xdadevelopers" alt="Xposed Downloads" /></a>
</p>

<p align="center">
  <a href="https://github.com/hxreborn/amznkiller/stargazers"><img src="https://img.shields.io/github/stars/hxreborn/amznkiller?style=flat&logo=github" alt="Stars" /></a>
  <a href="https://github.com/hxreborn/amznkiller/issues"><img src="https://img.shields.io/github/issues/hxreborn/amznkiller?style=flat&logo=github" alt="Issues" /></a>
  <a href="https://github.com/hxreborn/amznkiller/issues?q=is%3Aissue+is%3Aclosed"><img src="https://img.shields.io/github/issues-closed/hxreborn/amznkiller?style=flat&logo=github" alt="Closed Issues" /></a>
  <a href="https://github.com/hxreborn/amznkiller/commits/main"><img src="https://img.shields.io/github/last-commit/hxreborn/amznkiller?style=flat&logo=github" alt="Last Commit" /></a>
  <a href="https://github.com/hxreborn/amznkiller/blob/main/LICENSE"><img src="https://img.shields.io/github/license/hxreborn/amznkiller?style=flat&logo=gnu" alt="License" /></a>
</p>

## Features

- Remove sponsored cards, video carousels, and other promotional UI in the Amazon app
- Maintained built-in selector list with remote updates. Optionally use your own self-hosted selector list via custom URL.
- Selector sanitization blocks common CSS injection patterns
- Price history charts on product pages via Keepa and CamelCamelCamel (US, UK, DE, FR, JP, CA, IT, ES, IN, MX, BR, AU)
- [Force Dark](#how-does-force-dark-work) (experimental) uses the native force dark algorithm with supplementary CSS fixes to darken Amazon pages
- Recommended alongside Private DNS and hosts-based blocking
- Material 3 Expressive settings UI with Jetpack Compose
- Free and open source (FOSS)

## Known Issues

- [Force Dark](#how-does-force-dark-work) is experimental and disabled by default. Requires Android 15 (API 35) or higher for full support. On Android 10-14 force dark may not apply correctly. Some Amazon screens may still have contrast issues even on supported versions
- Price history charts are still being expanded and may not appear on some product pages yet. 

## Requirements

- Android 10 (API 29) or higher
- An LSPosed Manager version with API 101 support (required for now)
- Amazon Shopping app (`com.amazon.mShop.android.shopping`)

## Installation

1. Download the APK:

    <a href="../../releases"><img src="https://github.com/user-attachments/assets/d18f850c-e4d2-4e00-8b03-3b0e87e90954" height="60" alt="Get it on GitHub" /></a>
    <a href="http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22eu.hxreborn.amznkiller%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fhxreborn%2Famznkiller%22%2C%22author%22%3A%22rafareborn%22%2C%22name%22%3A%22AmznKiller%22%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Afalse%7D%22%7D"><img src="https://github.com/user-attachments/assets/dffe8fb9-c0d1-470b-8d69-6d5b38a8aa2d" height="60" alt="Get it on Obtainium" /></a>

2. Enable the module in LSPosed and scope it to `com.amazon.mShop.android.shopping`
3. Open the AmznKiller companion app to verify the module is active and optionally fetch updated
   lists (built-in filters work out of the box)
4. Force-stop the Amazon app and relaunch it. A toast confirms the module is active.

## Screenshots

Search query: _"macbook air m1 16gb 512"_

<table>
<tr><th>Stock (2 real results, rest are ads)</th><th>Patched</th></tr>
<tr>
  <td><img src="https://github.com/user-attachments/assets/4c2fb092-0e63-4a34-b9ba-afe1a4028b09" width="280" alt="Stock Amazon app" /></td>
  <td><img src="https://github.com/user-attachments/assets/135e1fec-b0e9-4dc1-9d58-49fabd825262" width="280" alt="Patched Amazon app" /></td>
</tr>
</table>

<table>
<tr><th>Force Dark</th><th>Price History</th></tr>
<tr>
  <td><img src="https://github.com/user-attachments/assets/f24a9ac7-126e-4eff-b5e7-d3b8cc652158" width="280" alt="Force Dark mode" /></td>
  <td><img src="https://github.com/user-attachments/assets/fbebb479-8bdc-4395-b531-318ac07a68c9" width="280" alt="Price history charts" /></td>
</tr>
</table>

**Settings app**

<table>
<tr>
  <td><img src="https://github.com/user-attachments/assets/d875c580-d81c-4be7-a857-98bc13656432" width="280" alt="Dashboard" /></td>
  <td><img src="https://github.com/user-attachments/assets/88a5aeee-8184-41c6-bf0a-e4d1fb3c1b13" width="280" alt="Selectors" /></td>
</tr>
<tr>
  <td><img src="https://github.com/user-attachments/assets/cb69012d-94a0-4ca9-b6d7-9cd97cd5c580" width="280" alt="Settings" /></td>
  <td><img src="https://github.com/user-attachments/assets/a374ccec-9dce-4538-bdad-e89c15e24307" width="280" alt="Settings (bottom)" /></td>
</tr>
</table>

## FAQ

<details>
<summary>Nothing changes in Amazon</summary>

See [Troubleshooting](#troubleshooting). Most common causes: module not scoped correctly,
missing force stop on Amazon Shopping, or LSPosed not activated.

</details>

<details>
<summary>Some products or sections are missing (blank lists, missing tiles)</summary>

1. Disable CSS injection in AmznKiller settings to confirm selectors are the cause.
2. Update selectors from the dashboard (tap refresh).
3. If it persists, open an issue with: AmznKiller version, Amazon app version, WebView version,
selector count.

 </details>

<details>
<summary>Sync says "remote failed" or "bundled only"</summary>

Bundled selectors are still applied. Check connectivity, reset the selector URL in settings,
and refresh again.

</details>

<details>
<summary>Does this block network requests or just hide elements?</summary>

Cosmetic only. It injects CSS to hide ad elements. Network requests still happen.
Works alongside DNS-based blockers (AdGuard, NextDNS, Private DNS, hosts files).

</details>

<details>
<summary>Does this work on Amazon Lite or other Amazon apps?</summary>

No. The module is scoped to `com.amazon.mShop.android.shopping` only for now.

</details>

<details>
<summary>Do I need to reboot after updating selectors?</summary>

No. Updated selectors apply on the next page load inside Amazon Shopping. Force stop
Amazon Shopping if changes don't appear immediately.

</details>

<details>
<summary id="how-does-force-dark-work">How does Force Dark work?</summary>

Amazon disables Android's force dark algorithm via its theme (`forceDarkAllowed=false`). The
module hooks `ViewRootImpl.determineForceDarkType` and overrides the return to
`FORCE_DARK_ALWAYS`, which triggers GPU-level algorithmic darkening on all content including
WebViews. Additional hooks set dark window backgrounds, theme native nav elements, and prevent
white flash on WebView load. `DarkModeInjector` ships CSS fixes for elements the algorithm gets
wrong (product images, buy buttons, deal badges).

**Limitation:** `ViewRootImpl.determineForceDarkType` was introduced in Android 15 (API 35).
On Android 10-14, this method does not exist so the primary force dark override cannot apply.
A fallback hook on `HardwareRenderer.setForceDark` is attempted but Amazon's theme-level
opt-out still blocks darkening on those versions.

If you spot a rendering issue, open an issue with a screenshot, page URL, and Android version.
You can enable WebView debugging in settings and inspect via `chrome://inspect`.

</details>

## Troubleshooting

1. Confirm the module is enabled in LSPosed and scoped to Amazon Shopping.
2. Force stop Amazon Shopping, then reopen (or reboot).
3. Open AmznKiller. Verify Xposed is active and selector count is above 0.
4. Tap refresh on the dashboard. If it fails, reset the selector URL in settings and retry.
5. If pages look broken, disable CSS injection temporarily, reopen Amazon Shopping, update
   selectors, then re-enable.

## Build

```bash
git clone https://github.com/hxreborn/amznkiller.git
cd amznkiller
./gradlew :app:assembleDebug
```

Requires JDK 21 and Android SDK. Configure `local.properties`:

```properties
sdk.dir=/path/to/android/sdk

# Optional: release signing for reproducible builds
RELEASE_STORE_FILE=<path/to/keystore.jks>
RELEASE_STORE_PASSWORD=<store_password>
RELEASE_KEY_ALIAS=<key_alias>
RELEASE_KEY_PASSWORD=<key_password>
```

## Contributing

See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for guidelines on pull requests, code style, and commit conventions.

For bugs or feature requests, [open an issue](https://github.com/hxreborn/amznkiller/issues/new/choose).

## License

<a href="LICENSE"><img src="https://github.com/user-attachments/assets/b211cf0d-e255-421c-9213-6b6258676013" height="90" alt="GPLv3"></a>

This project is licensed under the GNU General Public License v3.0 – see the [LICENSE](LICENSE) file
for details.
