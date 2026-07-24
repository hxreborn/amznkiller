package eu.hxreborn.amznkiller.http

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

object HttpClient {
    private const val MAX_BYTES = 512_000L
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 10_000
    private const val BUFFER_SIZE = 8192

    fun isHttps(url: String): Boolean =
        runCatching {
            URI(url.trim()).scheme.equals("https", ignoreCase = true)
        }.getOrDefault(false)

    fun fetch(url: String): String {
        val target = url.trim()
        if (!isHttps(target)) throw IOException("Only https URLs are allowed")
        val conn =
            (URI(target).toURL().openConnection() as? HttpURLConnection)
                ?: error("Non-HTTP URL: $target")
        try {
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "amznkiller/1.0")
            validateResponse(conn)
            if (!isHttps(conn.url.toString())) throw IOException("Redirected to a non-https URL")
            return readLimited(conn.inputStream)
        } finally {
            conn.disconnect()
        }
    }

    private fun validateResponse(conn: HttpURLConnection) {
        val code = conn.responseCode
        if (code !in 200..299) throw IOException("HTTP $code")
    }

    private fun readLimited(stream: InputStream): String =
        stream.use {
            val baos = ByteArrayOutputStream()
            val chunk = ByteArray(BUFFER_SIZE)
            var total = 0L
            while (true) {
                val bytesRead = it.read(chunk)
                if (bytesRead < 0) break
                total += bytesRead
                if (total > MAX_BYTES) throw IOException("Response exceeded $MAX_BYTES bytes")
                baos.write(chunk, 0, bytesRead)
            }
            baos.toString(Charsets.UTF_8.name())
        }
}
