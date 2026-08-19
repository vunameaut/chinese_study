package vhn.dev.study_chines.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.data.repository.StudyRepository

data class HomeUiState(
    val sessions: List<SessionDto> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(private val repository: StudyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allSessions.collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions, isLoading = false)
            }
        }
    }

    fun createSession(title: String, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createSession(title)
            onResult(id)
        }
    }

    fun deleteSession(id: Int) {
        viewModelScope.launch { repository.deleteSession(id) }
    }
}