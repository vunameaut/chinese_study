package vhn.dev.study_chines.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.data.repository.StudyRepository

data class HomeUiState(
    val sessions: List<SessionDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedHsk: Int = 1
)

class HomeViewModel(private val repository: StudyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allSessions.collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions, isLoading = false, error = null)
            }
        }
    }

    val hskLevels = listOf(1, 2, 3, 4, 5, 6)

    fun selectHsk(level: Int) {
        _uiState.value = _uiState.value.copy(selectedHsk = level)
    }

    fun createSession(title: String, hskLevel: Int, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createSession(title, hskLevel)
            if (id > 0L) {
                onResult(id)
                _uiState.value = _uiState.value.copy(error = null)
            } else {
                _uiState.value = _uiState.value.copy(error = "Lỗi: Không thể tạo buổi học")
            }
        }
    }

    fun deleteSession(id: Int) {
        viewModelScope.launch { repository.deleteSession(id) }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}