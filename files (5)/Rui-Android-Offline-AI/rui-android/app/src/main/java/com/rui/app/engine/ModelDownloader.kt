package com.rui.app.engine

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/** One-time model download with resume. After this, Rui never needs the internet again. */
class ModelDownloader(context: Context) {
    private val dir = File(context.filesDir, "models").apply { mkdirs() }

    suspend fun download(spec: ModelSpec, onProgress: (done: Long, total: Long) -> Unit): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val out = File(dir, spec.fileName)
                if (out.exists() && out.length() > 0) return@runCatching out
                val part = File(dir, spec.fileName + ".part")
                var existing = if (part.exists()) part.length() else 0L

                val conn = (URL(spec.url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Rui-Android")
                    if (existing > 0) setRequestProperty("Range", "bytes=$existing-")
                }
                try {
                    val code = conn.responseCode
                    require(code == 200 || code == 206) { "Download server answered $code. Try again later." }
                    val resuming = code == 206
                    if (!resuming) { existing = 0; part.delete() }
                    val remaining = conn.contentLengthLong
                    val total = if (remaining > 0) existing + remaining else spec.approxBytes
                    require(StatFs(dir.path).availableBytes > (total - existing) + 200L * 1024 * 1024) {
                        "Not enough free storage (need about ${(total shr 20)} MB)."
                    }
                    conn.inputStream.use { input ->
                        FileOutputStream(part, resuming).use { o ->
                            val buf = ByteArray(1 shl 18)
                            var done = existing
                            while (true) {
                                coroutineContext.ensureActive()
                                val n = input.read(buf)
                                if (n < 0) break
                                o.write(buf, 0, n)
                                done += n
                                onProgress(done, total)
                            }
                            o.fd.sync()
                        }
                    }
                } finally {
                    conn.disconnect()
                }
                check(part.length() > 50L * 1024 * 1024) { "Download was incomplete. Try again." }
                if (out.exists()) out.delete()
                check(part.renameTo(out)) { "Could not save the model." }
                out
            }
        }
}
