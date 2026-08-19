package vhn.dev.study_chines.data.model

data class Vocabulary(
    val id: Int = 0,
    val sessionId: Int = 0,
    val hanzi: String,
    val pinyin: String,
    val wordType: String? = null,
    val meaning: String,
    val reviewStatus: Int = 0,
    val createdAt: String = "",
    val lastReviewedAt: String? = null
)
