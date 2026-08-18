package vhn.dev.study_chines.data.repository

import kotlinx.coroutines.flow.Flow
import vhn.dev.study_chines.data.local.VocabularyDao
import vhn.dev.study_chines.data.local.VocabularyEntity

open class VocabularyRepository(private val vocabularyDao: VocabularyDao) {

    open val allVocabulary: Flow<List<VocabularyEntity>>
        get() = vocabularyDao.getAllVocabulary()

    open val vocabularyForReview: Flow<List<VocabularyEntity>>
        get() = vocabularyDao.getVocabularyForReview()

    suspend fun insertVocabulary(vocabulary: VocabularyEntity) {
        vocabularyDao.insertVocabulary(vocabulary)
    }

    suspend fun updateVocabulary(vocabulary: VocabularyEntity) {
        vocabularyDao.updateVocabulary(vocabulary)
    }

    open suspend fun getRandomPinyinDistractors(excludeId: Int, limit: Int = 3): List<String> {
        return vocabularyDao.getRandomPinyinDistractors(excludeId, limit)
    }

    open suspend fun getRandomMeaningDistractors(excludeId: Int, limit: Int = 3): List<String> {
        return vocabularyDao.getRandomMeaningDistractors(excludeId, limit)
    }
}
