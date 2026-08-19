package vhn.dev.study_chines.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SessionDto(
    val id: Int = 0,
    val title: String,
    @SerialName("created_at")
    val createdAt: String = "",
    @Transient
    val word_count: Int = 0
)

@Serializable
data class VocabularyDto(
    val id: Int = 0,
    @SerialName("session_id")
    val sessionId: Int = 0,
    val hanzi: String,
    val pinyin: String,
    @SerialName("word_type")
    val wordType: String? = null,
    val meaning: String,
    @SerialName("review_status")
    val reviewStatus: Int = 0,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("last_reviewed_at")
    val lastReviewedAt: String? = null
)