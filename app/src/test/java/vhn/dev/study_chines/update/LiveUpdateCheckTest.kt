package vhn.dev.study_chines.update

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.json.Json

class LiveUpdateCheckTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testLiveGitHubReleaseApi_detectsNewRelease() = runBlocking {
        val url = URL("https://api.github.com/repos/vunameaut/chinese_study/releases/latest")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", "StudyChineseAppTest/2.0")
            connectTimeout = 10000
            readTimeout = 10000
        }

        assertEquals(200, connection.responseCode)
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val release = json.decodeFromString<GitHubRelease>(body)

        println("Live Release Tag: ${release.tagName}")
        assertEquals("v2.1", release.tagName)

        val apkAsset = release.assets.find { it.name.endsWith(".apk") }
        assertTrue("File APK phai ton tai tren Release", apkAsset != null)
        println("File APK Name: ${apkAsset?.name}")
        println("File APK Size: ${apkAsset?.size} bytes")
        println("File APK URL: ${apkAsset?.browserDownloadUrl}")
    }
}
