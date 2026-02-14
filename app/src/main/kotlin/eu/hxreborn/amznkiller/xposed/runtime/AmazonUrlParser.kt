package eu.hxreborn.amznkiller.xposed.runtime

object AmazonUrlParser {
    private val ASIN_RE = Regex("/(?:dp|gp/product)/([A-Z0-9]{10})", RegexOption.IGNORE_CASE)
    private val HOST_RE = Regex("https?://(?:www\\.)?(amazon\\.[a-z.]+)")

    fun parse(url: String): AmazonUrlInfo {
        val isAmazon = url.contains("amazon.")
        return AmazonUrlInfo(
            isAmazon = isAmazon,
            isProductPage = isAmazon && (url.contains("/dp/") || url.contains("/gp/product/")),
            asin = ASIN_RE.find(url)?.groupValues?.get(1),
            domain = HOST_RE.find(url)?.groupValues?.get(1),
        )
    }
}
