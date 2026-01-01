package com.rk.debianproot.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class RootfsRepository(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .build()
) {

    private val rootfsUrl = "https://www.dropbox.com/s/zxfg8aosr7zzmg8/arm64-rootfs-20170318T102424Z.tar.gz?dl=1"

    private val filesDir = context.filesDir
    val downloadsDir: File = File(filesDir, "downloads")
    val tarball: File = File(downloadsDir, "rootfs.tar.gz")
    val rootfsDir: File = File(filesDir, "debian-rootfs")

    suspend fun isReady(): Boolean = withContext(Dispatchers.IO) {
        File(rootfsDir, "bin/bash").exists()
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        tarball.parentFile?.mkdirs()
        if (tarball.exists()) tarball.delete()
        rootfsDir.deleteRecursively()
        File(filesDir, "bin").deleteRecursively()
    }

    suspend fun ensureRootfs(onProgress: (RootfsState) -> Unit) {
        if (isReady()) {
            onProgress(RootfsState.Ready)
            return
        }

        downloadsDir.mkdirs()
        rootfsDir.mkdirs()

        downloadRootfs(onProgress)
        extractRootfs(onProgress)
        validateRootfs(onProgress)
    }

    private suspend fun downloadRootfs(onProgress: (RootfsState) -> Unit) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(rootfsUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Download failed: ${response.code}")
            val body = response.body ?: throw IOException("Empty response body")
            val total = body.contentLength().takeIf { it > 0 } ?: -1
            tarball.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read: Int
                var bytesRead = 0L
                val input = body.byteStream()
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    bytesRead += read
                    val percent = if (total > 0) ((bytesRead * 100) / total).toInt() else -1
                    onProgress(RootfsState.Downloading(percent, bytesRead, total))
                }
            }
            if (total > 0 && tarball.length() != total) {
                throw IOException("Downloaded size mismatch: expected $total, got ${tarball.length()}")
            }
        }
    }

    private suspend fun extractRootfs(onProgress: (RootfsState) -> Unit) = withContext(Dispatchers.IO) {
        rootfsDir.deleteRecursively()
        rootfsDir.mkdirs()

        FileInputStream(tarball).use { fileInput ->
            GzipCompressorInputStream(fileInput).use { gzipInput ->
                TarArchiveInputStream(gzipInput).use { tarInput ->
                    var entry = tarInput.nextTarEntry
                    var processed = 0
                    val estimatedEntries = 1600 // fallback for progress
                    while (entry != null) {
                        val target = File(rootfsDir, entry.name)
                        if (entry.isDirectory) {
                            target.mkdirs()
                        } else {
                            target.parentFile?.mkdirs()
                            FileOutputStream(target).use { output ->
                                tarInput.copyTo(output)
                            }
                            target.setExecutable(entry.mode and 0b001000000 != 0)
                        }
                        processed++
                        val percent = ((processed * 100f) / estimatedEntries).toInt().coerceAtMost(100)
                        onProgress(RootfsState.Extracting(percent, entry.name))
                        entry = tarInput.nextTarEntry
                    }
                }
            }
        }
    }

    private suspend fun validateRootfs(onProgress: (RootfsState) -> Unit) = withContext(Dispatchers.IO) {
        onProgress(RootfsState.Verifying())
        val bash = File(rootfsDir, "bin/bash")
        val etc = File(rootfsDir, "etc")
        if (!bash.exists()) throw IOException("Rootfs missing /bin/bash")
        ensureNetworkingFiles(etc)
        onProgress(RootfsState.Ready)
    }

    private fun ensureNetworkingFiles(etcDir: File) {
        val resolvConf = File(etcDir, "resolv.conf")
        resolvConf.writeText("nameserver 8.8.8.8\n")
        val hosts = File(etcDir, "hosts")
        hosts.writeText("127.0.0.1 localhost\n::1 localhost\n")
    }
}
