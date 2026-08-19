package vhn.dev.study_chines.ui.quiz

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vhn.dev.study_chines.data.local.SessionDao
import vhn.dev.study_chines.data.local.SessionEntity
import vhn.dev.study_chines.data.local.VocabularyDao
import vhn.dev.study_chines.data.local.VocabularyEntity
import vhn.dev.study_chines.data.repository.StudyRepository

class FakeVocabularyDao(initial: List<VocabularyEntity> = emptyList()) : VocabularyDao {
    private val items = initial.toMutableList()
    var pinyinDistractors: List<String> = emptyList()
    var meaningDistractors: List<String> = emptyList()

    override suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long {
        val nextId = if (items.isEmpty()) 1 else (items.maxOf { it.id } + 1)
        items += vocabulary.copy(id = if (vocabulary.id == 0) nextId else vocabulary.id)
        return nextId.toLong()
    }

    override suspend fun updateVocabulary(vocabulary: VocabularyEntity) {
        val index = items.indexOfFirst { it.id == vocabulary.id }
        if (index >= 0) items[index] = vocabulary
    }

    override fun getVocabularyBySession(sessionId: Int): Flow<List<VocabularyEntity>> = flow {
        emit(items.filter { it.sessionId == sessionId })
    }

    override fun getVocabularyForReview(sessionId: Int): Flow<List<VocabularyEntity>> = flow {
        emit(items.filter { it.sessionId == sessionId && it.reviewStatus != 2 })
    }

    override suspend fun getRandomPinyinDistractors(
        excludeId: Int,
        sessionId: Int,
        limit: Int
    ): List<String> = pinyinDistractors.take(limit)

    override suspend fun getRandomMeaningDistractors(
        excludeId: Int,
        sessionId: Int,
        limit: Int
    ): List<String> = meaningDistractors.take(limit)

    override suspend fun deleteBySession(sessionId: Int) {
        items.removeAll { it.sessionId == sessionId }
    }
}

class FakeSessionDao : SessionDao {
    override suspend fun insertSession(session: SessionEntity): Long = 1L

    override suspend fun updateSession(session: SessionEntity) = Unit

    override fun getAllSessions(): Flow<List<SessionEntity>> = flow { emit(emptyList()) }

    override suspend fun getSessionById(id: Int): SessionEntity? = null

    override suspend fun deleteSession(id: Int) = Unit

    override suspend fun getMasteredCount(sessionId: Int): Int = 0

    override suspend fun getTotalCount(sessionId: Int): Int = 0
}

private fun createRepository(
    initial: List<VocabularyEntity>,
    pinyin: List<String> = emptyList(),
    meaning: List<String> = emptyList()
): StudyRepository {
    val fakeDao = FakeVocabularyDao(initial)
    fakeDao.pinyinDistractors = pinyin
    fakeDao.meaningDistractors = meaning
    return StudyRepository(fakeDao, FakeSessionDao())
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @Test
    fun `initial load picks first vocab and shows pinyin options`() = runTest {
        val v1 = VocabularyEntity(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val v2 = VocabularyEntity(id = 2, hanzi = "校", pinyin = "xiào", wordType = "Danh từ", meaning = "Trường", sessionId = 10)

        val repository = createRepository(
            listOf(v1, v2),
            pinyin = listOf("xiào", "shì", "mìng")
        )
        val vm = QuizViewModel(repository, 10)

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(v1, state.currentVocab)
        assertEquals(QuizStep.PINYIN_VALIDATION, state.step)
        assertTrue(state.options.contains(v1.pinyin))
        assertEquals(2, state.remainingVocabs)
    }

    @Test
    fun `correct pinyin then move to meaning validation`() = runTest {
        val v1 = VocabularyEntity(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val repository = createRepository(
            listOf(v1),
            pinyin = listOf("xiào", "shì", "mìng"),
            meaning = listOf("Trường", "Người", "Đất")
        )

        val vm = QuizViewModel(repository, 10)
        vm.submitAnswer("xué")

        val s1 = vm.uiState.value
        assertTrue(s1.isAnswerSelected)
        assertTrue(s1.isCorrect)
        assertEquals(QuizStep.PINYIN_VALIDATION, s1.step)

        vm.nextStep()
        val s2 = vm.uiState.value
        assertEquals(QuizStep.MEANING_VALIDATION, s2.step)
        assertFalse(s2.isAnswerSelected)
        assertTrue(s2.options.contains(v1.meaning))
    }

    @Test
    fun `wrong answer pushes vocab to end and advances`() = runTest {
        val v1 = VocabularyEntity(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val v2 = VocabularyEntity(id = 2, hanzi = "校", pinyin = "xiào", wordType = "Danh từ", meaning = "Trường", sessionId = 10)
        val repository = createRepository(
            listOf(v1, v2),
            pinyin = listOf("xiào", "shì", "mìng")
        )

        val vm = QuizViewModel(repository, 10)
        vm.submitAnswer("wrong")

        val s1 = vm.uiState.value
        assertTrue(s1.isAnswerSelected)
        assertFalse(s1.isCorrect)

        vm.nextStep()
        val s2 = vm.uiState.value
        assertEquals(v2, s2.currentVocab)
    }
}
