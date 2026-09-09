package vhn.dev.study_chines.ui.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.model.GrammarExercise
import vhn.dev.study_chines.data.model.GrammarPoint
import vhn.dev.study_chines.data.model.LessonItem
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.data.repository.StudyRepository

data class GrammarUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0 = Lý thuyết & Ví dụ, 1 = Luyện tập
    val session: SessionDto? = null,
    val hskLevel: Int = 1,
    val lessonNum: Int = 1,
    val lessonTitle: String = "Bài 1",
    val grammarPoints: List<GrammarPoint> = emptyList(),
    val availableLessons: List<LessonItem> = emptyList(),

    // Practice State
    val exercises: List<GrammarExercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val selectedChoiceOption: String? = null,
    val selectedWords: List<String> = emptyList(),
    val availableWords: List<Pair<Int, String>> = emptyList(),
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val isPracticeFinished: Boolean = false
)

class GrammarViewModel(
    private val repository: StudyRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarUiState())
    val uiState: StateFlow<GrammarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                var detectedHsk = 1
                var detectedLesson = 1
                var detectedLessonTitle = "Bài 1"

                val sessions = repository.allSessions.first()
                val foundSession = sessions.find { it.id.toLong() == sessionId }
                if (foundSession != null) {
                    detectedHsk = foundSession.hskLevel
                    val title = foundSession.title
                    val numMatch = Regex("""(?:bài|bai|lesson|b)\s*[:.]?\s*(\d+)""", RegexOption.IGNORE_CASE).find(title)
                    if (numMatch != null) {
                        detectedLesson = numMatch.groupValues[1].toIntOrNull() ?: 1
                    }
                    detectedLessonTitle = "Bài $detectedLesson"
                }

                // Tải danh mục các bài học của HSK
                val allLessons = repository.getAllGrammarLessons(detectedHsk)
                val matchedLessonItem = allLessons.find { it.lessonNum == detectedLesson }
                if (matchedLessonItem != null) {
                    detectedLessonTitle = matchedLessonItem.title
                }

                val points = repository.getGrammarPoints(sessionId, detectedHsk, detectedLesson)
                val exList = points.flatMap { it.exercises }

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        session = foundSession,
                        hskLevel = detectedHsk,
                        lessonNum = detectedLesson,
                        lessonTitle = detectedLessonTitle,
                        grammarPoints = points,
                        availableLessons = allLessons,
                        exercises = exList
                    )
                }
                initExercise()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectLesson(hsk: Int, lesson: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hskLevel = hsk, lessonNum = lesson) }
            val allLessons = repository.getAllGrammarLessons(hsk)
            val lessonItem = allLessons.find { it.lessonNum == lesson }
            val title = lessonItem?.title ?: "Bài $lesson"

            val points = repository.getGrammarPoints(null, hsk, lesson)
            val exList = points.flatMap { it.exercises }

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    hskLevel = hsk,
                    lessonNum = lesson,
                    lessonTitle = title,
                    grammarPoints = points,
                    availableLessons = allLessons,
                    exercises = exList,
                    currentExerciseIndex = 0,
                    isPracticeFinished = false
                )
            }
            initExercise()
        }
    }

    fun switchTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun initExercise() {
        val state = _uiState.value
        val ex = state.exercises.getOrNull(state.currentExerciseIndex) ?: return

        if (ex.type == "order") {
            val wordsList = if (ex.words.isNotEmpty()) ex.words else ex.answer.split(" ").filter { it.isNotBlank() }
            val shuffled = wordsList.shuffled().mapIndexed { index, word -> index to word }
            _uiState.update {
                it.copy(
                    selectedChoiceOption = null,
                    selectedWords = emptyList(),
                    availableWords = shuffled,
                    isAnswered = false,
                    isCorrect = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedChoiceOption = null,
                    selectedWords = emptyList(),
                    availableWords = emptyList(),
                    isAnswered = false,
                    isCorrect = false
                )
            }
        }
    }

    fun selectChoice(option: String) {
        val state = _uiState.value
        if (state.isAnswered) return
        val currentEx = state.exercises.getOrNull(state.currentExerciseIndex) ?: return

        val isCorrect = option.trim() == currentEx.answer.trim()
        _uiState.update {
            it.copy(
                selectedChoiceOption = option,
                isAnswered = true,
                isCorrect = isCorrect,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                wrongCount = if (!isCorrect) it.wrongCount + 1 else it.wrongCount
            )
        }
    }

    fun pickWord(wordId: Int, word: String) {
        val state = _uiState.value
        if (state.isAnswered) return
        _uiState.update {
            it.copy(
                selectedWords = it.selectedWords + word,
                availableWords = it.availableWords.filter { p -> p.first != wordId }
            )
        }
    }

    fun unpickWord(wordIndex: Int) {
        val state = _uiState.value
        if (state.isAnswered) return
        val word = state.selectedWords.getOrNull(wordIndex) ?: return
        val newSelected = state.selectedWords.toMutableList().apply { removeAt(wordIndex) }
        val newId = (state.availableWords.maxOfOrNull { it.first } ?: 0) + 1
        _uiState.update {
            it.copy(
                selectedWords = newSelected,
                availableWords = it.availableWords + (newId to word)
            )
        }
    }

    fun checkOrderAnswer() {
        val state = _uiState.value
        if (state.isAnswered) return
        val currentEx = state.exercises.getOrNull(state.currentExerciseIndex) ?: return

        val assembled = state.selectedWords.joinToString("").replace(" ", "").trim()
        val expected = currentEx.answer.replace(" ", "").trim()
        val isCorrect = assembled == expected

        _uiState.update {
            it.copy(
                isAnswered = true,
                isCorrect = isCorrect,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                wrongCount = if (!isCorrect) it.wrongCount + 1 else it.wrongCount
            )
        }
    }

    fun nextExercise() {
        val state = _uiState.value
        val nextIdx = state.currentExerciseIndex + 1
        if (nextIdx >= state.exercises.size) {
            _uiState.update { it.copy(isPracticeFinished = true) }
        } else {
            _uiState.update { it.copy(currentExerciseIndex = nextIdx) }
            initExercise()
        }
    }

    fun restartPractice() {
        _uiState.update {
            it.copy(
                currentExerciseIndex = 0,
                correctCount = 0,
                wrongCount = 0,
                isPracticeFinished = false
            )
        }
        initExercise()
    }
}
