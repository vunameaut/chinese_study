package vhn.dev.study_chines.ui.write_pinyin

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vhn.dev.study_chines.data.remote.VocabularyDto
import vhn.dev.study_chines.ui.quiz.FakeStudyRepository

@OptIn(ExperimentalCoroutinesApi::class)
class WritePinyinViewModelTest {

    private fun createRepository(initial: List<VocabularyDto>): FakeStudyRepository {
        val repo = FakeStudyRepository()
        repo.vocabs = initial.toMutableList()
        return repo
    }

    @Test
    fun `test normalize pinyin with tone numbers and marks`() {
        assertEquals("xué", WritePinyinViewModel.normalizePinyin("xue2"))
        assertEquals("xué", WritePinyinViewModel.normalizePinyin("XUE2"))
        assertEquals("xué", WritePinyinViewModel.normalizePinyin("  xué  "))
        assertEquals("hǎo", WritePinyinViewModel.normalizePinyin("hao3"))
        assertEquals("nǐ", WritePinyinViewModel.normalizePinyin("ni3"))
        assertEquals("gǒu", WritePinyinViewModel.normalizePinyin("gou3"))
        assertEquals("lǜ", WritePinyinViewModel.normalizePinyin("lv4"))
        assertEquals("lǜ", WritePinyinViewModel.normalizePinyin("lü4"))
        assertEquals("nǐ hǎo", WritePinyinViewModel.normalizePinyin("ni3 hao3"))
    }

    @Test
    fun `test strip tones removes all diacritics and tone numbers`() {
        assertEquals("xue", WritePinyinViewModel.stripTones("xué"))
        assertEquals("xue", WritePinyinViewModel.stripTones("xue2"))
        assertEquals("hao", WritePinyinViewModel.stripTones("hǎo"))
        assertEquals("ni", WritePinyinViewModel.stripTones("nǐ"))
        assertEquals("lü", WritePinyinViewModel.stripTones("lǜ"))
        assertEquals("lü", WritePinyinViewModel.stripTones("lv4"))
    }

    @Test
    fun `test isPinyinMatch accepts both toned and toneless inputs`() {
        // Toned input (mark or number)
        assertTrue(WritePinyinViewModel.isPinyinMatch("xué", "xué"))
        assertTrue(WritePinyinViewModel.isPinyinMatch("xue2", "xué"))

        // Toneless input (without any tone marks)
        assertTrue(WritePinyinViewModel.isPinyinMatch("xue", "xué"))
        assertTrue(WritePinyinViewModel.isPinyinMatch("hao", "hǎo"))
        assertTrue(WritePinyinViewModel.isPinyinMatch("ni", "nǐ"))

        // Ü with v or ü
        assertTrue(WritePinyinViewModel.isPinyinMatch("lv", "lǜ"))
        assertTrue(WritePinyinViewModel.isPinyinMatch("lü", "lǜ"))
        assertTrue(WritePinyinViewModel.isPinyinMatch("lv4", "lǜ"))

        // Multi-syllable with or without space
        assertTrue(WritePinyinViewModel.isPinyinMatch("ni hao", "nǐ hǎo"))
        assertTrue(WritePinyinViewModel.isPinyinMatch("nihao", "nǐ hǎo"))
        assertTrue(WritePinyinViewModel.isPinyinMatch("ni3 hao3", "nǐ hǎo"))

        // Wrong input
        assertFalse(WritePinyinViewModel.isPinyinMatch("wrong", "xué"))
        assertFalse(WritePinyinViewModel.isPinyinMatch("ma", "xué"))
    }

    @Test
    fun `test checkAnswer succeeds with toneless input`() = runTest {
        val v1 = VocabularyDto(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val repo = createRepository(listOf(v1))
        val vm = WritePinyinViewModel(repo, 10)

        // User enters toneless "xue"
        vm.onInputChange("xue")
        vm.checkAnswer()

        val state = vm.uiState.value
        assertTrue(state.isChecked)
        assertTrue(state.isCorrect)

        vm.nextWord()
        val finalState = vm.uiState.value
        assertTrue(finalState.isFinished)
        assertEquals(1, finalState.correctCount)
        assertEquals(0, finalState.wrongCount)
    }

    @Test
    fun `test correct answer with tone number input`() = runTest {
        val v1 = VocabularyDto(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val repo = createRepository(listOf(v1))
        val vm = WritePinyinViewModel(repo, 10)

        vm.onInputChange("xue2")
        vm.checkAnswer()

        val state = vm.uiState.value
        assertTrue(state.isChecked)
        assertTrue(state.isCorrect)

        vm.nextWord()
        val finalState = vm.uiState.value
        assertTrue(finalState.isFinished)
        assertEquals(1, finalState.correctCount)
        assertEquals(0, finalState.wrongCount)
    }

    @Test
    fun `test wrong answer requeues word`() = runTest {
        val v1 = VocabularyDto(id = 1, hanzi = "学", pinyin = "xué", wordType = "Danh từ", meaning = "Học", sessionId = 10)
        val v2 = VocabularyDto(id = 2, hanzi = "校", pinyin = "xiào", wordType = "Danh từ", meaning = "Trường", sessionId = 10)
        val repo = createRepository(listOf(v1, v2))
        val vm = WritePinyinViewModel(repo, 10)

        val firstId = vm.uiState.value.currentVocab?.id

        vm.onInputChange("wrong")
        vm.checkAnswer()

        val state = vm.uiState.value
        assertTrue(state.isChecked)
        assertFalse(state.isCorrect)

        vm.nextWord()
        val nextState = vm.uiState.value
        assertEquals(1, nextState.wrongCount)
        // Vocabulary should have advanced to second word
        val secondId = nextState.currentVocab?.id
        assertTrue(firstId != secondId)
    }
}
