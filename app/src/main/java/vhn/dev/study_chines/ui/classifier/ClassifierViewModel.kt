package vhn.dev.study_chines.ui.classifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.model.ClassifierExercise
import vhn.dev.study_chines.data.model.ClassifierLessonItem
import vhn.dev.study_chines.data.model.ClassifierPoint
import vhn.dev.study_chines.data.repository.StudyRepository

data class ClassifierUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0 = Thẻ lượng từ & Cụm từ, 1 = Luyện tập
    val hskLevel: Int = 1,
    val lessonNum: Int = 1,
    val lessonTitle: String = "Bài 1",
    val classifiers: List<ClassifierPoint> = emptyList(),
    val availableLessons: List<ClassifierLessonItem> = emptyList(),

    // Practice / Quiz State
    val exercises: List<ClassifierExercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val selectedOption: String? = null,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val isPracticeFinished: Boolean = false
)

class ClassifierViewModel(
    private val repository: StudyRepository,
    initialHskLevel: Int = 1,
    initialLessonNum: Int = 1,
    private val sessionId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ClassifierUiState(hskLevel = initialHskLevel, lessonNum = initialLessonNum)
    )
    val uiState: StateFlow<ClassifierUiState> = _uiState.asStateFlow()

    init {
        loadData(initialHskLevel, initialLessonNum)
    }

    fun loadData(hsk: Int, lesson: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hskLevel = hsk, lessonNum = lesson) }
            try {
                var targetHsk = hsk
                var targetLesson = lesson
                var detectedTitle: String? = null

                if (sessionId != null && sessionId > 0) {
                    val sessions = repository.allSessions.first()
                    val foundSession = sessions.find { it.id.toLong() == sessionId }
                    if (foundSession != null) {
                        targetHsk = foundSession.hskLevel
                        val title = foundSession.title
                        val numMatch = Regex("""(?:bài|bai|lesson|b)\s*[:.]?\s*(\d+)""", RegexOption.IGNORE_CASE).find(title)
                        if (numMatch != null) {
                            targetLesson = numMatch.groupValues[1].toIntOrNull() ?: lesson
                        }
                        detectedTitle = "Bài $targetLesson"
                    }
                }

                val allLessons = repository.getAllClassifierLessons(targetHsk)
                val lessonItem = allLessons.find { it.lessonNum == targetLesson }
                val title = detectedTitle ?: lessonItem?.title ?: "Bài $targetLesson"

                val list = repository.getClassifiers(targetHsk, targetLesson, sessionId)

                // Tải danh sách bài tập: ưu tiên bài tập cấu hình sẵn từ DB
                val exList = mutableListOf<ClassifierExercise>()
                for (item in list) {
                    if (item.exercises.isNotEmpty()) {
                        exList.addAll(item.exercises)
                    }
                }

                // Nếu chưa có bài tập trong DB, tự động sinh bài tập trắc nghiệm từ các cụm danh từ
                if (exList.isEmpty() && list.isNotEmpty()) {
                    val allClassifiersInHsk = allLessons.flatMap { it.classifiers.map { c -> c.substringBefore(' ') } }.distinct()
                    val defaultPool = listOf("个", "只", "本", "张", "条", "件", "杯", "块", "辆", "把", "位", "瓶", "支")

                    for (item in list) {
                        for (col in item.collocations) {
                            val distractors = (allClassifiersInHsk + defaultPool)
                                .filter { it != item.classifier && it.isNotBlank() }
                                .shuffled()
                                .take(3)
                            val options = (distractors + item.classifier).shuffled()
                            val phraseText = col.phrase.ifEmpty { "一${item.classifier}${col.noun}" }
                            exList.add(
                                ClassifierExercise(
                                    type = "choice",
                                    question = "Điền lượng từ thích hợp:  一 ( ___ ) ${col.noun}",
                                    options = options,
                                    answer = item.classifier,
                                    pinyin = col.pinyin,
                                    meaning = col.meaning,
                                    explanation = "Danh từ \"${col.noun}\" kết hợp với lượng từ \"${item.classifier}\" ($phraseText: ${col.meaning})."
                                )
                            )
                        }
                    }
                }

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        hskLevel = targetHsk,
                        lessonNum = targetLesson,
                        lessonTitle = title,
                        classifiers = list,
                        availableLessons = allLessons,
                        exercises = exList,
                        currentExerciseIndex = 0,
                        selectedOption = null,
                        isAnswered = false,
                        isCorrect = false,
                        correctCount = 0,
                        wrongCount = 0,
                        isPracticeFinished = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectLesson(hsk: Int, lesson: Int) {
        loadData(hsk, lesson)
    }

    fun switchTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectOption(option: String, onAnswered: (Boolean, String) -> Unit) {
        val state = _uiState.value
        if (state.isAnswered) return
        val currentEx = state.exercises.getOrNull(state.currentExerciseIndex) ?: return

        val isCorrect = option.trim() == currentEx.answer.trim()
        _uiState.update {
            it.copy(
                selectedOption = option,
                isAnswered = true,
                isCorrect = isCorrect,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                wrongCount = if (!isCorrect) it.wrongCount + 1 else it.wrongCount
            )
        }
        val filledSentence = currentEx.question.replace(Regex("""\(?\s*___\s*\)?"""), currentEx.answer).substringAfter(":")
        val speechText = if (filledSentence.isNotBlank()) filledSentence else currentEx.answer
        onAnswered(isCorrect, speechText)
    }

    fun nextExercise() {
        val state = _uiState.value
        val nextIdx = state.currentExerciseIndex + 1
        if (nextIdx >= state.exercises.size) {
            _uiState.update { it.copy(isPracticeFinished = true) }
        } else {
            _uiState.update {
                it.copy(
                    currentExerciseIndex = nextIdx,
                    selectedOption = null,
                    isAnswered = false,
                    isCorrect = false
                )
            }
        }
    }

    fun restartPractice() {
        _uiState.update {
            it.copy(
                currentExerciseIndex = 0,
                selectedOption = null,
                isAnswered = false,
                isCorrect = false,
                correctCount = 0,
                wrongCount = 0,
                isPracticeFinished = false
            )
        }
    }
}
