package eu.hxreborn.amznkiller.update

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import eu.hxreborn.amznkiller.BuildConfig
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class Release(
    val versionName: String,
    val versionCode: Int,
    val publishedAt: String?,
    val url: String,
    val notes: List<Note>,
    val asset: Asset?,
) {
    data class Note(
        val kind: Kind,
        val section: String,
        val scope: String?,
        val text: String,
        val commitUrl: String?,
    ) {
        enum class Kind { NEW, FIXED, OTHER }
    }

    data class Asset(
        val name: String,
        val size: Long,
        val sha256: String?,
        val url: String,
    )

    val notesNew: List<String> get() = notesOf(Note.Kind.NEW)
    val notesFixed: List<String> get() = notesOf(Note.Kind.FIXED)
    val notesOther: List<String> get() = notesOf(Note.Kind.OTHER)

    private fun notesOf(kind: Note.Kind) = notes.filter { it.kind == kind }.map { it.text }

    val newer: Boolean get() = versionCode > versionCodeOf(BuildConfig.VERSION_NAME)

    companion object {
        private const val MAJOR = 10_000
        private const val MINOR = 100
        private val assetPattern = Regex("""^amznkiller-v.+-release\.apk$""")

        fun versionCodeOf(versionName: String): Int {
            val parts =
                versionName.removePrefix("v").split('.').map {
                    it.takeWhile(Char::isDigit).toIntOrNull() ?: 0
                }
            return parts.getOrElse(0) { 0 } * MAJOR + parts.getOrElse(1) { 0 } * MINOR +
                parts.getOrElse(2) { 0 }
        }

        private val bulletCleanups =
            listOf(
                Regex("""^\[`?[0-9a-f]{7,40}`?]\([^)]*\)\s*""") to "",
                Regex("""^\*\*[^*]+:\*\*\s*""") to "",
                Regex("""^\*\([^)]*\)\*\s*""") to "",
                Regex("""\s+by @\S+(\s+in\s+\[[^]]*]\([^)]*\))?\s*$""") to "",
                Regex("""(?i)(\s*,?\s*(closes\s+)?\(?\[[^]]*]\([^)]*\)\)?)+\s*$""") to "",
                Regex("""\[([^]]*)]\([^)]*\)""") to "$1",
                Regex("""\*\*|`""") to "",
            )

        private fun cleanBullet(text: String): String =
            bulletCleanups
                .fold(text) { acc, (regex, replacement) -> regex.replace(acc, replacement) }
                .trim()

        private val bulletScope =
            Regex("""^(?:\[`?[0-9a-f]{7,40}`?]\([^)]*\)\s*)?\*\(([^)]+)\)\*""")
        private val bulletCommit =
            Regex("""^\[`?[0-9a-f]{7,40}`?]\((https://[^)]+/commit/[0-9a-f]{7,40})\)""")

        fun parseNotes(body: String): List<Note> {
            val notes = mutableListOf<Note>()
            var section: Pair<Note.Kind, String>? = null
            body.lines().forEach { line ->
                val heading = Regex("""^###\s+(.+?)\s*$""").find(line)
                if (heading != null) {
                    val title = heading.groupValues[1]
                    val kind =
                        when (title) {
                            "Features", "Added" -> Note.Kind.NEW
                            "Bug Fixes", "Fixed" -> Note.Kind.FIXED
                            else -> Note.Kind.OTHER
                        }
                    section = kind to title
                    return@forEach
                }
                if (Regex("""^#{1,2}\s""").containsMatchIn(line)) section = null
                val (kind, title) = section ?: return@forEach
                val bullet =
                    Regex("""^\s*[*-]\s+(.+)$""").find(line)?.groupValues?.get(1) ?: return@forEach
                notes +=
                    Note(
                        kind = kind,
                        section = title,
                        scope = bulletScope.find(bullet)?.groupValues?.get(1),
                        text = cleanBullet(bullet),
                        commitUrl = bulletCommit.find(bullet)?.groupValues?.get(1),
                    )
            }
            return notes
        }

        fun fromGitHub(json: JSONObject): Release {
            val tag = json.getString("tag_name")
            val versionName = tag.removePrefix("v")
            val asset =
                json
                    .optJSONArray("assets")
                    ?.let { array -> List(array.length()) { array.getJSONObject(it) } }
                    ?.firstOrNull { assetPattern.containsMatchIn(it.getString("name")) }
            return Release(
                versionName = versionName,
                versionCode = versionCodeOf(versionName),
                publishedAt = json.optStringOrNull("published_at"),
                url = json.optString("html_url"),
                notes = parseNotes(json.optString("body")),
                asset =
                    asset?.let {
                        Asset(
                            name = it.getString("name"),
                            size = it.getLong("size"),
                            sha256 = it.optStringOrNull("digest")?.removePrefix("sha256:"),
                            url = it.getString("browser_download_url"),
                        )
                    },
            )
        }

        fun parse(body: String): Release = fromGitHub(JSONObject(body))
    }
}

