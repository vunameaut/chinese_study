package vhn.dev.study_chines.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("study_chinese_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_HSK = "last_hsk"
        private const val KEY_LAST_SESSION_ID = "last_session_id"
    }

    var lastHsk: Int
        get() = sharedPreferences.getInt(KEY_LAST_HSK, 1)
        set(value) = sharedPreferences.edit().putInt(KEY_LAST_HSK, value).apply()

    var lastSessionId: Long
        get() = sharedPreferences.getLong(KEY_LAST_SESSION_ID, -1L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_SESSION_ID, value).apply()
}