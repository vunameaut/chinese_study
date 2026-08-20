package vhn.dev.study_chines.ui.quiz

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vhn.dev.study_chines.data.remote.VocabularyDto
import vhn.dev.study_chines.data.repository.StudyRepository
import vhn.dev.study_chines.data.remote.SupabaseDataSource
import kotlinx.coroutines.flow.MutableStateFlow

class FakeStudyRepository : StudyRepository(SupabaseDataSource()) {
    var vocabs = mutableListOf<VocabularyDto>()
    var pinyinDistractors = listOf<String>()
    var meaningDistractors = listOf<String>()
    val updatedVocabs = mutableListOf<VocabularyDto>()

    override fun getVocabularyForReview(sessionId: Int): Flow<List<VocabularyDto>> = flow {
        emit(vocabs.filter { it.sessionId == sessionId && it.reviewStatus != 2 })
    }

    override suspend fun getRandomPinyinDistractors(excludeId: Int, sessionId: Int, limit: Int): List<String> {
        return pinyinDistractors.filter { it != vocabs.find { v -> v.id == excludeId }?.pinyin }.take(limit)
    }

    override suspend fun getRandomMeaningDistractors(excludeId: Int, sessionId: Int, limit: Int): List<String> {
        return meaningDistractors.filter { it != vocabs.find { v -> v.id == excludeId }?.meaning }.take(limit)
    }

    override suspend fun updateVocabulary(vocab: VocabularyDto) {
        updatedVocabs.add(vocab)
        val index = vocabs.indexOfFirst { it.id == vocab.id }
        if (index >= 0) vocabs[index] = vocab
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private fun createRepository(
        initial: List<VocabularyDto>,
        pinyin: List<String> = emptyList(),
        meaning: List<String> = emptyList()
    ): FakeStudyRepository {
        val repo = FakeStudyRepository()
        repo.vocabs = initial.toMutableList()
        repo.pinyinDistractors = pinyin
        repo.meaningDistractors = meaning
        return repo
    }

    @Test
    fun `initial load picks first vocab and shows pinyin options`() = runTest {
        val v1 = VocabularyDto(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val v2 = VocabularyDto(id = 2, hanzi = "校", pinyin = "xiào", wordType = "Danh từ", meaning = "Trường", sessionId = 10)

        val repository = createRepository(
            listOf(v1, v2),
            pinyin = listOf("xiào", "shì", "mìng")
        )
        val vm = QuizViewModel(repository, 10)

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(v1.id, state.currentVocab?.id)
        assertEquals(QuizStep.PINYIN_VALIDATION, state.step)
        assertTrue(state.options.contains(v1.pinyin))
        assertEquals(2, state.remainingVocabs)
    }

    @Test
    fun `correct pinyin then move to meaning validation`() = runTest {
        val v1 = VocabularyDto(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
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
        val v1 = VocabularyDto(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val v2 = VocabularyDto(id = 2, hanzi = "校", pinyin = "xiào", wordType = "Danh từ", meaning = "Trường", sessionId = 10)
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
        assertEquals(v2.id, s2.currentVocab?.id)
    }
}
