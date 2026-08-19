package vhn.dev.study_chines.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.remote.VocabularyDto
import vhn.dev.study_chines.data.repository.StudyRepository
import java.time.Instant

class EntryViewModel(private val repository: StudyRepository, private val sessionId: Long) : ViewModel() {
    private val _hanzi = MutableStateFlow("")
    val hanzi: StateFlow<String> = _hanzi.asStateFlow()
    private val _pinyin = MutableStateFlow("")
    val pinyin: StateFlow<String> = _pinyin.asStateFlow()
    private val _wordType = MutableStateFlow("")
    val wordType: StateFlow<String> = _wordType.asStateFlow()
    private val _meaning = MutableStateFlow("")
    val meaning: StateFlow<String> = _meaning.asStateFlow()
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()
    private val _savedCount = MutableStateFlow(0)
    val savedCount: StateFlow<Int> = _savedCount.asStateFlow()

    fun updateHanzi(v: String) { _hanzi.value = v }
    fun updatePinyin(v: String) { _pinyin.value = v }
    fun updateWordType(v: String) { _wordType.value = v }
    fun updateMeaning(v: String) { _meaning.value = v }

    fun saveVocabulary() {
        val h = _hanzi.value.trim()
        val p = _pinyin.value.trim()
        val m = _meaning.value.trim()
        if (h.isEmpty() || p.isEmpty() || m.isEmpty()) return

        viewModelScope.launch {
            repository.insertVocabulary(VocabularyDto(
                hanzi = h, pinyin = p,
                wordType = _wordType.value.trim().takeIf { it.isNotEmpty() },
                meaning = m, sessionId = sessionId.toInt(),
                createdAt = Instant.now().toString()
            ))
            _isSaved.value = true
            _savedCount.value++
            clearFields()
        }
    }

    fun resetSaveState() { _isSaved.value = false }

    private fun clearFields() {
        _hanzi.value = ""; _pinyin.value = ""; _wordType.value = ""; _meaning.value = ""
    }
}