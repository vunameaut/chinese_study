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

        // Granular Audio Channels (0 - 200% Discord style)
        private const val KEY_MASTER_ENABLED = "master_enabled"
        private const val KEY_MASTER_VOLUME = "master_volume"
        private const val KEY_SPEECH_ENABLED = "speech_enabled"
        private const val KEY_SPEECH_VOLUME = "speech_volume"
        private const val KEY_SPEECH_AUTOPLAY = "speech_autoplay"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_VOICE = "speech_voice"
        private const val KEY_SFX_ENABLED = "sfx_enabled"
        private const val KEY_SFX_VOLUME = "sfx_volume"
        private const val KEY_FINISH_ENABLED = "finish_enabled"
        private const val KEY_FINISH_VOLUME = "finish_volume"
    }

    var lastHsk: Int
        get() = sharedPreferences.getInt(KEY_LAST_HSK, 1)
        set(value) = sharedPreferences.edit().putInt(KEY_LAST_HSK, value).apply()

    var lastSessionId: Long
        get() = sharedPreferences.getLong(KEY_LAST_SESSION_ID, -1L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_SESSION_ID, value).apply()

    // Backward compatibility: isSoundEnabled bridges to masterEnabled
    var isSoundEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_MASTER_ENABLED, sharedPreferences.getBoolean(KEY_SOUND_ENABLED, true))
        set(value) {
            sharedPreferences.edit()
                .putBoolean(KEY_SOUND_ENABLED, value)
                .putBoolean(KEY_MASTER_ENABLED, value)
                .apply()
        }

    var masterEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_MASTER_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_MASTER_ENABLED, value).apply()

    var masterVolume: Int
        get() = sharedPreferences.getInt(KEY_MASTER_VOLUME, 100)
        set(value) = sharedPreferences.edit().putInt(KEY_MASTER_VOLUME, value).apply()

    var speechEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_SPEECH_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SPEECH_ENABLED, value).apply()

    var speechVolume: Int
        get() = sharedPreferences.getInt(KEY_SPEECH_VOLUME, 100)
        set(value) = sharedPreferences.edit().putInt(KEY_SPEECH_VOLUME, value).apply()

    var speechAutoPlay: Boolean
        get() = sharedPreferences.getBoolean(KEY_SPEECH_AUTOPLAY, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SPEECH_AUTOPLAY, value).apply()

    var speechRate: Float
        get() = sharedPreferences.getFloat(KEY_SPEECH_RATE, 0.85f)
        set(value) = sharedPreferences.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    var speechVoice: String
        get() = sharedPreferences.getString(KEY_SPEECH_VOICE, "google-online") ?: "google-online"
        set(value) = sharedPreferences.edit().putString(KEY_SPEECH_VOICE, value).apply()

    var sfxEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_SFX_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SFX_ENABLED, value).apply()

    var sfxVolume: Int
        get() = sharedPreferences.getInt(KEY_SFX_VOLUME, 100)
        set(value) = sharedPreferences.edit().putInt(KEY_SFX_VOLUME, value).apply()

    var finishEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_FINISH_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_FINISH_ENABLED, value).apply()

    var finishVolume: Int
        get() = sharedPreferences.getInt(KEY_FINISH_VOLUME, 100)
        set(value) = sharedPreferences.edit().putInt(KEY_FINISH_VOLUME, value).apply()

    var customCorrectSoundPath: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_CORRECT_SOUND, null)
        set(value) = sharedPreferences.edit().putString(KEY_CUSTOM_CORRECT_SOUND, value).apply()

    var customWrongSoundPath: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_WRONG_SOUND, null)
        set(value) = sharedPreferences.edit().putString(KEY_CUSTOM_WRONG_SOUND, value).apply()

    var customFinishSoundPath: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_FINISH_SOUND, null)
        set(value) = sharedPreferences.edit().putString(KEY_CUSTOM_FINISH_SOUND, value).apply()

    fun resetAudioDefaults() {
        sharedPreferences.edit()
            .putBoolean(KEY_MASTER_ENABLED, true)
            .putInt(KEY_MASTER_VOLUME, 100)
            .putBoolean(KEY_SPEECH_ENABLED, true)
            .putInt(KEY_SPEECH_VOLUME, 100)
            .putBoolean(KEY_SPEECH_AUTOPLAY, true)
            .putFloat(KEY_SPEECH_RATE, 0.85f)
            .putString(KEY_SPEECH_VOICE, "google-online")
            .putBoolean(KEY_SFX_ENABLED, true)
            .putInt(KEY_SFX_VOLUME, 100)
            .putBoolean(KEY_FINISH_ENABLED, true)
            .putInt(KEY_FINISH_VOLUME, 100)
            .apply()
    }
}
