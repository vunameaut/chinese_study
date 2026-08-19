package vhn.dev.study_chines.data.repository

import kotlinx.coroutines.flow.Flow
import vhn.dev.study_chines.data.model.Session
import vhn.dev.study_chines.data.model.Vocabulary
import vhn.dev.study_chines.data.remote.SupabaseDataSource
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.data.remote.VocabularyDto

class StudyRepository(private val dataSource: SupabaseDataSource) {
    // === Sessions ===
    val allSessions: Flow<List<SessionDto>> = dataSource.fetchSessionsFlow()

    suspend fun createSession(title: String): Long = dataSource.createSession(title) ?: 0L

    suspend fun deleteSession(id: Int) {
        dataSource.deleteSession(id)
    }

    suspend fun getMasteredCount(sessionId: Int): Int = dataSource.getMasteredCount(sessionId)

    suspend fun getTotalCount(sessionId: Int): Int = dataSource.getTotalCount(sessionId)

    // === Vocabulary ===
    fun getVocabularyBySession(sessionId: Int): Flow<List<VocabularyDto>> = 
        dataSource.getVocabularyBySessionFlow(sessionId)

    fun getVocabularyForReview(sessionId: Int): Flow<List<VocabularyDto>> = 
        dataSource.getVocabularyForReviewFlow(sessionId)

    suspend fun insertVocabulary(vocab: VocabularyDto): Long = 
        dataSource.insertVocabulary(vocab) ?: 0L

    suspend fun updateVocabulary(vocab: VocabularyDto) {
        dataSource.updateVocabulary(vocab)
    }

    suspend fun getRandomPinyinDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> =
        dataSource.getRandomPinyinDistractors(excludeId, sessionId, limit)

    suspend fun getRandomMeaningDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> =
        dataSource.getRandomMeaningDistractors(excludeId, sessionId, limit)
}