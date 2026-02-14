package eu.hxreborn.amznkiller.xposed.js

enum class ScriptId(
    val path: String,
) {
    AD_BLOCK("payload/js/ad_block.js"),
    DARK_MODE("payload/js/dark_mode.js"),
    CHARTS("payload/js/charts.js"),
}
