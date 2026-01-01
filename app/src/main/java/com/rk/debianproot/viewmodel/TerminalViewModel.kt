package com.rk.debianproot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.debianproot.data.RootfsRepository
import com.rk.debianproot.data.RootfsState
import com.rk.debianproot.terminal.DebianLauncher
import com.rk.debianproot.terminal.ProcessManager
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TerminalUiState(
    val rootfsState: RootfsState = RootfsState.Idle,
    val activeSession: TerminalSession? = null,
    val lastMessage: String? = null
)

class TerminalViewModel(
    private val repository: RootfsRepository,
    private val launcher: DebianLauncher
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(rootfsState = if (repository.isReady()) RootfsState.Ready else RootfsState.Idle) }
        }
    }

    fun downloadRootfs() {
        viewModelScope.launch {
            try {
                repository.ensureRootfs { state ->
                    _uiState.update { it.copy(rootfsState = state) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(rootfsState = RootfsState.Error(e.message ?: "Download failed")) }
            }
        }
    }

    fun launchTerminal(client: TerminalSessionClient, onSessionReady: (TerminalSession) -> Unit) {
        viewModelScope.launch {
            try {
                repository.ensureRootfs { state -> _uiState.update { it.copy(rootfsState = state) } }
                if (_uiState.value.rootfsState !is RootfsState.Ready) return@launch

                val session = launcher.createSession(client)
                ProcessManager.register(session.mHandle, session)
                _uiState.update { it.copy(activeSession = session) }
                onSessionReady(session)
            } catch (e: Exception) {
                _uiState.update { it.copy(rootfsState = RootfsState.Error(e.message ?: "Unable to start Debian")) }
            }
        }
    }

    fun stopSession() {
        viewModelScope.launch {
            ProcessManager.killAll()
            val ready = repository.isReady()
            _uiState.update { it.copy(activeSession = null, rootfsState = if (ready) RootfsState.Ready else RootfsState.Idle) }
        }
    }

    fun fixSystem() {
        viewModelScope.launch {
            ProcessManager.killAll()
            repository.reset()
            _uiState.update { TerminalUiState(rootfsState = RootfsState.Idle, lastMessage = "System reset") }
        }
    }

    override fun onCleared() {
        ProcessManager.killAll()
        super.onCleared()
    }
}
