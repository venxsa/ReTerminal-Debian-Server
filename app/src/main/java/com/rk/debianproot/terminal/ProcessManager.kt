package com.rk.debianproot.terminal

import android.os.Process
import com.termux.terminal.TerminalSession
import java.util.concurrent.ConcurrentHashMap

object ProcessManager {
    private val sessions = ConcurrentHashMap<String, TerminalSession>()
    private val pids = ConcurrentHashMap<String, Int>()

    fun register(id: String, session: TerminalSession) {
        sessions[id] = session
    }

    fun setPid(id: String, pid: Int) {
        pids[id] = pid
    }

    fun remove(id: String) {
        sessions.remove(id)
        pids.remove(id)
    }

    fun killAll() {
        sessions.values.forEach { it.finishIfRunning() }
        pids.values.forEach { pid ->
            runCatching { Process.killProcess(pid) }
        }
        sessions.clear()
        pids.clear()
    }
}
