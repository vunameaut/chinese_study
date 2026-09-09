package vhn.dev.study_chines.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("body")
    val body: String? = null,
    @SerialName("html_url")
    val htmlUrl: String? = null,
    @SerialName("assets")
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("size")
    val size: Long = 0L,
    @SerialName("content_type")
    val contentType: String? = null
)

data class UpdateInfo(
    val versionName: String,
    val title: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long
)

sealed class UpdateCheckResult {
    data object UpToDate : UpdateCheckResult()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

sealed class AppUpdateState {
    data object Idle : AppUpdateState()
    data object Checking : AppUpdateState()
    data class Available(val info: UpdateInfo) : AppUpdateState()
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : AppUpdateState()
    data class ReadyToInstall(val info: UpdateInfo, val apkFile: File) : AppUpdateState()
    data class Error(val message: String) : AppUpdateState()
}
