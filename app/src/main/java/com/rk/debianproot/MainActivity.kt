package com.rk.debianproot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rk.debianproot.data.ProotInstaller
import com.rk.debianproot.data.RootfsRepository
import com.rk.debianproot.terminal.DebianLauncher
import com.rk.debianproot.terminal.TerminalClient
import com.rk.debianproot.ui.TerminalScreen
import com.rk.debianproot.ui.theme.DebianTheme
import com.rk.debianproot.viewmodel.TerminalViewModel
import com.termux.view.TerminalView

class MainActivity : ComponentActivity() {

    private val repository by lazy { RootfsRepository(this) }
    private val prootInstaller by lazy { ProotInstaller(this) }
    private val launcher by lazy { DebianLauncher(repository, prootInstaller) }

    private val viewModel by viewModels<TerminalViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TerminalViewModel(repository, launcher) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val terminalView = TerminalView(this)
        setContent {
            DebianTheme {
                val client = TerminalClient(this, terminalView) {
                    viewModel.stopSession()
                }
                val uiState by viewModel.uiState.collectAsState()
                TerminalScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    terminalView = terminalView,
                    terminalClient = client
                )
            }
        }
    }
}
