package vhn.dev.study_chines.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import vhn.dev.study_chines.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(private val context: Context) {

    companion object {
        private const val TAG = "AppUpdateManager"
        private const val GITHUB_OWNER = "vunameaut"
        private const val GITHUB_REPO = "chinese_study"
        private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val currentVersionName: String
        get() = BuildConfig.VERSION_NAME

    val currentVersionCode: Int
        get() = BuildConfig.VERSION_CODE

    /**
     * Kiểm tra GitHub Releases xem có phiên bản mới hơn không
     */
    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "StudyChineseApp/${currentVersionName}")
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext UpdateCheckResult.UpToDate
            }
            if (responseCode !in 200..299) {
                return@withContext UpdateCheckResult.Error("Lỗi kết nối GitHub (HTTP $responseCode)")
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val release = json.decodeFromString<GitHubRelease>(responseBody)

            val remoteVersion = release.tagName.removePrefix("v").trim()
            val localVersion = currentVersionName.removePrefix("v").trim()

            if (isNewerVersion(remoteVersion, localVersion)) {
                // Tìm file APK trong danh sách assets
                val apkAsset = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
                if (apkAsset != null) {
                    val updateInfo = UpdateInfo(
                        versionName = release.tagName,
                        title = release.name ?: "Phiên bản mới ${release.tagName}",
                        releaseNotes = release.body?.trim().takeUnless { it.isNullOrEmpty() }
                            ?: "Bản cập nhật mới có nhiều cải tiến và sửa lỗi.",
                        downloadUrl = apkAsset.browserDownloadUrl,
                        fileSize = apkAsset.size
                    )
                    return@withContext UpdateCheckResult.UpdateAvailable(updateInfo)
                }
            }

            UpdateCheckResult.UpToDate
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi kiểm tra cập nhật", e)
            UpdateCheckResult.Error(e.localizedMessage ?: "Không thể kiểm tra bản cập nhật")
        }
    }

    /**
     * So sánh 2 chuỗi phiên bản dạng x.y.z
     * Trả về true nếu remoteVersion > localVersion
     */
    fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        try {
            val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
            val localParts = localVersion.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, localParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val l = localParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
        } catch (_: Exception) {
            return remoteVersion != localVersion
        }
        return false
    }

    /**
     * Tải file APK với theo dõi tiến trình %
     */
    suspend fun downloadApk(
        downloadUrl: String,
        targetVersion: String,
        onProgress: (percent: Int, downloaded: Long, total: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val sanitizedVersion = targetVersion.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val cacheDir = File(context.cacheDir, "updates")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val apkFile = File(cacheDir, "chinese_study_$sanitizedVersion.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val url = URL(downloadUrl)
            var connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15000
            connection.readTimeout = 30000

            // Xử lý chuyển hướng (GitHub assets thường redirect sang AWS S3)
            var responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == 307 || responseCode == 308
            ) {
                val newUrl = connection.getHeaderField("Location")
                connection = URL(newUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
            }

            val contentLength = connection.contentLengthLong

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalDownloaded = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        if (contentLength > 0) {
                            val percent = ((totalDownloaded * 100) / contentLength).toInt()
                            onProgress(percent.coerceIn(0, 100), totalDownloaded, contentLength)
                        } else {
                            onProgress(-1, totalDownloaded, -1)
                        }
                    }
                    output.flush()
                }
            }

            Result.success(apkFile)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi tải APK", e)
            Result.failure(e)
        }
    }

    /**
     * Kiểm tra quyền cài đặt ứng dụng từ nguồn không xác định
     */
    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Mở màn hình cấp quyền cài đặt ứng dụng không rõ nguồn gốc
     */
    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Kích hoạt trình cài đặt hệ thống để cập nhật APK
     */
    fun installApk(apkFile: File): Result<Unit> {
        return try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi mở trình cài đặt APK", e)
            Result.failure(e)
        }
    }
}
