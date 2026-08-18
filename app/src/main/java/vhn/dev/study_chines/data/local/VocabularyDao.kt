package vhn.dev.study_chines.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long

    @Update
    suspend fun updateVocabulary(vocabulary: VocabularyEntity)

    @Query("SELECT * FROM vocabulary ORDER BY created_at DESC")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE review_status != 2 ORDER BY last_reviewed_at ASC")
    fun getVocabularyForReview(): Flow<List<VocabularyEntity>>

    @Query("SELECT pinyin FROM vocabulary WHERE id != :excludeId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPinyinDistractors(excludeId: Int, limit: Int = 3): List<String>

    @Query("SELECT meaning FROM vocabulary WHERE id != :excludeId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomMeaningDistractors(excludeId: Int, limit: Int = 3): List<String>
}
