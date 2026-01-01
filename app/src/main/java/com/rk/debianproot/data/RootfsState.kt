package com.rk.debianproot.data

sealed class RootfsState {
    data object Idle : RootfsState()
    data object Ready : RootfsState()
    data class Downloading(val progress: Int, val bytesRead: Long, val totalBytes: Long) : RootfsState()
    data class Extracting(val progress: Int, val currentEntry: String) : RootfsState()
    data class Verifying(val message: String = "Verifying rootfs") : RootfsState()
    data class Error(val message: String) : RootfsState()
}
