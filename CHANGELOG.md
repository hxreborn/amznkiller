
## What's Changed



### Features

- [`25eeb8c`](https://github.com/hxreborn/amznkiller/commit/25eeb8c28725b0962cc80b637b90b46b722e1f85) *(hooks)* Add verbose logging to help debug force dark by @hxreborn

- [`5732872`](https://github.com/hxreborn/amznkiller/commit/573287219d868ad6816be3c45065681127441ffa) *(prefs)* Replace force dark toggle with mode selector by @byemaxx in [#24](https://github.com/hxreborn/amznkiller/pull/24)

- [`4d5b860`](https://github.com/hxreborn/amznkiller/commit/4d5b860ad5d41bf9b8b3015b77ab108c767bf3db) *(selectors)* Expand ad blocking for EU variants and fix carousel false positives by @hxreborn

- [`42c6ee3`](https://github.com/hxreborn/amznkiller/commit/42c6ee3bd0685b2b05b83f957b483d51da6afa87) *(ui)* Match nav-bar slide to tab order

- [`c75524d`](https://github.com/hxreborn/amznkiller/commit/c75524d11f55cd57f90a8e2d44706c704a9e5829) *(ui)* Auto-fetch selectors on app open

- [`2fb696f`](https://github.com/hxreborn/amznkiller/commit/2fb696ff7d968768caa63dc5c3b2e0fcba4d635c) *(ui)* Add expressive floating-pill nav bar

- [`54e7661`](https://github.com/hxreborn/amznkiller/commit/54e766101e7489a0da4e76e1b728ef953fecbb39) Add option to disable Rufus (Amazon AI assistant) by @hxreborn


### Refactor

- [`6628458`](https://github.com/hxreborn/amznkiller/commit/6628458ade72e3c43bfdf2d220d5c562a43f49f3) *(scripts)* Clean up rule-table in update_selectors by @hxreborn

- [`96e2262`](https://github.com/hxreborn/amznkiller/commit/96e2262a0d72fbae8f441e917fa9093e0920d592) *(ui)* Use M3 Expressive LoadingIndicator

- [`6064485`](https://github.com/hxreborn/amznkiller/commit/606448577d69918738f6ce7ae597244f69662709) *(ui)* Tokenize design surface


### CI/CD

- [`de2bf1f`](https://github.com/hxreborn/amznkiller/commit/de2bf1fea51db672a1ad3292d66b62e5d1b4f951) *(deps)* Bump actions/upload-artifact from 4 to 7 in the actions group by @dependabot[bot] in [#29](https://github.com/hxreborn/amznkiller/pull/29)

- [`f882700`](https://github.com/hxreborn/amznkiller/commit/f88270016ab126bfa5f82c2717ec2220715d5ecf) *(deps)* Bump the actions group with 8 updates by @dependabot[bot] in [#20](https://github.com/hxreborn/amznkiller/pull/20)


### Miscellaneous

- [`666a7de`](https://github.com/hxreborn/amznkiller/commit/666a7dedb1a1a45d0d77723ee1944c2577804bb8) *(gitignore)* Ignore tarball artifacts by @hxreborn

- [`b5bf20a`](https://github.com/hxreborn/amznkiller/commit/b5bf20a3149b0422174212e5320b370ca8caffc0) *(selectors)* Refresh upstream sources lock by @hxreborn

- [`40cd5cf`](https://github.com/hxreborn/amznkiller/commit/40cd5cf13f7f3cb76c34f16f33fd2e021a8fd816) Release v2.2.0



## What's Changed



### Features

- [`f3e9022`](https://github.com/hxreborn/amznkiller/commit/f3e902273db5b8ef21b7c8f5d1e24202a80f0d00) Add hide launcher icon setting by @hxreborn


### Bug Fixes

- [`ab6c401`](https://github.com/hxreborn/amznkiller/commit/ab6c4010c6b7422d7ef86cf13406e8ed71993293) *(hooks)* Gate setSystemBarsAppearance on API 30+ by @hxreborn

- [`82c38b1`](https://github.com/hxreborn/amznkiller/commit/82c38b11e59759624d0c787986de4c8d557e3afe) *(manifest)* Set fullBackupContent for pre-Android 12 by @hxreborn

- [`725317a`](https://github.com/hxreborn/amznkiller/commit/725317a505f87f3d1dc6785d1609c2e6c6b0901c) *(ui)* Add monochrome adaptive icon layer by @hxreborn


### Refactor

- [`61f6ff0`](https://github.com/hxreborn/amznkiller/commit/61f6ff055a60cabb60a296b62823250c8231c8df) *(hooks)* Use Color.toDrawable ktx by @hxreborn

- [`1f0d3e9`](https://github.com/hxreborn/amznkiller/commit/1f0d3e97df0facebe7c705c66b14cfc94398b4f3) *(prefs)* Drop unused PrefSpec.reset by @hxreborn

- [`472b555`](https://github.com/hxreborn/amznkiller/commit/472b555c3c1a52a9e06ba9ff9a0eef996c4ec6a2) *(selectors)* Drop unused error field on MergeResult.Partial by @hxreborn

- [`1ff31fb`](https://github.com/hxreborn/amznkiller/commit/1ff31fb87cf12937cece16bb3c45908fe86643d4) *(strings)* Convert count strings to plurals by @hxreborn

- [`5577f58`](https://github.com/hxreborn/amznkiller/commit/5577f583455383f881a50389a981882119735638) *(theme)* Make EXPANDED_TITLE_MAX_LINES const by @hxreborn

- [`b054615`](https://github.com/hxreborn/amznkiller/commit/b054615e66f2c0d857a6262693b0aa7b5dd0d537) *(ui)* Drop redundant guards in UpdatesCard status when by @hxreborn

- [`1783c4c`](https://github.com/hxreborn/amznkiller/commit/1783c4c7ee051a91ab65aa779f98c77539188d94) *(ui)* Use LocalResources for tagline lookup by @hxreborn


### Miscellaneous

- [`a3d9b51`](https://github.com/hxreborn/amznkiller/commit/a3d9b5112f91164bd982149a5e7dbabf02084116) *(strings)* Drop unused string resources by @hxreborn

- [`e540514`](https://github.com/hxreborn/amznkiller/commit/e540514044bf3d023c14d5aa7c936938febe181a) Release v2.1.0 by @hxreborn



## What's Changed



### Bug Fixes

- [`0b18141`](https://github.com/hxreborn/amznkiller/commit/0b18141be219042af2fc090ea0b7655f0c16deca) *(charts)* Show charts on cart product links by @hxreborn

- [`ba8a66b`](https://github.com/hxreborn/amznkiller/commit/ba8a66be148310e409e7a9a32bab6dff1719cf12) *(ui)* Keep splash until settings state is ready by @hxreborn


### Refactor

- [`05db728`](https://github.com/hxreborn/amznkiller/commit/05db728275460981fb7d4631ac11288d8ff46894) *(ui)* Split screen state and extract dashboard/settings components by @hxreborn

- [`64f0a94`](https://github.com/hxreborn/amznkiller/commit/64f0a9494731c5ef70f510a33333e79bdbb6d457) *(ui)* Remove selector bottom sheet from dashboard by @hxreborn


### CI/CD

- [`571f0ee`](https://github.com/hxreborn/amznkiller/commit/571f0ee31053c69b408c6b44f08d3510a1c0bc6d) Add Dependabot config for monthly Gradle updates by @hxreborn


### Miscellaneous

- [`5eda6dc`](https://github.com/hxreborn/amznkiller/commit/5eda6dc04d34b905a5189231f489f08b239f4c7f) Release v2.0.1 by @hxreborn



## What's Changed



### Features

- [`8006a71`](https://github.com/hxreborn/amznkiller/commit/8006a717153e6a2e82bbc37661539cd1cc645add) Migrate to libxposed API 101 [**breaking**] by @hxreborn in [#14](https://github.com/hxreborn/amznkiller/pull/14)


### Miscellaneous

- [`5214751`](https://github.com/hxreborn/amznkiller/commit/5214751cf0a21cb0b6e6be57d25dac827cda79b8) Release v2.0.0 by @hxreborn



## What's Changed



### Features

- [`bb74fa4`](https://github.com/hxreborn/amznkiller/commit/bb74fa43dfe7cca0b72b2546ff6b6a93db30f9ab) *(hooks)* Add debug logging to ForceDarkHooker pipeline by @hxreborn


### Bug Fixes

- [`31a4695`](https://github.com/hxreborn/amznkiller/commit/31a4695e2a8e64ce89b9f1da000905d008d8ae56) *(charts)* Re-inject price charts on SPA variant navigation by @hxreborn

- [`21064f2`](https://github.com/hxreborn/amznkiller/commit/21064f2074c88201eb1d329c9a7fafa26eff18c3) *(selectors)* Hide homepage video/creative ad cards by @hxreborn


### Miscellaneous

- [`a51b9a3`](https://github.com/hxreborn/amznkiller/commit/a51b9a3594c6a3cb9caa9ce1c58de7f2f4620a8e) Release v1.2.3 by @hxreborn



## What's Changed



### Features

- [`4796bc4`](https://github.com/hxreborn/amznkiller/commit/4796bc438bdd2a01edae7a8a2714074c4cc0bc62) *(ui)* Show sync delta in UpdatesCard after refresh by @hxreborn


### Bug Fixes

- [`009ef83`](https://github.com/hxreborn/amznkiller/commit/009ef834de518386e0fe102a0df07e1ec08d1630) *(charts)* Prevent injection inside buybox swatch buttons by @hxreborn

  > Fixes #11


### Refactor

- [`996d3b1`](https://github.com/hxreborn/amznkiller/commit/996d3b185c3a2c25e39442212d6d5f329861b7cd) *(ui)* Remove WIP placeholder rows from SystemEnvironmentCard by @hxreborn

- [`b52cef4`](https://github.com/hxreborn/amznkiller/commit/b52cef4fc88fdadbab41b86061ed4193ae6bdeb2) *(ui)* Use check/X icons for ad blocking status card by @hxreborn

- [`1a516b6`](https://github.com/hxreborn/amznkiller/commit/1a516b65fb4cc0d2913c12803ea51281065c6204) *(ui)* Add Android 15 requirement note to force dark toggle by @hxreborn


### Miscellaneous

- [`169074a`](https://github.com/hxreborn/amznkiller/commit/169074afdf5cea3490cb3c59467cf6d0d1765c67) Release v1.2.2 by @hxreborn



## What's Changed



### Bug Fixes

- [`8317698`](https://github.com/hxreborn/amznkiller/commit/8317698b955a41aabfb3b1aeb3af06ade9864867) *(xposed)* Stop writing to read-only remote prefs in hooked process by @hxreborn

  > Closes #10


### CI/CD

- [`076cf5c`](https://github.com/hxreborn/amznkiller/commit/076cf5c3b7d189c555cda5f1bc4d54d5978784bf) Add stale issue auto-close workflow by @hxreborn


### Miscellaneous

- [`27e6949`](https://github.com/hxreborn/amznkiller/commit/27e6949df99b7827f0e63db54b52628287f386a5) Release v1.2.1 by @hxreborn



## What's Changed



### Features

- [`2d8e4c8`](https://github.com/hxreborn/amznkiller/commit/2d8e4c8cc0ec53521a81b03d69ba9ff6f58de9ea) Add Amazon India package support by @hxreborn

  > Fixes #8


### Refactor

- [`a5ca687`](https://github.com/hxreborn/amznkiller/commit/a5ca6875e8ce884227e0613e805125fc388acf57) *(scripts)* Stop writing embedded.css from update_selectors by @hxreborn


### Miscellaneous

- [`613b6b8`](https://github.com/hxreborn/amznkiller/commit/613b6b833da4b581a2a6155955b3817dcf0880e1) Release v1.2.0 by @hxreborn

- [`a570b28`](https://github.com/hxreborn/amznkiller/commit/a570b288c503feccd41dae0f8d30498e1706f3fb) Exclude markdown files from linguist stats by @hxreborn



## What's Changed



### Features

- [`ed754f6`](https://github.com/hxreborn/amznkiller/commit/ed754f668ddfd45be328a50aab413b00fbea9714) Initial price history charts and experimental force dark mode by @hxreborn


### Bug Fixes

- [`2f0c94e`](https://github.com/hxreborn/amznkiller/commit/2f0c94edcbb7901d8ab500c3ba56abdcf9d35d8a) *(ui)* Correct license screen wording by @hxreborn

- [`b620c55`](https://github.com/hxreborn/amznkiller/commit/b620c556d9cc45c9d178064686a5ef96e961d732) *(xposed)* Pass classLoader to ForceDarkHooker.hook by @hxreborn

- [`0667f1a`](https://github.com/hxreborn/amznkiller/commit/0667f1ae153349fddd539538b04a7ff8966bc5c1) *(xposed)* Make bottom nav icons visible under GPU force dark by @hxreborn

- [`0d4141a`](https://github.com/hxreborn/amznkiller/commit/0d4141a9fdab44d7752e9599395b7fe0cbd417d4) Close resource leak in embedded selector loader by @hxreborn


### Refactor

- [`11b3167`](https://github.com/hxreborn/amznkiller/commit/11b31672aae7aac1f4a25041d8cfd136090e9525) *(ui)* Remove copy-on-click from version preference by @hxreborn

- [`f50aeb6`](https://github.com/hxreborn/amznkiller/commit/f50aeb673f62bccc752affbffa6c41426f28406e) *(ui)* Adopt Google Blue 500 accent and redesign dashboard by @hxreborn

- [`a7a9afe`](https://github.com/hxreborn/amznkiller/commit/a7a9afeb729996bb8a06935a50780d7c9456808e) *(xposed)* Extract modular injector pipeline by @hxreborn

- [`e383ad7`](https://github.com/hxreborn/amznkiller/commit/e383ad7a7b0c9daaa733371e6ba16cd5faeed363) Fix TODOs and code cleanup by @hxreborn


### CI/CD

- [`4a76cff`](https://github.com/hxreborn/amznkiller/commit/4a76cff6b661209a55ba602075547a80e213cb5a) Add mirror-release workflow by @hxreborn


### Miscellaneous

- [`ce7a042`](https://github.com/hxreborn/amznkiller/commit/ce7a042ebbdcd49ecf0f6f1b39ea8893a4a94b34) Release v1.1.0 by @hxreborn

- [`8a414eb`](https://github.com/hxreborn/amznkiller/commit/8a414eb74db9727a3da770cde075c5c51926887c) Add organization options to issue templates by @hxreborn

- [`a73bf43`](https://github.com/hxreborn/amznkiller/commit/a73bf43a5aa90eb25fd258d9424621f8ef90b104) Assorted build and resource fixes by @hxreborn

- [`215890c`](https://github.com/hxreborn/amznkiller/commit/215890c1011508df4fccff9c866bdb729eee85cb) Remove unreferenced local screenshot assets by @hxreborn



## What's Changed



### Features

- [`c990808`](https://github.com/hxreborn/amznkiller/commit/c99080819544f4209d6919ea6bab654c8257e75d) *(ui)* Add animated splash screen by @hxreborn

- [`839b2e4`](https://github.com/hxreborn/amznkiller/commit/839b2e44947ca79a462f35bb90494ff23c8ce446) Add ghost state to metrics grid when injection is disabled by @hxreborn

- [`2e2755e`](https://github.com/hxreborn/amznkiller/commit/2e2755e3fc55046f1621823386f7f0d6814479a8) Persist refresh failure state and use string resources for error messages by @hxreborn


### Bug Fixes

- [`36e713f`](https://github.com/hxreborn/amznkiller/commit/36e713fe8e707356b63100646521f70d7060d45b) *(hook)* Skip toast when amazon starts in bg by @hxreborn

- [`a2185b7`](https://github.com/hxreborn/amznkiller/commit/a2185b76ff3935bdf27404e09a709371a6ec19a1) *(util)* Add missing XposedModule import by @hxreborn

- [`073fd06`](https://github.com/hxreborn/amznkiller/commit/073fd0695deb78600910b97a0745fb64dda385d9) Split Logger into debug and info tiers by @hxreborn

- [`b3dc297`](https://github.com/hxreborn/amznkiller/commit/b3dc29743c28acacf7e1f9bff61aa92855561353) Gate WebView debugging on WEBVIEW_DEBUGGING pref by @hxreborn

- [`b04643c`](https://github.com/hxreborn/amznkiller/commit/b04643c8ec4c1d2ea7a2b646072a1ed552970bad) Replace report-issue icon with Feedback by @hxreborn


### Refactor

- [`ddb01f7`](https://github.com/hxreborn/amznkiller/commit/ddb01f78bff0293775a35f953006c857ba67058d) Simplify toast logic in AmznkillerModule by @hxreborn

- [`b973482`](https://github.com/hxreborn/amznkiller/commit/b97348227582b7bed5ce619d62eab7261975e4eb) Reduce StyleInjector.inject() complexity by @hxreborn

- [`e412094`](https://github.com/hxreborn/amznkiller/commit/e4120940fc0890de92776eb94179260210af4137) Improve ShapeUtil readability and TimeFormat granularity by @hxreborn

- [`6d6ddc7`](https://github.com/hxreborn/amznkiller/commit/6d6ddc76430bf5daa7e06a0d1244a54c65b7743a) Simplify dashboard state, remove dead code, reorganize packages by @hxreborn


### Miscellaneous

- [`d390e2c`](https://github.com/hxreborn/amznkiller/commit/d390e2cf1999a0ea30ec03769f9a50c998285716) Release v1.0.0 by @hxreborn

- [`a7df079`](https://github.com/hxreborn/amznkiller/commit/a7df07919be31ff870cbdf55116e5f92803a9584) Add --allow-dirty flag to release script by @hxreborn

- [`7b4596e`](https://github.com/hxreborn/amznkiller/commit/7b4596ef240272ad56eab5c882951082e5bf7743) Add GPLv3 license file by @hxreborn



## What's Changed



### Features

- [`90542f4`](https://github.com/hxreborn/amznkiller/commit/90542f4206d81b379d1efb514cac23fdafe1d8fd) *(hook)* Add random toast on module load by @hxreborn

- [`48d1f14`](https://github.com/hxreborn/amznkiller/commit/48d1f141fbec14f8964528978e6c83fc50f4c0cd) *(theme)* Add dracula palette for non-dynamic color mode by @hxreborn

- [`0109051`](https://github.com/hxreborn/amznkiller/commit/01090516596d651f20ce52a296c24994d9009e17) *(ui)* Redesign complete ui by @hxreborn

- [`7d5f3c5`](https://github.com/hxreborn/amznkiller/commit/7d5f3c5ae37fd963c49eb392618be7dd43c28344) *(ui)* Redesign dashboard as preference list by @hxreborn

- [`22ece5d`](https://github.com/hxreborn/amznkiller/commit/22ece5d862d2b83f10e3f2fa64119edf937c0b45) *(ui)* Add bottom nav and settings screen by @hxreborn

- [`e452272`](https://github.com/hxreborn/amznkiller/commit/e452272179c770e83ebceedaee67bf12a60ebcea) Add injection toggle and compose previews by @hxreborn


### Bug Fixes

- [`d549176`](https://github.com/hxreborn/amznkiller/commit/d549176d85b637a022a76c2ada806e95322b81b7) *(hook)* Dedup injection and hook onPageCommitVisible by @hxreborn


### Refactor

- [`1a2e776`](https://github.com/hxreborn/amznkiller/commit/1a2e7761508ccd9e07db42b46c7da2cd3fe9d4d5) *(ui)* Replace shapeForPosition(1,0) with Tokens.CardShape by @hxreborn

- [`48cf9ca`](https://github.com/hxreborn/amznkiller/commit/48cf9ca0cc1ce7c28fdd71281eaf3832e0a8c6e4) *(ui)* Replace zygisk row with WIP placeholders by @hxreborn

- [`238ff2d`](https://github.com/hxreborn/amznkiller/commit/238ff2d291e1863d6c8dc049db1b5bdfc3f57121) Rename state classes, inline FQNs, fix idioms by @hxreborn


### Miscellaneous

- [`e2add52`](https://github.com/hxreborn/amznkiller/commit/e2add521d546799b5cbbd629833a766159e40d7c) Release v1.0.0-rc1 by @hxreborn

- [`12f5b68`](https://github.com/hxreborn/amznkiller/commit/12f5b6844f79205ad41cdfa1cb450e1587eebafc) Sync selectors and add static rules by @hxreborn



## What's Changed



### Bug Fixes

- [`e1d7d32`](https://github.com/hxreborn/amznkiller/commit/e1d7d328e7068a3658d38cbde5a5088ca46dad84) *(build)* Use commit-based version code by @hxreborn


### Refactor

- [`fa9030e`](https://github.com/hxreborn/amznkiller/commit/fa9030e4451470041e3c417197e2767696c0e752) Split ViewModel and reorganize packages by @hxreborn


### Miscellaneous

- [`be28760`](https://github.com/hxreborn/amznkiller/commit/be28760886499550fa6c3eca6336c024a30c8adc) Release v1.0.0-beta3 by @hxreborn

- [`d9c1655`](https://github.com/hxreborn/amznkiller/commit/d9c16551ea5572f481ceb6ebeeab3a7246fb26d4) Hide TOML from linguist detection by @hxreborn



## What's Changed



### Bug Fixes

- [`cf798df`](https://github.com/hxreborn/amznkiller/commit/cf798df75d9eca01146903b58946bc58edb77acc) *(build)* Handle pre-release in version code by @hxreborn


### Miscellaneous

- [`fadd56c`](https://github.com/hxreborn/amznkiller/commit/fadd56c2693e1d02388e8101f7033ee7b4ddf682) Release v1.0.0-beta2 by @hxreborn



## What's Changed



### Features

- [`6b45f2c`](https://github.com/hxreborn/amznkiller/commit/6b45f2c368408795a38caa01883b9036a5f3449a) *(ui)* Add expanded title, overscroll, tokens by @hxreborn

- [`004e455`](https://github.com/hxreborn/amznkiller/commit/004e455ddb49decc8f5d67f8d0d8056a8a54e95f) *(ui)* Add splash screen and wobble by @hxreborn

- [`05f2e62`](https://github.com/hxreborn/amznkiller/commit/05f2e6245f732f6a7d2d3d9bb606b7163852ec09) Add companion app and filter UI by @hxreborn

- [`3852314`](https://github.com/hxreborn/amznkiller/commit/38523148420e9989f17769b7895ae5c74c5afb64) Add remote selector list by @hxreborn


### Bug Fixes

- [`eefca2f`](https://github.com/hxreborn/amznkiller/commit/eefca2f16e074c92eba03e378b1d8df3b9bbe322) *(ci)* Narrow diff to selector content by @hxreborn

- [`51f6fde`](https://github.com/hxreborn/amznkiller/commit/51f6fde349c48022524236ca40c2ff92e80ef544) *(tools)* Skip missing files in validate by @hxreborn

- [`b7b1063`](https://github.com/hxreborn/amznkiller/commit/b7b1063ef1f9c5ac9a47f6fb737e5a5db1127005) *(tools)* Fix ternary and O(n²) scan by @hxreborn

  > write_sorted:parenthesize ternary for precedence.

  > cmd_validate:Counter replaces quadratic .count().

- [`dae28fa`](https://github.com/hxreborn/amznkiller/commit/dae28fa3a2959a22602118e8034e81bc28eec5e3) *(ui)* Propagate fetch errors to UI by @hxreborn


### Refactor

- [`ab426cb`](https://github.com/hxreborn/amznkiller/commit/ab426cb6d04cbbec4b879ab646cccbb2a5a642bb) *(structure)* Reorganize tools/ into scripts/ci/ by @hxreborn

- [`471bea7`](https://github.com/hxreborn/amznkiller/commit/471bea70a6ef31cde9488408676d0956ef58e122) *(ui)* Rewrite copy, drop card heading by @hxreborn

- [`fdd48f5`](https://github.com/hxreborn/amznkiller/commit/fdd48f5c039b8ad5a19df89d9a9adb036f8d097f) Rename manual.txt to static.txt by @hxreborn

- [`e9294a3`](https://github.com/hxreborn/amznkiller/commit/e9294a394eacec44f8724f3d0bbb1e7734ae90fe) Harden fetch, sanitize, inject by @hxreborn

  > TextFetcher:validate HTTP status, enforce 512KB limit.

  > SelectorSanitizer:reject injection primitives and
adblock syntax; apply at prefs read boundary.

  > MergeResult:seal as Success/Partial.

  > PrefSpec.copyTo:eliminate type dispatch.

  > StyleInjector:cache inject+validate script pair.

- [`fbd104c`](https://github.com/hxreborn/amznkiller/commit/fbd104c2c364db49e53c14a301f15a5f7b247777) Rename ModuleEntry to AmznkillerModule by @hxreborn


### CI/CD

- [`a1b6561`](https://github.com/hxreborn/amznkiller/commit/a1b656189993dac22a261cfdffac4e8f0b33bccf) Make changelog generation non-fatal by @hxreborn

- [`5335d2c`](https://github.com/hxreborn/amznkiller/commit/5335d2c31e95733d7174538cb775f3643d16ab7f) Add git-cliff changelog generation by @hxreborn

- [`e0beefb`](https://github.com/hxreborn/amznkiller/commit/e0beefb28295b8b6f0eab907884038a67c414bca) Add PR and push build gate by @hxreborn

- [`b06d6e8`](https://github.com/hxreborn/amznkiller/commit/b06d6e8dbae580c3bfd16a48c565dfcceb900259) Add tag-triggered build and release workflow by @hxreborn

- [`b925235`](https://github.com/hxreborn/amznkiller/commit/b9252354e0d431f4824a417bbfed691420ed1ab6) Rename auto-PR title by @hxreborn

- [`f50153c`](https://github.com/hxreborn/amznkiller/commit/f50153c650b901ed3b2c2befff64dede760d4fb8) Add Chromium selector validation by @hxreborn


### Miscellaneous

- [`76b7026`](https://github.com/hxreborn/amznkiller/commit/76b702692f88beda16a19634f46afa680c78b65f) Remove yml from linguist by @hxreborn

- [`f2ea40c`](https://github.com/hxreborn/amznkiller/commit/f2ea40cd996568c70e2ab37aced7a95b97da9d8b) Release v1.0.0-beta1 by @hxreborn

- [`c3dae3b`](https://github.com/hxreborn/amznkiller/commit/c3dae3bc26d707b4f393aeea42e802a7700d1e4c) Track merged selector list by @hxreborn




