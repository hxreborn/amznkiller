# AmznKiller

Xposed module built on the modern LSPosed API that hides ads and sponsored content inside the Amazon Android app.

![Update Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/update-selectors.yml/badge.svg)
![Validate Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/validate-selectors.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/API-28%2B-3DDC84?logo=android&logoColor=white)

## Features

- Remove sponsored cards, video carousels, and other promotional UI in the Amazon app
- Maintained built-in selector list with remote updates. Optionally use your own self-hosted selector list via custom URL.
- Selector sanitization blocks common CSS injection patterns
- Material 3 companion app
- Recommended alongside Private DNS and hosts-based blocking
- Free and open source (FOSS)

## Requirements

- Android 9 (API 28) or higher
- [LSPosed](https://github.com/JingMatrix/LSPosed) (JingMatrix fork recommended)
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

Search query: *"macbook air m1 16gb 512"*

<table>
<tr><th>Stock (2 real results, rest are ads)</th><th>Patched</th></tr>
<tr>
  <td><img src="https://github.com/user-attachments/assets/4c2fb092-0e63-4a34-b9ba-afe1a4028b09" width="280" alt="Stock Amazon app" /></td>
  <td><img src="https://github.com/user-attachments/assets/135e1fec-b0e9-4dc1-9d58-49fabd825262" width="280" alt="Patched Amazon app" /></td>
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
<summary>Sync says "remote failed" or "embedded only"</summary>

Embedded selectors are still applied. Check connectivity, reset the selector URL in settings,
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

# Build libxposed and publish to local Maven repo
./gradlew buildLibxposed
./gradlew :app:assembleDebug          # or assembleRelease (requires signing config)
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
