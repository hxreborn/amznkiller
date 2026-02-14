package eu.hxreborn.amznkiller.xposed.runtime

data class AmazonUrlInfo(
    val isAmazon: Boolean,
    val isProductPage: Boolean,
    val asin: String?,
    val domain: String?,
)
