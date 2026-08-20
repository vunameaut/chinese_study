package vhn.dev.study_chines.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import vhn.dev.study_chines.data.model.Session
import vhn.dev.study_chines.data.model.Vocabulary
import vhn.dev.study_chines.data.remote.SupabaseDataSource
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.data.remote.VocabularyDto

open class StudyRepository(private val dataSource: SupabaseDataSource) {
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    
    init {
        // Emit initial value to start flow
        refreshTrigger.tryEmit(Unit)
    }

    // === Sessions ===
    val allSessions: Flow<List<SessionDto>> = refreshTrigger.flatMapLatest { 
        dataSource.fetchSessionsFlow() 
    }

    suspend fun createSession(title: String): Long {
        val result = dataSource.createSession(title) ?: 0L
        if (result > 0L) {
            refreshTrigger.emit(Unit)
        }
        return result
    }

    suspend fun deleteSession(id: Int) {
        dataSource.deleteSession(id)
        refreshTrigger.emit(Unit)
    }

    suspend fun getMasteredCount(sessionId: Int): Int = dataSource.getMasteredCount(sessionId)

    suspend fun getTotalCount(sessionId: Int): Int = dataSource.getTotalCount(sessionId)

    // === Vocabulary ===
    open fun getVocabularyBySession(sessionId: Int): Flow<List<VocabularyDto>> = 
        dataSource.getVocabularyBySessionFlow(sessionId)

    open fun getVocabularyForReview(sessionId: Int): Flow<List<VocabularyDto>> = 
        dataSource.getVocabularyForReviewFlow(sessionId)

    open suspend fun insertVocabulary(vocab: VocabularyDto): Long = 
        dataSource.insertVocabulary(vocab) ?: 0L

    open suspend fun updateVocabulary(vocab: VocabularyDto) {
        dataSource.updateVocabulary(vocab)
    }

    open suspend fun getRandomPinyinDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> =
        dataSource.getRandomPinyinDistractors(excludeId, sessionId, limit)

    open suspend fun getRandomMeaningDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> =
        dataSource.getRandomMeaningDistractors(excludeId, sessionId, limit)
}