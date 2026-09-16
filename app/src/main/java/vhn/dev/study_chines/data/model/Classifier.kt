package vhn.dev.study_chines.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassifierCollocation(
    val noun: String = "",
    val pinyin: String = "",
    val phrase: String = "",
    val meaning: String = ""
)

@Serializable
data class ClassifierExample(
    val hanzi: String = "",
    val pinyin: String = "",
    val meaning: String = ""
)

@Serializable
data class ClassifierExercise(
    val type: String = "choice",
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = "",
    val pinyin: String = "",
    val meaning: String = "",
    val explanation: String = ""
)

@Serializable
data class ClassifierPoint(
    val id: Long = 0,
    @SerialName("session_id")
    val sessionId: Long? = null,
    @SerialName("hsk_level")
    val hskLevel: Int = 1,
    @SerialName("lesson_num")
    val lessonNum: Int = 1,
    @SerialName("lesson_title")
    val lessonTitle: String? = null,
    val classifier: String = "",
    val pinyin: String = "",
    val meaning: String = "",
    @SerialName("usage_note")
    val usageNote: String? = null,
    val collocations: List<ClassifierCollocation> = emptyList(),
    val examples: List<ClassifierExample> = emptyList(),
    val exercises: List<ClassifierExercise> = emptyList(),
    @SerialName("order_index")
    val orderIndex: Int = 1,
    @SerialName("created_at")
    val createdAt: String = ""
)

data class ClassifierLessonItem(
    val hskLevel: Int,
    val lessonNum: Int,
    val title: String,
    val classifiers: List<String> = emptyList(),
    val previewCollocations: List<String> = emptyList()
)
