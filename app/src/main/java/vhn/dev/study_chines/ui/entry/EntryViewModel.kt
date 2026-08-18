package vhn.dev.study_chines.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.local.VocabularyEntity
import vhn.dev.study_chines.data.repository.VocabularyRepository

class EntryViewModel(private val repository: VocabularyRepository) : ViewModel() {

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

    fun updateHanzi(newHanzi: String) {
        _hanzi.value = newHanzi
    }

    fun updatePinyin(newPinyin: String) {
        _pinyin.value = newPinyin
    }

    fun updateWordType(newWordType: String) {
        _wordType.value = newWordType
    }

    fun updateMeaning(newMeaning: String) {
        _meaning.value = newMeaning
    }

    fun saveVocabulary() {
        val currentHanzi = _hanzi.value.trim()
        val currentPinyin = _pinyin.value.trim()
        val currentMeaning = _meaning.value.trim()

        if (currentHanzi.isNotEmpty() && currentPinyin.isNotEmpty() && currentMeaning.isNotEmpty()) {
            viewModelScope.launch {
                val newVocab = VocabularyEntity(
                    hanzi = currentHanzi,
                    pinyin = currentPinyin,
                    wordType = _wordType.value.trim().takeIf { it.isNotEmpty() },
                    meaning = currentMeaning
                )
                repository.insertVocabulary(newVocab)
                _isSaved.value = true
                clearFields()
            }
        }
    }

    fun resetSaveState() {
        _isSaved.value = false
    }

    private fun clearFields() {
        _hanzi.value = ""
        _pinyin.value = ""
        _wordType.value = ""
        _meaning.value = ""
    }
}
