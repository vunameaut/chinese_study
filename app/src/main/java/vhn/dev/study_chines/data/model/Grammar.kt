package vhn.dev.study_chines.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GrammarExample(
    val hanzi: String = "",
    val pinyin: String = "",
    val meaning: String = ""
)

@Serializable
data class GrammarExercise(
    val type: String = "choice", // "choice" or "order"
    val question: String = "",
    val options: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val answer: String = "",
    val pinyin: String = "",
    val meaning: String = "",
    val explanation: String = ""
)

@Serializable
data class GrammarPoint(
    val id: Long = 0,
    @SerialName("session_id")
    val sessionId: Long? = null,
    @SerialName("hsk_level")
    val hskLevel: Int = 1,
    @SerialName("lesson_num")
    val lessonNum: Int = 1,
    @SerialName("lesson_title")
    val lessonTitle: String? = null,
    val title: String = "",
    val structure: String? = null,
    val explanation: String? = null,
    val examples: List<GrammarExample> = emptyList(),
    val exercises: List<GrammarExercise> = emptyList(),
    @SerialName("order_index")
    val orderIndex: Int = 1,
    @SerialName("created_at")
    val createdAt: String = ""
)

data class LessonItem(
    val hskLevel: Int,
    val lessonNum: Int,
    val title: String
)
