package vhn.dev.study_chines.ui.quiz

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import vhn.dev.study_chines.data.local.VocabularyEntity
import vhn.dev.study_chines.data.repository.VocabularyRepository

// A lightweight fake repository used for unit testing the ViewModel logic without Android.
class FakeVocabularyRepository(initial: List<VocabularyEntity> = emptyList()) : VocabularyRepository(FakeDao()) {
    // Will override methods by providing our own backing fields
    private val _vocabs = MutableStateFlow(initial)
    override val vocabularyForReview = _vocabs.asStateFlow()

    private var pinyinDistractors: List<String> = emptyList()
    private var meaningDistractors: List<String> = emptyList()

    fun setPinyinDistractors(list: List<String>) { pinyinDistractors = list }
    fun setMeaningDistractors(list: List<String>) { meaningDistractors = list }

    override suspend fun getRandomPinyinDistractors(excludeId: Int, limit: Int): List<String> {
        return pinyinDistractors.take(limit)
    }

    override suspend fun getRandomMeaningDistractors(excludeId: Int, limit: Int): List<String> {
        return meaningDistractors.take(limit)
    }

    // Utility to change vocabs mid-test
    fun setVocabs(list: List<VocabularyEntity>) { _vocabs.value = list }
}

// Minimal fake DAO implementation used to satisfy base class constructor; methods not used in tests.
class FakeDao : vhn.dev.study_chines.data.local.VocabularyDao {
    override suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long { return 1L }
    override suspend fun updateVocabulary(vocabulary: VocabularyEntity) {}
    override fun getAllVocabulary() = throw UnsupportedOperationException()
    override fun getVocabularyForReview() = throw UnsupportedOperationException()
    override suspend fun getRandomPinyinDistractors(excludeId: Int, limit: Int) = emptyList<String>()
    override suspend fun getRandomMeaningDistractors(excludeId: Int, limit: Int) = emptyList<String>()
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @Test
    fun `initial load picks first vocab and shows pinyin options`() = runTest {
        val v1 = VocabularyEntity(id = 1, hanzi = "学\\u00e0", pinyin = "xué", wordType = "Danh từ", meaning = "Học")
        val v2 = VocabularyEntity(id = 2, hanzi = "校", pinyin = "xiào", wordType = "Danh từ", meaning = "Trường")

        val fake = FakeVocabularyRepository(listOf(v1, v2))
        fake.setPinyinDistractors(listOf("xiào", "sh\\u00ec", "m\\u00edng"))

        val vm = QuizViewModel(fake)

        // wait for initial load (ViewModel init launches coroutine)
        // small delay is implicit in runTest; check state
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(v1, state.currentVocab)
        assertEquals(QuizStep.PINYIN_VALIDATION, state.step)
        assertTrue(state.options.contains(v1.pinyin))
        assertEquals(2, state.remainingVocabs)
    }

    @Test
    fun `correct pinyin then move to meaning validation`() = runTest {
        val v1 = VocabularyEntity(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học")
        val fake = FakeVocabularyRepository(listOf(v1))
        fake.setPinyinDistractors(listOf("xiào", "sh\\u00ec", "m\\u00edng"))
        fake.setMeaningDistractors(listOf("Trường", "Người", "Đất"))

        val vm = QuizViewModel(fake)

        // Submit correct pinyin
        vm.submitAnswer("xué")
        val s1 = vm.uiState.value
        assertTrue(s1.isAnswerSelected)
        assertTrue(s1.isCorrect)
        assertEquals(QuizStep.PINYIN_VALIDATION, s1.step)

        // Move to next step (meaning)
        vm.nextStep()
        val s2 = vm.uiState.value
        assertEquals(QuizStep.MEANING_VALIDATION, s2.step)
        assertFalse(s2.isAnswerSelected)
        assertTrue(s2.options.contains(v1.meaning))
    }

    @Test
    fun `wrong answer pushes vocab to end and advances`() = runTest {
        val v1 = VocabularyEntity(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học")
        val v2 = VocabularyEntity(id = 2, hanzi = "校", pinyin = "xiào", wordType = "Danh từ", meaning = "Trường")
        val fake = FakeVocabularyRepository(listOf(v1, v2))
        fake.setPinyinDistractors(listOf("xiào", "sh\\u00ec", "m\\u00edng"))

        val vm = QuizViewModel(fake)

        // Submit wrong pinyin
        vm.submitAnswer("wrong")
        val s1 = vm.uiState.value
        assertTrue(s1.isAnswerSelected)
        assertFalse(s1.isCorrect)

        // Advance; wrong answer should rotate queue so next vocab becomes current
        vm.nextStep()
        val s2 = vm.uiState.value
        assertEquals(v2, s2.currentVocab)
        // Also check that the failed vocab was marked as Learning (status 1) in repository update (no-op fake)
        // We cannot inspect repository internal update easily here; main point is queue advanced.
    }
}
