package eu.hxreborn.amznkiller.xposed.runtime

import android.net.Uri

object AmazonUrlParser {
    private val PRODUCT_PATHS = listOf("/dp/", "/gp/product/", "/gp/aw/d/")
    private val ASIN_RE =
        Regex("/(?:dp|gp/product|gp/aw/d)/([A-Z0-9]{10})", RegexOption.IGNORE_CASE)

    fun parse(url: String): AmazonUrlInfo {
        val host = runCatching { Uri.parse(url).host }.getOrNull()?.lowercase()
        if (host == null || "amazon" !in host.split('.')) {
            return AmazonUrlInfo(
                isAmazon = false,
                isProductPage = false,
                asin = null,
                domain = null,
            )
        }
        val path = runCatching { Uri.parse(url).path }.getOrNull().orEmpty()
        return AmazonUrlInfo(
            isAmazon = true,
            isProductPage = PRODUCT_PATHS.any { path.contains(it) },
            asin = ASIN_RE.find(path)?.groupValues?.get(1),
            domain = registrableAmazonDomain(host),
        )
    }

    private fun registrableAmazonDomain(host: String): String {
        val idx = host.indexOf("amazon.")
        return if (idx >= 0) host.substring(idx) else host
    }
}
