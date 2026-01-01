package com.rk.debianproot.terminal

import android.os.Environment
import com.rk.debianproot.data.ProotInstaller
import com.rk.debianproot.data.RootfsRepository
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import java.io.File

class DebianLauncher(
    private val repository: RootfsRepository,
    private val installer: ProotInstaller
) {

    suspend fun createSession(client: TerminalSessionClient): TerminalSession {
        val proot = installer.ensureProot()
        val rootfs = repository.rootfsDir
        val workingDir = File(rootfs, "root")
        workingDir.mkdirs()

        val sdcardBind = "${Environment.getExternalStorageDirectory().absolutePath}:/mnt/sdcard"

        val args = buildList {
            addAll(listOf("--link2symlink", "-0"))
            addAll(listOf("-r", rootfs.absolutePath))
            addAll(listOf("-b", "/dev"))
            addAll(listOf("-b", "/proc"))
            addAll(listOf("-b", "/sys"))
            addAll(listOf("-b", sdcardBind))
            addAll(listOf("-w", "/root"))
            add("/usr/bin/env")
            add("-i")
            add("HOME=/root")
            add("TERM=xterm-256color")
            add("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin")
            add("LANG=en_US.UTF-8")
            add("LC_ALL=en_US.UTF-8")
            add("/bin/bash")
            add("--login")
        }

        val env = arrayOf(
            "PROOT_TMP_DIR=${File(repository.downloadsDir, "tmp").apply { mkdirs() }.absolutePath}",
            "HOME=/root",
            "TERM=xterm-256color",
            "LANG=en_US.UTF-8",
            "LC_ALL=en_US.UTF-8",
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
        )

        return TerminalSession(
            proot.absolutePath,
            workingDir.absolutePath,
            args.toTypedArray(),
            env,
            TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
            client
        )
    }
}
