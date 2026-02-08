# AmznKiller

AmznKiller is an Xposed module built on the modern LSPosed API that removes ads, "Sponsored"
products, video carousels, and other promotional junk from the Amazon Android app.

![Update Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/update-selectors.yml/badge.svg)
![Validate Selectors](https://github.com/hxreborn/amznkiller/actions/workflows/validate-selectors.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/API-28%2B-3DDC84?logo=android&logoColor=white)

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
