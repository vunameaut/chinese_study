package vhn.dev.study_chines.data.repository

import kotlinx.coroutines.flow.Flow
import vhn.dev.study_chines.data.local.SessionDao
import vhn.dev.study_chines.data.local.SessionEntity
import vhn.dev.study_chines.data.local.VocabularyDao
import vhn.dev.study_chines.data.local.VocabularyEntity

class StudyRepository(
    private val vocabularyDao: VocabularyDao,
    private val sessionDao: SessionDao
) {
    // === Sessions ===
    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun createSession(title: String): Long = sessionDao.insertSession(SessionEntity(title = title))
    suspend fun deleteSession(id: Int) { vocabularyDao.deleteBySession(id); sessionDao.deleteSession(id) }
    suspend fun getMasteredCount(sessionId: Int): Int = sessionDao.getMasteredCount(sessionId)
    suspend fun getTotalCount(sessionId: Int): Int = sessionDao.getTotalCount(sessionId)

    // === Vocabulary ===
    fun getVocabularyBySession(sessionId: Int): Flow<List<VocabularyEntity>> = vocabularyDao.getVocabularyBySession(sessionId)
    fun getVocabularyForReview(sessionId: Int): Flow<List<VocabularyEntity>> = vocabularyDao.getVocabularyForReview(sessionId)

    suspend fun insertVocabulary(vocab: VocabularyEntity): Long = vocabularyDao.insertVocabulary(vocab)
    suspend fun updateVocabulary(vocab: VocabularyEntity) = vocabularyDao.updateVocabulary(vocab)
    suspend fun getRandomPinyinDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> =
        vocabularyDao.getRandomPinyinDistractors(excludeId, sessionId, limit)
    suspend fun getRandomMeaningDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> =
        vocabularyDao.getRandomMeaningDistractors(excludeId, sessionId, limit)
}