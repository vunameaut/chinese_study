package vhn.dev.study_chines.ui.write_pinyin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import vhn.dev.study_chines.data.remote.VocabularyDto
import vhn.dev.study_chines.data.repository.StudyRepository

data class WritePinyinState(
    val currentVocab: VocabularyDto? = null,
    val userInput: String = "",
    val isChecked: Boolean = false,
    val isCorrect: Boolean = false,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val remainingVocabs: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val isRepractice: Boolean = false
)

class WritePinyinViewModel(
    private val repository: StudyRepository,
    private val sessionId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(WritePinyinState())
    val uiState: StateFlow<WritePinyinState> = _uiState.asStateFlow()
    private val vocabQueue = mutableListOf<VocabularyDto>()

    init { loadVocabulary() }

    private fun loadVocabulary() {
        runBlocking {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val vocabs = repository.getVocabularyForReview(sessionId).first()
            val isRepractice = vocabs.isNotEmpty() && vocabs.all { it.reviewStatus == 2 }
            _uiState.value = _uiState.value.copy(isRepractice = isRepractice)
            vocabQueue.clear(); vocabQueue.addAll(vocabs.shuffled())
            if (vocabQueue.isNotEmpty()) setupNextWord()
            else _uiState.value = _uiState.value.copy(isLoading = false, isFinished = true)
        }
    }

    private fun setupNextWord() {
        if (vocabQueue.isEmpty()) {
            _uiState.value = _uiState.value.copy(isFinished = true, remainingVocabs = 0)
            return
        }
        val next = vocabQueue.first()
        _uiState.value = _uiState.value.copy(
            currentVocab = next,
            userInput = "",
            isChecked = false,
            isCorrect = false,
            isLoading = false,
            isFinished = false,
            remainingVocabs = vocabQueue.size
        )
    }

    fun onInputChange(input: String) {
        if (_uiState.value.isChecked) return
        _uiState.value = _uiState.value.copy(userInput = input)
    }

    fun checkAnswer() {
        val s = _uiState.value
        if (s.isChecked || s.currentVocab == null || s.userInput.isBlank()) return

        val correct = isPinyinMatch(s.userInput, s.currentVocab.pinyin)
        _uiState.value = s.copy(isChecked = true, isCorrect = correct)
    }

    fun nextWord() {
        val s = _uiState.value
        if (!s.isChecked) return
        val v = s.currentVocab ?: return
        val isRepractice = s.isRepractice

        runBlocking {
            if (s.isCorrect) {
                // Mastered: Chỉ cập nhật database nếu không phải ôn lại và từ chưa thuộc
                if (!isRepractice && v.reviewStatus != 2) {
                    val updated = v.copy(
                        lastReviewedAt = System.currentTimeMillis().toIso8601(),
                        reviewStatus = 2
                    )
                    repository.updateVocabulary(updated)
                }
                _uiState.value = _uiState.value.copy(correctCount = _uiState.value.correctCount + 1)
                if (vocabQueue.isNotEmpty()) vocabQueue.removeAt(0)
            } else {
                // Requeue: Chỉ cập nhật database nếu không phải ôn lại và từ chưa thuộc
                if (!isRepractice && v.reviewStatus != 2) {
                    val updated = v.copy(
                        lastReviewedAt = System.currentTimeMillis().toIso8601(),
                        reviewStatus = 1
                    )
                    repository.updateVocabulary(updated)
                }
                if (vocabQueue.isNotEmpty()) vocabQueue.removeAt(0)
                vocabQueue.add(v)
                _uiState.value = _uiState.value.copy(wrongCount = _uiState.value.wrongCount + 1)
            }
            setupNextWord()
        }
    }

    companion object {
        /**
         * Normalize pinyin cho so sánh:
         * 1. Lowercase, trim
         * 2. Chuyển tone number → tone mark (ví dụ: xue2 → xué, ni3 hao3 → nǐ hǎo)
         * 3. Chuyển v → ü
         */
        fun normalizePinyin(input: String): String {
            val s = input.trim().lowercase().replace("v", "ü")
            if (s.contains(" ")) {
                return s.split("\\s+".toRegex()).joinToString(" ") { convertToneNumberToMark(it) }
            }
            return convertToneNumberToMark(s)
        }

        private val toneMap = mapOf(
            'a' to listOf('ā', 'á', 'ǎ', 'à', 'a'),
            'e' to listOf('ē', 'é', 'ě', 'è', 'e'),
            'i' to listOf('ī', 'í', 'ǐ', 'ì', 'i'),
            'o' to listOf('ō', 'ó', 'ǒ', 'ò', 'o'),
            'u' to listOf('ū', 'ú', 'ǔ', 'ù', 'u'),
            'ü' to listOf('ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü')
        )

        // Reverse map: tone mark → base vowel (for normalization)
        val toneMarkToBase: Map<Char, Char> = buildMap {
            toneMap.forEach { (base, marks) ->
                marks.forEach { mark -> put(mark, base) }
            }
        }

        /**
         * Bỏ toàn bộ thanh điệu (số hoặc dấu) để so sánh không dấu
         */
        fun stripTones(input: String): String {
            val sb = StringBuilder()
            for (ch in input.trim().lowercase().replace("v", "ü")) {
                if (ch in '1'..'5') continue
                sb.append(toneMarkToBase[ch] ?: ch)
            }
            return sb.toString()
        }

        /**
         * Kiểm tra đáp án: chấp nhận cả có thanh điệu (xué, xue2) và không thanh điệu (xue)
         */
        fun isPinyinMatch(userInput: String, targetPinyin: String): Boolean {
            val normUser = normalizePinyin(userInput)
            val normTarget = normalizePinyin(targetPinyin)

            // 1. Khớp có thanh điệu (dấu thanh hoặc số)
            if (normUser == normTarget) return true
            if (normUser.replace(" ", "") == normTarget.replace(" ", "")) return true

            // 2. Chấp nhận không có thanh điệu
            val strippedUser = stripTones(normUser).replace(" ", "")
            val strippedTarget = stripTones(normTarget).replace(" ", "")

            return strippedUser == strippedTarget
        }

        /**
         * Nếu input là "xue2" → chuyển thành "xué"
         * Nếu input đã có dấu thanh → giữ nguyên
         */
        private fun convertToneNumberToMark(s: String): String {
            if (s.isEmpty()) return s
            val last = s.last()
            if (last !in '1'..'5') return s

            val tone = last.digitToInt() // 1-5
            val base = s.dropLast(1)
            if (base.isEmpty()) return s

            // Tìm nguyên âm để đặt dấu thanh theo quy tắc pinyin:
            // 1. Nếu có 'a' hoặc 'e' → đặt dấu trên đó
            // 2. Nếu có 'ou' → đặt dấu trên 'o'
            // 3. Ngược lại → đặt dấu trên nguyên âm cuối cùng
            val vowelIndex = findToneVowelIndex(base)
            if (vowelIndex < 0) return s

            val vowel = base[vowelIndex]
            val toneChars = toneMap[vowel] ?: return s
            val toned = toneChars[tone - 1]
            return base.substring(0, vowelIndex) + toned + base.substring(vowelIndex + 1)
        }

        private fun findToneVowelIndex(s: String): Int {
            // Rule 1: a or e gets the tone
            for (i in s.indices) {
                if (s[i] == 'a' || s[i] == 'e') return i
            }
            // Rule 2: ou → tone on o
            val ouIdx = s.indexOf("ou")
            if (ouIdx >= 0) return ouIdx
            // Rule 3: last vowel
            for (i in s.indices.reversed()) {
                if (s[i] in "iouü") return i
            }
            return -1
        }
    }
}

private fun Long.toIso8601(): String {
    return java.time.Instant.ofEpochMilli(this).toString()
}
