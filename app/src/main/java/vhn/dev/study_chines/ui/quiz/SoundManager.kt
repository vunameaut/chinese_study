package vhn.dev.study_chines.ui.quiz

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Quản lý âm thanh hiệu ứng từ file (SoundPool).
 *
 * CÁCH DÙNG:
 * 1. Tạo thư mục: app/src/main/res/raw/
 * 2. Bỏ file âm thanh vào đó, ví dụ: correct.ogg, wrong.ogg, finish.ogg
 *    (định dạng khuyên dùng: .ogg hoặc .wav; .mp3 cũng được từ API 21+)
 * 3. Trong QuizScreen, thay ToneGenerator bằng:
 *      val context = LocalContext.current
 *      val soundManager = remember { SoundManager(context, R.raw.correct, R.raw.wrong, R.raw.finish) }
 *      DisposableEffect(Unit) { onDispose { soundManager.release() } }
 * 4. Phát: soundManager.play(R.raw.correct)
 */
class SoundManager(context: Context, vararg resIds: Int) {
    private val appContext = context.applicationContext
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<Int, Int>()

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
        for (resId in resIds) {
            soundIds[resId] = soundPool.load(appContext, resId, 1)
        }
    }

    fun play(resId: Int, volume: Float = 1f) {
        val id = soundIds[resId] ?: return
        soundPool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
