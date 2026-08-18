package vhn.dev.study_chines.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hanzi: String,
    val pinyin: String,
    @ColumnInfo(name = "word_type")
    val wordType: String?,
    val meaning: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_reviewed_at")
    val lastReviewedAt: Long? = null,
    @ColumnInfo(name = "review_status")
    val reviewStatus: Int = 0 // 0: New, 1: Learning, 2: Mastered
)
