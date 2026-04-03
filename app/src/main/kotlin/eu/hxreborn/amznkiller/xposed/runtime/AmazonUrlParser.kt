package eu.hxreborn.amznkiller.xposed.runtime

object AmazonUrlParser {
    private val PRODUCT_PATHS = listOf("/dp/", "/gp/product/", "/gp/aw/d/")
    private val ASIN_RE =
        Regex("/(?:dp|gp/product|gp/aw/d)/([A-Z0-9]{10})", RegexOption.IGNORE_CASE)
    private val HOST_RE = Regex("https?://(?:www\\.)?(amazon\\.[a-z.]+)")

    fun parse(url: String): AmazonUrlInfo {
        val isAmazon = url.contains("amazon.")
        return AmazonUrlInfo(
            isAmazon = isAmazon,
            isProductPage = isAmazon && PRODUCT_PATHS.any { url.contains(it) },
            asin = ASIN_RE.find(url)?.groupValues?.get(1),
            domain = HOST_RE.find(url)?.groupValues?.get(1),
        )
    }
}
