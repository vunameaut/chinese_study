package vhn.dev.study_chines.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.local.VocabularyEntity
import vhn.dev.study_chines.data.repository.VocabularyRepository

enum class QuizStep {
    PINYIN_VALIDATION,
    MEANING_VALIDATION,
    FINISHED
}

data class QuizState(
    val currentVocab: VocabularyEntity? = null,
    val step: QuizStep = QuizStep.PINYIN_VALIDATION,
    val options: List<String> = emptyList(),
    val isAnswerSelected: Boolean = false,
    val isCorrect: Boolean = false,
    val isLoading: Boolean = true,
    val remainingVocabs: Int = 0
)

class QuizViewModel(private val repository: VocabularyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizState())
    val uiState: StateFlow<QuizState> = _uiState.asStateFlow()

    private val vocabQueue = mutableListOf<VocabularyEntity>()

    private val bgScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // Run synchronously so unit tests can observe state immediately
        loadVocabulary()
    }

    private fun loadVocabulary() {
        runBlocking {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Lấy danh sách từ vựng cần ôn (status != 2)
            val vocabs = repository.vocabularyForReview.first()
            vocabQueue.clear()
            vocabQueue.addAll(vocabs)
            
            if (vocabQueue.isNotEmpty()) {
                setupNextFlashcard()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = QuizStep.FINISHED
                )
            }
        }
    }

    private suspend fun setupNextFlashcard() {
        if (vocabQueue.isEmpty()) {
            _uiState.value = _uiState.value.copy(step = QuizStep.FINISHED, remainingVocabs = 0)
            return
        }

        val nextVocab = vocabQueue.first()
        _uiState.value = _uiState.value.copy(
            currentVocab = nextVocab,
            step = QuizStep.PINYIN_VALIDATION,
            isAnswerSelected = false,
            isCorrect = false,
            remainingVocabs = vocabQueue.size
        )
        generatePinyinOptions(nextVocab)
    }

    private suspend fun generatePinyinOptions(vocab: VocabularyEntity) {
        val distractors = repository.getRandomPinyinDistractors(vocab.id, 3)
        val options = (distractors + vocab.pinyin).shuffled()
        _uiState.value = _uiState.value.copy(
            options = options,
            isLoading = false
        )
    }

    private suspend fun generateMeaningOptions(vocab: VocabularyEntity) {
        val distractors = repository.getRandomMeaningDistractors(vocab.id, 3)
        val options = (distractors + vocab.meaning).shuffled()
        _uiState.value = _uiState.value.copy(
            options = options,
            isAnswerSelected = false,
            isCorrect = false
        )
    }

    fun submitAnswer(answer: String) {
        val currentState = _uiState.value
        if (currentState.isAnswerSelected || currentState.currentVocab == null) return

        val vocab = currentState.currentVocab
        val isCorrect = when (currentState.step) {
            QuizStep.PINYIN_VALIDATION -> answer == vocab.pinyin
            QuizStep.MEANING_VALIDATION -> answer == vocab.meaning
            else -> false
        }

        _uiState.value = currentState.copy(
            isAnswerSelected = true,
            isCorrect = isCorrect
        )
    }

    fun nextStep() {
        val currentState = _uiState.value
        val vocab = currentState.currentVocab ?: return

        runBlocking {
            if (currentState.isCorrect) {
                if (currentState.step == QuizStep.PINYIN_VALIDATION) {
                    // Trả lời đúng Pinyin, chuyển sang Meaning
                    _uiState.value = currentState.copy(
                        step = QuizStep.MEANING_VALIDATION,
                        isAnswerSelected = false,
                        isCorrect = false
                    )
                    generateMeaningOptions(vocab)
                } else {
                    // Trả lời đúng cả Meaning -> Passed (Mastered)
                    markVocabAsPassed(vocab)
                    // remove current and advance
                    if (vocabQueue.isNotEmpty()) vocabQueue.removeAt(0)
                    setupNextFlashcard()
                }
            } else {
                // Trả lời sai (ở bất kỳ bước nào) -> Đánh dấu Learning và đẩy xuống cuối hàng đợi
                val updatedVocab = vocab.copy(
                    lastReviewedAt = System.currentTimeMillis(),
                    reviewStatus = 1
                )
                repository.updateVocabulary(updatedVocab)

                if (vocabQueue.isNotEmpty()) vocabQueue.removeAt(0)
                vocabQueue.add(updatedVocab)
                setupNextFlashcard()
            }
        }
    }

    private suspend fun markVocabAsPassed(vocab: VocabularyEntity) {
        // Update lastReviewedAt and mark as Mastered (2)
        val updatedVocab = vocab.copy(
            lastReviewedAt = System.currentTimeMillis(),
            reviewStatus = 2
        )
        repository.updateVocabulary(updatedVocab)
    }

    override fun onCleared() {
        super.onCleared()
        bgScope.cancel()
    }
}
