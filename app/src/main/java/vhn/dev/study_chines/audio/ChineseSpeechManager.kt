package vhn.dev.study_chines.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vhn.dev.study_chines.data.local.UserPreferences
import java.net.URLEncoder
import java.util.*

data class VoiceOption(
    val id: String,
    val displayName: String,
    val description: String = "",
    val badge: String = "",
    val pitch: Float = 1.0f,
    val rateMultiplier: Float = 1.0f,
    val isOnline: Boolean = false
)

/**
 * Quản lý phát âm tiếng Trung kết hợp MediaPlayer (Online Studio + Hardware Pitch Shift) 
 * và TextToSpeech (Thiết bị), với cơ chế DUAL-FALLBACK đảm bảo không bao giờ bị câm tiếng.
 */
class ChineseSpeechManager(
    private val context: Context,
    private val preferences: UserPreferences
) : TextToSpeech.OnInitListener {

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var isSpeaking: Boolean = false
        private set

    // Danh sách 9 giọng đọc tuyển chọn có phong cách âm sắc và cao độ (pitch/speed) khác biệt hoàn toàn
    private val curatedVoices = listOf(
        VoiceOption(
            id = "google-online",
            displayName = "Google Tiêu Chuẩn (Nữ)",
            description = "Giọng phổ thông Bắc Kinh, phát âm to, tròn vành rõ chữ",
            badge = "Online Studio",
            pitch = 1.0f,
            rateMultiplier = 1.0f,
            isOnline = true
        ),
        VoiceOption(
            id = "baidu-online",
            displayName = "Baidu Bản Xứ (Nữ)",
            description = "Ngữ điệu tự nhiên bản xứ, độ mềm mại truyền cảm cao",
            badge = "Online Studio",
            pitch = 1.0f,
            rateMultiplier = 1.0f,
            isOnline = true
        ),
        VoiceOption(
            id = "youdao-online",
            displayName = "Youdao Từ Điển (Sư phạm)",
            description = "Chuyên dùng cho từ điển và giáo trình chuẩn, dứt khoát",
            badge = "Online Studio",
            pitch = 1.0f,
            rateMultiplier = 1.0f,
            isOnline = true
        ),
        VoiceOption(
            id = "google-tw",
            displayName = "Google Đài Loan (Nữ Phồn Thể)",
            description = "Thanh điệu êm ái, đặc trưng phát âm miền Nam và Đài Loan",
            badge = "Online Studio",
            pitch = 1.0f,
            rateMultiplier = 1.0f,
            isOnline = true
        ),
        VoiceOption(
            id = "male-deep",
            displayName = "Nam Trầm Ấm (Trưởng thành)",
            description = "Tông giọng nam dày, ấm, đĩnh đạc và uy lực",
            badge = "Giọng Nam",
            pitch = 0.76f,
            rateMultiplier = 0.95f,
            isOnline = true
        ),
        VoiceOption(
            id = "male-energetic",
            displayName = "Nam Trẻ Trung (Năng động)",
            description = "Tông nam thanh niên tươi sáng, phát âm gọn gàng dứt khoát",
            badge = "Giọng Nam",
            pitch = 0.88f,
            rateMultiplier = 1.05f,
            isOnline = true
        ),
        VoiceOption(
            id = "female-gentle",
            displayName = "Nữ Dịu Dàng (Cô giáo)",
            description = "Âm sắc truyền cảm, nhịp điệu từ tốn chuẩn mực sư phạm",
            badge = "Giọng Nữ",
            pitch = 1.12f,
            rateMultiplier = 0.92f,
            isOnline = true
        ),
        VoiceOption(
            id = "female-cute",
            displayName = "Nữ Hoạt Bát (Trong trẻo)",
            description = "Tông cao sinh động, tươi vui, gần gũi giao tiếp đời sống",
            badge = "Giọng Nữ",
            pitch = 1.30f,
            rateMultiplier = 1.00f,
            isOnline = true
        ),
        VoiceOption(
            id = "slow-study",
            displayName = "Luyện Âm Chậm (Rõ thanh điệu)",
            description = "Nhịp chậm kéo dài từng âm tiết, chuyên sâu luyện nghe Pinyin",
            badge = "Học Tập",
            pitch = 0.98f,
            rateMultiplier = 0.72f,
            isOnline = true
        )
    )

    private val _availableVoices = MutableStateFlow<List<VoiceOption>>(curatedVoices)
    val availableVoices: StateFlow<List<VoiceOption>> = _availableVoices.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
                if (!isTtsReady) {
                    val r2 = tts?.setLanguage(Locale.CHINESE)
                    isTtsReady = (r2 != TextToSpeech.LANG_MISSING_DATA && r2 != TextToSpeech.LANG_NOT_SUPPORTED)
                }
            } catch (e: Exception) {
                isTtsReady = false
            }
            refreshVoiceList()
        }
    }

    private fun refreshVoiceList() {
        val list = ArrayList(curatedVoices)
        if (isTtsReady && tts != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val sysVoices = tts?.voices?.filter { voice ->
                    val lang = voice.locale?.language?.lowercase() ?: ""
                    val tag = voice.locale?.toLanguageTag()?.lowercase() ?: ""
                    val isZh = lang == "zh" || lang == "cmn" || tag.startsWith("zh")
                    
                    // CHỈ lấy các giọng ĐÃ ĐƯỢC TẢI trên máy (tránh giọng thiếu data gây câm tiếng)
                    val features = voice.features ?: emptySet()
                    val isInstalled = !features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                    isZh && isInstalled
                } ?: emptyList()

                val seenKeys = mutableSetOf<String>()
                var counter = 1
                sysVoices.forEach { voice ->
                    val rawName = voice.name
                    val lowName = rawName.lowercase()
                    val key = rawName.substringAfterLast(":").trim()

                    if (!seenKeys.contains(key)) {
                        seenKeys.add(key)
                        val isNetwork = lowName.contains("network")
                        val badge = if (isNetwork) "Máy (HD Mạng)" else "Máy (Offline)"

                        val genderTag = when {
                            lowName.contains("male") || lowName.contains("-m-") || lowName.contains("#male") -> "Nam"
                            lowName.contains("female") || lowName.contains("-f-") || lowName.contains("#female") -> "Nữ"
                            else -> "Chuẩn"
                        }

                        val region = when {
                            voice.locale?.country?.equals("TW", ignoreCase = true) == true || lowName.contains("tw") -> "Đài Loan"
                            voice.locale?.country?.equals("HK", ignoreCase = true) == true || lowName.contains("hk") -> "Hồng Kông"
                            else -> "Bắc Kinh"
                        }

                        val displayName = "Giọng Thiết Bị $genderTag #$counter ($region)"
                        val desc = "Giọng đọc sẵn có trên máy [" + key.take(14) + "]"
                        counter++

                        list.add(
                            VoiceOption(
                                id = voice.name,
                                displayName = displayName,
                                description = desc,
                                badge = badge,
                                pitch = if (genderTag == "Nam") 0.85f else 1.0f,
                                rateMultiplier = 1.0f,
                                isOnline = isNetwork
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _availableVoices.value = list
    }

    fun getVoiceOptions(): List<VoiceOption> {
        return _availableVoices.value
    }

    /**
     * Phát âm từ vựng tiếng Trung với cam kết không bao giờ bị im lặng
     */
    fun speak(
        text: String,
        forcePlay: Boolean = false,
        voiceOverride: String? = null,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        if (text.isBlank()) return

        // Kiểm tra cài đặt âm lượng nếu không phải forcePlay (test)
        if (!forcePlay) {
            if (!preferences.masterEnabled || !preferences.speechEnabled) return
            if (preferences.masterVolume <= 0 || preferences.speechVolume <= 0) return
        }

        stop()
        this@ChineseSpeechManager.isSpeaking = true
        onStart?.invoke()

        val rate = preferences.speechRate
        val voiceChoice = voiceOverride ?: preferences.speechVoice

        val persona = curatedVoices.find { it.id == voiceChoice }
        val targetPitch = persona?.pitch ?: 1.0f
        val targetRate = (rate * (persona?.rateMultiplier ?: 1.0f)).coerceIn(0.5f, 2.0f)

        // Phân bổ luồng phát:
        // A. Nếu là giọng hệ thống của thiết bị (voiceChoice không thuộc curatedVoices)
        if (persona == null && voiceChoice.isNotBlank()) {
            speakViaTts(
                text = text,
                voiceName = voiceChoice,
                pitch = targetPitch,
                rate = targetRate,
                onComplete = onComplete,
                onFailure = {
                    // Fallback sang Online nếu giọng máy bị lỗi hoặc câm
                    val fallbackUrl = "https://translate.google.com/translate_tts?ie=UTF-8&q=" +
                            URLEncoder.encode(text, "UTF-8") + "&tl=zh-CN&client=tw-ob"
                    playOnlineAudio(fallbackUrl, targetRate, targetPitch, text, voiceChoice, forcePlay, onComplete)
                }
            )
            return
        }

        // B. Nếu là 9 giọng tuyển chọn (Curated Voices):
        // Tất cả 9 giọng đều có Audio URL nguồn chất lượng cao + hardware pitch shifting
        val primaryUrl = when (voiceChoice) {
            "baidu-online" -> {
                val bSpeed = if (targetRate <= 0.75f) "4" else if (targetRate >= 1.0f) "6" else "5"
                "https://fanyi.baidu.com/gettts?lan=zh&text=" + URLEncoder.encode(text, "UTF-8") + "&spd=" + bSpeed + "&source=web"
            }
            "youdao-online" -> "https://dict.youdao.com/dictvoice?audio=" + URLEncoder.encode(text, "UTF-8") + "&le=zh"
            "google-tw" -> "https://translate.google.com/translate_tts?ie=UTF-8&q=" + URLEncoder.encode(text, "UTF-8") + "&tl=zh-TW&client=tw-ob"
            else -> "https://translate.google.com/translate_tts?ie=UTF-8&q=" + URLEncoder.encode(text, "UTF-8") + "&tl=zh-CN&client=tw-ob"
        }

        playOnlineAudio(
            url = primaryUrl,
            rate = targetRate,
            pitch = targetPitch,
            fallbackText = text,
            voiceChoice = voiceChoice,
            forcePlay = forcePlay,
            onComplete = onComplete
        )
    }

    private fun playOnlineAudio(
        url: String,
        rate: Float,
        pitch: Float,
        fallbackText: String,
        voiceChoice: String,
        forcePlay: Boolean,
        onComplete: (() -> Unit)?
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA) // Luôn dùng USAGE_MEDIA chuẩn loa
                            .build()
                    )

                    val headers = mapOf(
                        "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
                        "Referer" to "https://translate.google.com/"
                    )
                    setDataSource(context, Uri.parse(url), headers)

                    // Tính âm lượng theo Discord 0 - 200%
                    val masterScale = if (preferences.masterEnabled) preferences.masterVolume / 100f else 1f
                    val speechScale = if (preferences.speechEnabled) preferences.speechVolume / 100f else 1f
                    val totalScale = masterScale * speechScale
                    val finalVol = if (forcePlay && totalScale <= 0f) 1.0f else totalScale.coerceIn(0.05f, 1.0f)
                    setVolume(finalVol, finalVol)

                    setOnPreparedListener { mp ->
                        // Chỉ set playbackParams SAU KHI mp đã chuẩn bị xong
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val params = mp.playbackParams
                                params.speed = rate.coerceIn(0.5f, 2.0f)
                                params.pitch = pitch.coerceIn(0.5f, 2.0f)
                                mp.playbackParams = params
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        mp.start()
                    }

                    setOnCompletionListener {
                        this@ChineseSpeechManager.isSpeaking = false
                        it.reset()
                        it.release()
                        mediaPlayer = null
                        scope.launch(Dispatchers.Main) { onComplete?.invoke() }
                    }

                    setOnErrorListener { _, _, _ ->
                        this@ChineseSpeechManager.isSpeaking = false
                        try {
                            mediaPlayer?.reset()
                            mediaPlayer?.release()
                        } catch (_: Exception) {}
                        mediaPlayer = null

                        // Fallback sang TTS nếu mất kết nối mạng
                        scope.launch(Dispatchers.Main) {
                            speakViaTts(fallbackText, voiceChoice, pitch, rate, onComplete, onFailure = {
                                // Nếu cả TTS cũng lỗi, thử URL Google dự phòng đơn giản
                                val googleUrl = "https://translate.google.com/translate_tts?ie=UTF-8&q=" +
                                        URLEncoder.encode(fallbackText, "UTF-8") + "&tl=zh-CN&client=tw-ob"
                                if (url != googleUrl) {
                                    playOnlineAudio(googleUrl, rate, pitch, fallbackText, "google-online", forcePlay, onComplete)
                                } else {
                                    onComplete?.invoke()
                                }
                            })
                        }
                        true
                    }

                    prepareAsync()
                }
                mediaPlayer = player
            } catch (e: Exception) {
                // Fallback nếu prepareAsync ném ngoại lệ
                scope.launch(Dispatchers.Main) {
                    speakViaTts(fallbackText, voiceChoice, pitch, rate, onComplete, onFailure = {
                        val googleUrl = "https://translate.google.com/translate_tts?ie=UTF-8&q=" +
                                URLEncoder.encode(fallbackText, "UTF-8") + "&tl=zh-CN&client=tw-ob"
                        if (url != googleUrl) {
                            playOnlineAudio(googleUrl, rate, pitch, fallbackText, "google-online", forcePlay, onComplete)
                        } else {
                            onComplete?.invoke()
                        }
                    })
                }
            }
        }
    }

    private fun speakViaTts(
        text: String,
        voiceName: String?,
        pitch: Float,
        rate: Float,
        onComplete: (() -> Unit)?,
        onFailure: (() -> Unit)? = null
    ) {
        if (!isTtsReady || tts == null) {
            onFailure?.invoke() ?: run {
                this@ChineseSpeechManager.isSpeaking = false
                onComplete?.invoke()
            }
            return
        }

        tts?.apply {
            setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            setPitch(pitch.coerceIn(0.5f, 2.0f))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (voiceName != null && curatedVoices.none { it.id == voiceName }) {
                    val matched = voices?.find { it.name == voiceName }
                    if (matched != null) {
                        voice = matched
                        language = matched.locale ?: Locale.SIMPLIFIED_CHINESE
                    } else {
                        language = Locale.SIMPLIFIED_CHINESE
                    }
                } else if (voiceName == "google-tw") {
                    language = Locale.TRADITIONAL_CHINESE
                } else {
                    language = Locale.SIMPLIFIED_CHINESE
                }
            } else {
                language = Locale.SIMPLIFIED_CHINESE
            }

            val utteranceId = "chinese_speech_" + System.currentTimeMillis()
            val speakResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } else {
                @Suppress("DEPRECATION")
                speak(text, TextToSpeech.QUEUE_FLUSH, null)
            }

            if (speakResult == TextToSpeech.ERROR) {
                onFailure?.invoke() ?: run {
                    this@ChineseSpeechManager.isSpeaking = false
                    onComplete?.invoke()
                }
                return
            }

            val estimatedDuration = ((text.length * 400L) / rate).toLong().coerceIn(1500L, 5000L)
            scope.launch {
                delay(estimatedDuration)
                this@ChineseSpeechManager.isSpeaking = false
                onComplete?.invoke()
            }
        }
    }

    /**
     * Nghe thử mẫu câu tiếng Trung "你好！很高兴认识你" (Xin chào! Rất vui được làm quen)
     */
    fun testVoice(
        voiceId: String? = null,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        speak("你好！很高兴认识你", forcePlay = true, voiceOverride = voiceId, onStart = onStart, onComplete = onComplete)
    }

    /**
     * Dừng phát âm ngay lập tức
     */
    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            tts?.stop()
        } catch (_: Exception) {}

        this@ChineseSpeechManager.isSpeaking = false
    }

    fun release() {
        stop()
        scope.cancel()
        try {
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
