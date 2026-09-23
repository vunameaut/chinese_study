package vhn.dev.study_chines

import android.content.Context
import android.os.Build
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

object AIOTracker {
    private const val SERVER_URL = "https://aio-analytics-server.wasmer.app"
    private var projectId = "study_chines"
    private var appName = "Study Chinese Android App"
    private val sessionId = "and_" + UUID.randomUUID().toString().replace("-", "").take(12)
    private val executor = Executors.newSingleThreadExecutor()
    private var isInitialized = false

    fun init(context: Context, customProjectId: String = "study_chines", customAppName: String = "Study Chinese Android App") {
        projectId = customProjectId
        appName = customAppName
        isInitialized = true
        trackScreen("AppLaunch")
    }

    fun trackScreen(screenName: String) {
        sendAsync(
            eventType = "screen_view",
            eventName = "Screen: $screenName",
            pathOrScreen = screenName
        )
    }

    fun trackEvent(eventName: String, properties: Map<String, Any>? = null) {
        sendAsync(
            eventType = "custom",
            eventName = eventName,
            properties = properties
        )
    }

    fun trackError(error: Throwable, contextInfo: String = "") {
        sendAsync(
            eventType = "error",
            eventName = "Android Exception",
            pathOrScreen = contextInfo,
            error = mapOf(
                "message" to (error.message ?: "Unknown Android Error"),
                "stack" to error.stackTraceToString()
            )
        )
    }

    private fun sendAsync(
        eventType: String,
        eventName: String,
        pathOrScreen: String = "/",
        properties: Map<String, Any>? = null,
        error: Map<String, String>? = null
    ) {
        executor.execute {
            try {
                val url = URL("$SERVER_URL/api/v1/track")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("X-Platform", "mobile_android")
                conn.setRequestProperty("X-Client-Sdk", "android-kotlin")
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.doOutput = true

                val payload = JSONObject().apply {
                    put("projectId", projectId)
                    put("projectName", appName)
                    put("sessionId", sessionId)
                    put("platform", "mobile_android")
                    put("deviceType", "mobile")
                    put("os", "Android " + Build.VERSION.RELEASE)
                    put("osVersion", Build.VERSION.SDK_INT.toString())
                    put("browser", "${Build.MANUFACTURER} ${Build.MODEL}")
                    put("eventType", eventType)
                    put("eventName", eventName)
                    put("path", pathOrScreen)
                    if (properties != null) {
                        put("properties", JSONObject(properties))
                    }
                    if (error != null) {
                        put("error", JSONObject(error))
                    }
                }

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }
                conn.responseCode // trigger connection
                conn.disconnect()
            } catch (_: Exception) {
                // Ignore silently so it never interrupts the app
            }
        }
    }
}
