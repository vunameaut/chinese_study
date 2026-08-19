package vhn.dev.study_chines.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM session ORDER BY created_at DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM session WHERE id = :id")
    suspend fun getSessionById(id: Int): SessionEntity?

    @Query("DELETE FROM session WHERE id = :id")
    suspend fun deleteSession(id: Int)

    @Query("SELECT COUNT(*) FROM vocabulary WHERE session_id = :sessionId AND review_status = 2")
    suspend fun getMasteredCount(sessionId: Int): Int

    @Query("SELECT COUNT(*) FROM vocabulary WHERE session_id = :sessionId")
    suspend fun getTotalCount(sessionId: Int): Int
}

@Dao
interface VocabularyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long

    @Update
    suspend fun updateVocabulary(vocabulary: VocabularyEntity)

    @Query("SELECT * FROM vocabulary WHERE session_id = :sessionId ORDER BY created_at DESC")
    fun getVocabularyBySession(sessionId: Int): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE session_id = :sessionId AND review_status != 2")
    fun getVocabularyForReview(sessionId: Int): Flow<List<VocabularyEntity>>

    @Query("SELECT pinyin FROM vocabulary WHERE id != :excludeId AND session_id = :sessionId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPinyinDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String>

    @Query("SELECT meaning FROM vocabulary WHERE id != :excludeId AND session_id = :sessionId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomMeaningDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String>

    @Query("DELETE FROM vocabulary WHERE session_id = :sessionId")
    suspend fun deleteBySession(sessionId: Int)
}