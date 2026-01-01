package com.rk.debianproot.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProotInstaller(private val context: Context) {

    private val binDir = File(context.filesDir, "bin")
    private val prootBinary = File(binDir, "proot")

    suspend fun ensureProot(): File = withContext(Dispatchers.IO) {
        if (prootBinary.exists()) return@withContext prootBinary

        binDir.mkdirs()
        if (!copyFromNativeLibs() && !copyFromAssets()) {
            throw IllegalStateException("No bundled proot binary found")
        }

        prootBinary.setExecutable(true)
        prootBinary
    }

    private fun copyFromAssets(): Boolean {
        return try {
            context.assets.open("proot").use { input ->
                FileOutputStream(prootBinary).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun copyFromNativeLibs(): Boolean {
        val libDir = File(context.applicationInfo.nativeLibraryDir)
        val loader = libDir.listFiles()?.firstOrNull { it.name.startsWith("libproot-loader") }
        if (loader != null) {
            loader.copyTo(prootBinary, overwrite = true)
            // Keep a copy alongside to satisfy the asset shim if needed
            loader.copyTo(File(binDir, loader.name), overwrite = true)
            return true
        }
        return false
    }
}
