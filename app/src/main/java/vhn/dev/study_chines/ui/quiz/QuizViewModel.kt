package vhn.dev.study_chines.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import vhn.dev.study_chines.data.remote.VocabularyDto
import vhn.dev.study_chines.data.repository.StudyRepository

enum class QuizStep { PINYIN_VALIDATION, MEANING_VALIDATION, FINISHED }

data class QuizState(
    val currentVocab: VocabularyDto? = null,
    val step: QuizStep = QuizStep.PINYIN_VALIDATION,
    val options: List<String> = emptyList(),
    val isAnswerSelected: Boolean = false,
    val isCorrect: Boolean = false,
    val isLoading: Boolean = true,
    val remainingVocabs: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0
)

class QuizViewModel(private val repository: StudyRepository, private val sessionId: Int) : ViewModel() {
    private val _uiState = MutableStateFlow(QuizState())
    val uiState: StateFlow<QuizState> = _uiState.asStateFlow()
    private val vocabQueue = mutableListOf<VocabularyDto>()
    private val bgScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init { loadVocabulary() }

    private fun loadVocabulary() {
        runBlocking {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val vocabs = repository.getVocabularyForReview(sessionId).first()
            // Xáo trộn thứ tự từ vựng để câu hỏi ngẫu nhiên, không theo thứ tự trong data
            vocabQueue.clear(); vocabQueue.addAll(vocabs.shuffled())
            if (vocabQueue.isNotEmpty()) setupNextFlashcard()
            else _uiState.value = _uiState.value.copy(isLoading = false, step = QuizStep.FINISHED)
        }
    }

    private suspend fun setupNextFlashcard() {
        if (vocabQueue.isEmpty()) { _uiState.value = _uiState.value.copy(step = QuizStep.FINISHED, remainingVocabs = 0); return }
        val next = vocabQueue.first()
        _uiState.value = _uiState.value.copy(currentVocab = next, step = QuizStep.PINYIN_VALIDATION, isAnswerSelected = false, remainingVocabs = vocabQueue.size)
        generatePinyinOptions(next)
    }

    private suspend fun generatePinyinOptions(vocab: VocabularyDto) {
        val d = repository.getRandomPinyinDistractors(vocab.id, sessionId, 3)
        _uiState.value = _uiState.value.copy(options = (d + vocab.pinyin).shuffled(), isLoading = false)
    }

    private suspend fun generateMeaningOptions(vocab: VocabularyDto) {
        val d = repository.getRandomMeaningDistractors(vocab.id, sessionId, 3)
        _uiState.value = _uiState.value.copy(options = (d + vocab.meaning).shuffled(), isAnswerSelected = false)
    }

    fun submitAnswer(answer: String) {
        val s = _uiState.value; if (s.isAnswerSelected || s.currentVocab == null) return
        val correct = when (s.step) {
            QuizStep.PINYIN_VALIDATION -> answer == s.currentVocab.pinyin
            QuizStep.MEANING_VALIDATION -> answer == s.currentVocab.meaning
            QuizStep.FINISHED -> false
        }
        _uiState.value = s.copy(isAnswerSelected = true, isCorrect = correct)
    }

    fun nextStep() {
        val s = _uiState.value; val v = s.currentVocab ?: return
        runBlocking {
            if (s.isCorrect) {
                when (s.step) {
                    QuizStep.PINYIN_VALIDATION -> {
                        _uiState.value = s.copy(step = QuizStep.MEANING_VALIDATION, isAnswerSelected = false); generateMeaningOptions(v)
                    }
                    QuizStep.MEANING_VALIDATION -> {
                        markMastered(v); if (vocabQueue.isNotEmpty()) vocabQueue.removeAt(0); setupNextFlashcard()
                    }
                    QuizStep.FINISHED -> { /* no-op */ }
                }
            } else {
                // Wrong: requeue to end
                val updated = v.copy(lastReviewedAt = System.currentTimeMillis().toIso8601(), reviewStatus = 1)
                repository.updateVocabulary(updated)
                if (vocabQueue.isNotEmpty()) vocabQueue.removeAt(0)
                vocabQueue.add(updated)
                _uiState.value = _uiState.value.copy(wrongCount = _uiState.value.wrongCount + 1)
                setupNextFlashcard()
            }
        }
    }

    private suspend fun markMastered(v: VocabularyDto) {
        val updated = v.copy(lastReviewedAt = System.currentTimeMillis().toIso8601(), reviewStatus = 2)
        repository.updateVocabulary(updated)
        _uiState.value = _uiState.value.copy(correctCount = _uiState.value.correctCount + 1)
    }

    override fun onCleared() { super.onCleared(); bgScope.cancel() }
}

private fun Long.toIso8601(): String {
    return java.time.Instant.ofEpochMilli(this).toString()
}
