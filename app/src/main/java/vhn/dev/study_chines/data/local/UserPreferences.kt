package vhn.dev.study_chines.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("study_chinese_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_HSK = "last_hsk"
        private const val KEY_LAST_SESSION_ID = "last_session_id"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_CUSTOM_CORRECT_SOUND = "custom_correct_sound"
        private const val KEY_CUSTOM_WRONG_SOUND = "custom_wrong_sound"
        private const val KEY_CUSTOM_FINISH_SOUND = "custom_finish_sound"
    }

    var lastHsk: Int
        get() = sharedPreferences.getInt(KEY_LAST_HSK, 1)
        set(value) = sharedPreferences.edit().putInt(KEY_LAST_HSK, value).apply()

    var lastSessionId: Long
        get() = sharedPreferences.getLong(KEY_LAST_SESSION_ID, -1L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_SESSION_ID, value).apply()

    var isSoundEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var customCorrectSoundPath: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_CORRECT_SOUND, null)
        set(value) = sharedPreferences.edit().putString(KEY_CUSTOM_CORRECT_SOUND, value).apply()

    var customWrongSoundPath: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_WRONG_SOUND, null)
        set(value) = sharedPreferences.edit().putString(KEY_CUSTOM_WRONG_SOUND, value).apply()

    var customFinishSoundPath: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_FINISH_SOUND, null)
        set(value) = sharedPreferences.edit().putString(KEY_CUSTOM_FINISH_SOUND, value).apply()
}