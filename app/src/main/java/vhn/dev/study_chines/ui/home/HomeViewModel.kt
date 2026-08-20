package vhn.dev.study_chines.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.data.repository.StudyRepository

data class HomeUiState(
    val sessions: List<SessionDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedHsk: Int = 1,
    val lastSessionId: Long = -1L
)

class HomeViewModel(
    private val repository: StudyRepository,
    private val preferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(selectedHsk = preferences.lastHsk, lastSessionId = preferences.lastSessionId))
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
        preferences.lastHsk = level
        _uiState.value = _uiState.value.copy(selectedHsk = level)
    }

    fun saveLastSession(sessionId: Long) {
        preferences.lastSessionId = sessionId
        _uiState.value = _uiState.value.copy(lastSessionId = sessionId)
    }

    fun createSession(title: String, hskLevel: Int, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createSession(title, hskLevel)
            if (id > 0L) {
                saveLastSession(id)
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

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.refresh()
        }
    }
}