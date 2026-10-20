package eu.hxreborn.amznkiller.update

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed interface ApiResponse {
    data class Ok(
        val code: Int,
        val body: String,
        val etag: String? = null,
    ) : ApiResponse

    data class Retryable(
        val reason: String,
        val afterSeconds: Long?,
    ) : ApiResponse

    data class Refused(
        val code: Int,
        val reason: String,
    ) : ApiResponse
}

sealed interface ReleaseCheck {
    val known: Release?

    data object Idle : ReleaseCheck {
        override val known: Release? = null
    }

    data class Checking(
        override val known: Release?,
    ) : ReleaseCheck

    data class Done(
        val release: Release?,
        val checkedAt: Long,
    ) : ReleaseCheck {
        override val known: Release? get() = release
    }

    data class Failed(
        val lastCheckedAt: Long?,
        override val known: Release?,
    ) : ReleaseCheck
}

object ReleaseClient {
    private const val REPO = "hxreborn/amznkiller"
    private const val API = "https://api.github.com/repos/$REPO/releases"
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 30_000
    private const val HTTP_TIMEOUT = 408
    private const val HTTP_TOO_MANY = 429
    private const val HTTP_SERVER_ERRORS = 500
    private const val MILLIS = 1000
    private const val CHANGELOG_PAGE = 100

    fun fetchLatest(etag: String?): ApiResponse = request("$API/latest", etag)

    fun fetchAll(): ApiResponse = request("$API?per_page=$CHANGELOG_PAGE", null)

    private fun request(
        url: String,
        etag: String?,
    ): ApiResponse {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            etag?.let { connection.setRequestProperty("If-None-Match", it) }
            val code = connection.responseCode
            val stream =
                if (code < HttpURLConnection.HTTP_BAD_REQUEST) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
            val text = stream?.use { it.bufferedReader().readText() }.orEmpty()
            when {
                code in HttpURLConnection.HTTP_OK until HttpURLConnection.HTTP_MULT_CHOICE ||
                    code == HttpURLConnection.HTTP_NOT_MODIFIED -> {
                    ApiResponse.Ok(code, text, connection.getHeaderField("ETag"))
                }

                code == HttpURLConnection.HTTP_FORBIDDEN &&
                    connection.getHeaderField("X-RateLimit-Remaining") == "0" -> {
                    ApiResponse.Retryable(
                        "rate limited",
                        connection.getHeaderField("X-RateLimit-Reset")?.toLongOrNull()?.let {
                            (it - System.currentTimeMillis() / MILLIS).coerceAtLeast(0)
                        },
                    )
                }

                code == HTTP_TIMEOUT || code == HTTP_TOO_MANY || code >= HTTP_SERVER_ERRORS -> {
                    ApiResponse.Retryable(
                        "http $code",
                        connection.getHeaderField("Retry-After")?.toLongOrNull(),
                    )
                }

                else -> {
                    ApiResponse.Refused(
                        code,
                        runCatching { JSONObject(text).optString("message") }
                            .getOrNull()
                            .orEmpty()
                            .ifEmpty { "http $code" },
                    )
                }
            }
        } catch (e: IOException) {
            ApiResponse.Retryable(e.javaClass.simpleName + (e.message?.let { ": $it" } ?: ""), null)
        } finally {
            connection.disconnect()
        }
    }
}