internal fun JSONObject.optStringOrNull(key: String): String? =
    if (isNull(key)) {
        null
    } else {
        optString(key).ifEmpty {
            null
        }
    }

internal fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

sealed interface InstallStep {
    data object Idle : InstallStep

    data object NeedsPermission : InstallStep

    data class Downloading(
        val bytes: Long,
        val total: Long,
        val partialHash: String,
    ) : InstallStep

    data class DownloadFailed(
        val bytes: Long,
        val reason: String,
    ) : InstallStep

    data class Verifying(
        val expected: String,
        val actual: String,
        val installedSigner: String?,
        val fileSigner: String?,
    ) : InstallStep

    data class Rejected(
        val cause: Cause,
        val expected: String?,
        val actual: String?,
        val checksum: String? = null,
    ) : InstallStep {
        val releaseKey: Boolean get() =
            cause == Cause.SIGNER &&
                actual == BuildConfig.RELEASE_SIGNER

        enum class Cause { CHECKSUM, SIGNER, PACKAGE, NO_CHECKSUM }
    }

    data object Installing : InstallStep

    data class InstallFailed(
        val message: String,
    ) : InstallStep
}

class Updater(
    private val context: Context,
) {
    private val dir = File(context.cacheDir, "update")

    fun canRequestInstalls(): Boolean = context.packageManager.canRequestPackageInstalls()

    fun download(
        asset: Release.Asset,
        cancelled: () -> Boolean,
        onProgress: (InstallStep.Downloading) -> Unit,
    ): Pair<File, String> {
        dir.listFiles()?.forEach(File::delete)
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("cannot create ${dir.path}")
        val file = File(dir, DOWNLOAD_NAME)
        val digest = MessageDigest.getInstance("SHA-256")
        val connection = URL(asset.url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("http ${connection.responseCode}")
            }
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: asset.size
            var bytes = 0L
            var lastReport = 0L
            connection.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(BUFFER)
                    while (true) {
                        if (cancelled()) throw IOException("cancelled")
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        bytes += read
                        if (bytes - lastReport >= REPORT_EVERY || bytes == total) {
                            lastReport = bytes
                            val partial = (digest.clone() as MessageDigest).digest().toHex()
                            onProgress(InstallStep.Downloading(bytes, total, partial))
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
        return file to digest.digest().toHex()
    }

    @Suppress("DEPRECATION")
    fun readApkPackageInfo(file: File): PackageInfo? {
        val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        return context.packageManager.getPackageArchiveInfo(file.path, flags)
    }

    fun installedSigners(): Set<String> =
        signers(
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            ),
        )

    @Suppress("DEPRECATION")
    fun signers(info: PackageInfo?): Set<String> {
        info ?: return emptySet()
        val certs = info.signingInfo?.apkContentsSigners ?: info.signatures ?: return emptySet()
        return certs
            .map {
                MessageDigest
                    .getInstance(
                        "SHA-256",
                    ).digest(it.toByteArray())
                    .toHex()
            }.toSet()
    }

    fun install(
        file: File,
        onResult: (InstallStep) -> Unit,
    ) {
        val installer = context.packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(context.packageName)
                setSize(file.length())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
            }
        val sessionId = installer.createSession(params)
        val action = "${context.packageName}.INSTALL_RESULT.$sessionId"
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    ctx: Context,
                    intent: Intent,
                ) {
                    when (
                        val status =
                            intent.getIntExtra(
                                PackageInstaller.EXTRA_STATUS,
                                PackageInstaller.STATUS_FAILURE,
                            )
                    ) {
                        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                            IntentCompat
                                .getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                                ?.let {
                                    ctx.startActivity(
                                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }
                        }

                        PackageInstaller.STATUS_SUCCESS -> {
                            ctx.unregisterReceiver(this)
                        }

                        else -> {
                            ctx.unregisterReceiver(this)
                            val message =
                                intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                                    ?: "status $status"
                            onResult(
                                if (status == PackageInstaller.STATUS_FAILURE_ABORTED) {
                                    InstallStep.Idle
                                } else {
                                    InstallStep.InstallFailed(message)
                                },
                            )
                        }
                    }
                }
            }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(action),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        try {
            installer.openSession(sessionId).use { session ->
                file.inputStream().use { input ->
                    session.openWrite("base.apk", 0, file.length()).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }
                val statusIntent =
                    PendingIntent.getBroadcast(
                        context,
                        sessionId,
                        Intent(action).setPackage(context.packageName),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
                    )
                session.commit(statusIntent.intentSender)
            }
        } catch (e: Exception) {
            runCatching { installer.abandonSession(sessionId) }
            runCatching { context.unregisterReceiver(receiver) }
            throw e
        }
    }

    companion object {
        private const val DOWNLOAD_NAME = "update.apk"
        private const val TIMEOUT_MS = 30_000
        private const val BUFFER = 64 * 1024
        private const val REPORT_EVERY = 256 * 1024L
    }
}
