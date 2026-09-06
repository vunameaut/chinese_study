package vhn.dev.study_chines.ui.quiz

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import vhn.dev.study_chines.data.local.UserPreferences
import java.io.File

/**
 * Quản lý âm thanh hiệu ứng từ file (SoundPool) hỗ trợ điều chỉnh âm lượng 0 - 200% kiểu Discord.
 */
class SoundManager(context: Context, private val preferences: UserPreferences) {
    private val appContext = context.applicationContext
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<Int, Int>() // resId -> soundPoolId
    private val loadedCustomPaths = mutableMapOf<Int, String>() // resId -> lastLoadedPath
    private val customSoundIds = mutableMapOf<Int, Int>() // resId -> soundPoolId

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()
    }

    fun loadDefault(vararg resIds: Int) {
        for (resId in resIds) {
            if (!soundIds.containsKey(resId)) {
                soundIds[resId] = soundPool.load(appContext, resId, 1)
            }
        }
    }

    private fun loadCustom(path: String): Int? {
        val file = File(path)
        if (!file.exists()) return null
        return soundPool.load(file.absolutePath, 1)
    }

    fun play(resId: Int, volume: Float = 1f, forcePlay: Boolean = false) {
        if (!forcePlay) {
            if (!preferences.masterEnabled || preferences.masterVolume <= 0) return
        }

        val isFinish = (resId == vhn.dev.study_chines.R.raw.finish)
        val channelEnabled = if (isFinish) preferences.finishEnabled else preferences.sfxEnabled
        val channelVol = if (isFinish) preferences.finishVolume else preferences.sfxVolume

        if (!forcePlay && (!channelEnabled || channelVol <= 0)) return

        val masterScale = if (preferences.masterEnabled) preferences.masterVolume / 100f else 1f
        val channelScale = channelVol / 100f
        val finalVol = (volume * masterScale * channelScale).coerceIn(0.05f, 2.0f)

        val customPath = when (resId) {
            vhn.dev.study_chines.R.raw.correct -> preferences.customCorrectSoundPath
            vhn.dev.study_chines.R.raw.wrong -> preferences.customWrongSoundPath
            vhn.dev.study_chines.R.raw.finish -> preferences.customFinishSoundPath
            else -> null
        }

        if (customPath != null) {
            // If path changed since last load, reload it
            if (loadedCustomPaths[resId] != customPath) {
                val newId = loadCustom(customPath)
                if (newId != null) {
                    customSoundIds[resId] = newId
                    loadedCustomPaths[resId] = customPath
                }
            }
            
            val id = customSoundIds[resId]
            if (id != null) {
                soundPool.play(id, finalVol, finalVol, 1, 0, 1f)
                return
            }
        }

        // Fallback to default
        val id = soundIds[resId] ?: return
        soundPool.play(id, finalVol, finalVol, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
