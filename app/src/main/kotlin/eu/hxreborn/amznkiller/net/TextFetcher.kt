package eu.hxreborn.amznkiller.net

import java.net.HttpURLConnection
import java.net.URI

object TextFetcher {
    fun fetch(
        url: String,
        connectTimeoutMs: Int = 15_000,
        readTimeoutMs: Int = 30_000,
    ): String {
        val conn = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            conn.connectTimeout = connectTimeoutMs
            conn.readTimeout = readTimeoutMs
            conn.requestMethod = "GET"
            conn.setRequestProperty(
                "User-Agent",
                "amznkiller/1.0 (selector-updater)",
            )
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }
}
