package eu.hxreborn.amznkiller.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.update.ApiResponse
import eu.hxreborn.amznkiller.update.InstallStep
import eu.hxreborn.amznkiller.update.Release
import eu.hxreborn.amznkiller.update.ReleaseCheck
import eu.hxreborn.amznkiller.update.ReleaseClient
import eu.hxreborn.amznkiller.update.Updater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import kotlin.time.Duration.Companion.seconds

@Immutable
data class UpdateUiState(
    val release: ReleaseCheck,
    val autoCheckUpdates: Boolean,
    val postponedVersion: Int,
    val install: InstallStep,
) {
    val available: Release? get() = release.known?.takeIf { it.newer }
    val showUpdateCard: Boolean get() = available?.let { it.versionCode > postponedVersion } == true
}

class UpdateViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("updates", Context.MODE_PRIVATE)
    private val updater = Updater(application)

    private val releaseCheck = MutableStateFlow(cachedRelease())
    private val installStep = MutableStateFlow<InstallStep>(InstallStep.Idle)
    private val autoCheck = MutableStateFlow(prefs.getBoolean(AUTO_CHECK_UPDATES, true))
    private val postponed = MutableStateFlow(prefs.getInt(POSTPONED_VERSION, 0))

    val state: StateFlow<UpdateUiState> =
        combine(releaseCheck, autoCheck, postponed, installStep, ::UpdateUiState)
            .stateIn(
                viewModelScope,
                WhileSubscribed(5.seconds.inWholeMilliseconds),
                UpdateUiState(releaseCheck.value, autoCheck.value, postponed.value, installStep.value),
            )

    private val _updatedNotice =
        MutableStateFlow(prefs.getInt(LAST_RUN_VERSION, 0) in 1 until BuildConfig.VERSION_CODE)
    val updatedNotice: StateFlow<Boolean> = _updatedNotice.asStateFlow()

    private var installJob: Job? = null
    private var verifiedApk: File? = null
    private var verifiedVersion: Int? = null

    init {
        prefs.edit { putInt(LAST_RUN_VERSION, BuildConfig.VERSION_CODE) }
    }

    private fun cachedRelease(): ReleaseCheck {
        val body = prefs.getString(RELEASE_BODY, null) ?: return ReleaseCheck.Idle
        val release = runCatching { Release.parse(body) }.getOrNull()
        return ReleaseCheck.Done(release, prefs.getLong(RELEASE_CHECKED_AT, 0))
    }

    fun consumeUpdatedNotice() {
        _updatedNotice.value = false
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        prefs.edit { putBoolean(AUTO_CHECK_UPDATES, enabled) }
        autoCheck.value = enabled
    }

    fun postponeUpdate(versionCode: Int) {
        prefs.edit { putInt(POSTPONED_VERSION, versionCode) }
        postponed.value = versionCode
    }

    fun checkRelease(force: Boolean) {
        if (!force && !autoCheck.value) return
        val last = prefs.getLong(RELEASE_CHECKED_AT, 0)
        if (!force && System.currentTimeMillis() - last < CHECK_INTERVAL_MS) return
        if (!force && System.currentTimeMillis() < prefs.getLong(RELEASE_RETRY_AT, 0)) return
        if (releaseCheck.value is ReleaseCheck.Checking) return
        val previous = releaseCheck.value
        releaseCheck.value = ReleaseCheck.Checking(previous.known)
        viewModelScope.launch(Dispatchers.IO) {
            releaseCheck.value =
                when (val reply = fetchRelease()) {
                    is ApiResponse.Ok -> {
                        val release = runCatching { Release.parse(reply.body) }.getOrNull()
                        val now = System.currentTimeMillis()
                        prefs.edit {
                            putString(RELEASE_BODY, reply.body)
                            reply.etag?.let { putString(GITHUB_ETAG, it) }
                            putLong(RELEASE_CHECKED_AT, now)
                            remove(RELEASE_RETRY_AT)
                        }
                        ReleaseCheck.Done(release, now)
                    }

                    else -> {
                        Log.w(TAG, "release check failed reason=$reply")
                        (reply as? ApiResponse.Retryable)?.afterSeconds?.let { seconds ->
                            prefs.edit { putLong(RELEASE_RETRY_AT, System.currentTimeMillis() + seconds * MILLIS) }
                        }
                        if (force) {
                            ReleaseCheck.Failed(last.takeIf { it > 0 }, previous.known)
                        } else {
                            previous.takeUnless { it is ReleaseCheck.Checking } ?: ReleaseCheck.Idle
                        }
                    }
                }
        }
    }

    private fun fetchRelease(): ApiResponse {
        val cached = prefs.getString(RELEASE_BODY, null)
        val etag = prefs.getString(GITHUB_ETAG, null)?.takeIf { cached != null }
        return when (val gitHub = ReleaseClient.fetchLatest(etag)) {
            is ApiResponse.Ok if gitHub.code == HttpURLConnection.HTTP_NOT_MODIFIED && cached != null -> {
                ApiResponse.Ok(gitHub.code, cached)
            }

            is ApiResponse.Ok -> {
                runCatching {
                    Release.parse(gitHub.body)
                    gitHub
                }.getOrElse { ApiResponse.Refused(gitHub.code, it.message ?: "bad release json") }
            }

            else -> {
                gitHub
            }
        }
    }

    fun startInstall(release: Release) {
        val asset = release.asset ?: return
        installJob?.cancel()
        installJob =
            viewModelScope.launch(Dispatchers.IO) {
                val job = coroutineContext.job
                installStep.value = InstallStep.Downloading(0, asset.size, "")
                val downloaded =
                    runCatching {
                        updater.download(asset, { !job.isActive }) {
                            if (job.isActive) installStep.value = it
                        }
                    }.getOrElse {
                        if (!job.isActive) return@launch
                        val bytes = (installStep.value as? InstallStep.Downloading)?.bytes ?: 0
                        installStep.value = InstallStep.DownloadFailed(bytes, it.message ?: it.javaClass.simpleName)
                        return@launch
                    }
                val (file, hash) = downloaded
                val rejection =
                    verify(file, hash, asset.sha256) {
                        if (job.isActive) installStep.value = it
                    }
                if (!job.isActive) return@launch
                if (rejection != null) {
                    if (rejection.releaseKey) {
                        verifiedApk = file
                        verifiedVersion = release.versionCode
                    } else {
                        file.delete()
                    }
                    installStep.value = rejection
                    return@launch
                }
                verifiedApk = file
                verifiedVersion = release.versionCode
                installVerified(file)
            }
    }

    private fun verify(
        file: File,
        hash: String,
        expected: String?,
        onStep: (InstallStep) -> Unit,
    ): InstallStep.Rejected? {
        expected ?: return InstallStep.Rejected(InstallStep.Rejected.Cause.NO_CHECKSUM, null, hash)
        val installed = updater.installedSigners()
        onStep(InstallStep.Verifying(expected, hash, installed.firstOrNull(), null))
        if (!expected.equals(hash, ignoreCase = true)) {
            return InstallStep.Rejected(InstallStep.Rejected.Cause.CHECKSUM, expected, hash)
        }
        val archive = updater.readApkPackageInfo(file)
        val fileSigners = updater.signers(archive)
        onStep(InstallStep.Verifying(expected, hash, installed.firstOrNull(), fileSigners.firstOrNull()))
        val packageName = getApplication<Application>().packageName
        return when {
            archive?.packageName != packageName -> {
                InstallStep.Rejected(InstallStep.Rejected.Cause.PACKAGE, packageName, archive?.packageName)
            }

            fileSigners.isEmpty() || fileSigners != installed -> {
                InstallStep.Rejected(
                    InstallStep.Rejected.Cause.SIGNER,
                    installed.firstOrNull(),
                    fileSigners.firstOrNull(),
                    checksum = hash,
                )
            }

            else -> {
                null
            }
        }
    }

    private fun installVerified(file: File) {
        if (!updater.canRequestInstalls()) {
            installStep.value = InstallStep.NeedsPermission
            return
        }
        installStep.value = InstallStep.Installing
        runCatching { updater.install(file) { installStep.value = it } }
            .onFailure { installStep.value = InstallStep.InstallFailed(it.message ?: it.javaClass.simpleName) }
    }

    fun installAnyway(release: Release) {
        val file = verifiedApk?.takeIf { verifiedVersion == release.versionCode } ?: return
        viewModelScope.launch(Dispatchers.IO) { installVerified(file) }
    }

    fun resumeAfterInstallPermission() {
        val file = verifiedApk ?: return
        if (installStep.value is InstallStep.NeedsPermission && updater.canRequestInstalls()) {
            viewModelScope.launch(Dispatchers.IO) { installVerified(file) }
        }
    }

    fun cancelInstall() {
        installJob?.cancel()
        installStep.value = InstallStep.Idle
    }

    fun resetInstall() {
        if (installStep.value !is InstallStep.Downloading && installStep.value !is InstallStep.Installing) {
            installStep.value = InstallStep.Idle
        }
    }

    companion object {
        private const val TAG = "AmznKiller"
        private const val AUTO_CHECK_UPDATES = "autoCheckUpdates"
        private const val RELEASE_BODY = "releaseBody"
        private const val GITHUB_ETAG = "githubEtag"
        private const val RELEASE_CHECKED_AT = "releaseCheckedAt"
        private const val RELEASE_RETRY_AT = "releaseRetryAt"
        private const val POSTPONED_VERSION = "postponedVersion"
        private const val LAST_RUN_VERSION = "lastRunVersion"
        private const val CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L
        private const val MILLIS = 1000L
    }
}
