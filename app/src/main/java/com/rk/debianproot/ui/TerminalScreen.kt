package com.rk.debianproot.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rk.debianproot.data.RootfsState
import com.rk.debianproot.terminal.TerminalClient
import com.rk.debianproot.viewmodel.TerminalUiState
import com.rk.debianproot.viewmodel.TerminalViewModel
import com.termux.view.TerminalView

@Composable
fun TerminalScreen(
    modifier: Modifier = Modifier,
    uiState: TerminalUiState,
    viewModel: TerminalViewModel,
    terminalView: TerminalView,
    terminalClient: TerminalClient,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.activeSession) {
        uiState.activeSession?.let { session ->
            terminalView.setTerminalViewClient(terminalClient)
            terminalView.attachSession(session)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Debian rootfs", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                when (val state = uiState.rootfsState) {
                    RootfsState.Idle -> Text(text = "Ready to download.")
                    RootfsState.Ready -> Text(text = "Ready.")
                    is RootfsState.Downloading -> ProgressRow("Downloading", state.progress, state.bytesRead, state.totalBytes)
                    is RootfsState.Extracting -> ProgressRow("Extracting ${state.currentEntry}", state.progress)
                    is RootfsState.Verifying -> Text(text = state.message)
                    is RootfsState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedButton(onClick = { viewModel.downloadRootfs() }) {
                        Text(text = "Download")
                    }
                    OutlinedButton(onClick = { viewModel.fixSystem() }) {
                        Text(text = "Fix system")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Terminal", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                AndroidTerminalContainer(terminalView)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedButton(onClick = {
                        viewModel.launchTerminal(terminalClient) {}
                    }) { Text("Launch Debian") }
                    TextButton(onClick = { viewModel.stopSession() }) {
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressRow(label: String, progress: Int, read: Long = 0, total: Long = -1) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
        Column {
            Text(text = label)
            if (progress >= 0) {
                Text(text = "$progress% (${humanBytes(read)} / ${if (total > 0) humanBytes(total) else "unknown"})")
            }
        }
    }
}

@Composable
private fun AndroidTerminalContainer(terminalView: TerminalView) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { terminalView.apply { requestFocus() } },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    )
}

private fun humanBytes(size: Long): String {
    if (size <= 0) return "0B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f%s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
