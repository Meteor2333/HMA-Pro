package cc.meteormc.hmapro.manager.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AtomicFile
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.xposedkit.XLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

object AppMetaLoader {
    private const val TAG = "AppMetaLoader"
    private const val MAX_CACHE_SIZE = 128 * 1024 * 1024 // 128MiB
    private const val CACHE_HEADER = 0x4D455441
    private const val CACHE_VERSION = 1
    private val CACHE_FILE = File(ManagerMain.CACHE_DIR, "app_meta.cache")

    private var dirty = false
    private val lock = CompletableDeferred<Unit>()
    private val cache = LruCache<String, MetaCache>(500)
    private val executor = Executors.newSingleThreadExecutor()

    private data class MetaCache(
        val updateTime: Long,
        val label: String?,
        val icon: Bitmap?
    )

    init {
        executor.execute {
            if (!CACHE_FILE.exists() || CACHE_FILE.length() > MAX_CACHE_SIZE) {
                executor.shutdown()
                lock.complete(Unit)
                return@execute
            }

            try {
                DataInputStream(CACHE_FILE.inputStream().buffered()).use { input ->
                    if (input.readInt() != CACHE_HEADER) {
                        XLog.w(TAG, "Invalid cache header, skipping cache load")
                        return@use
                    }

                    if (input.readInt() != CACHE_VERSION) {
                        XLog.w(TAG, "Cache version mismatch, invalidating")
                        return@use
                    }

                    for (i in 0 until input.readInt()) {
                        val packageName = input.readUTF()
                        val updateTime = input.readLong()
                        val label = if (input.readBoolean()) input.readUTF() else null
                        val icon = if (input.readBoolean()) {
                            val length = input.readInt()
                            val bytes = ByteArray(length)
                            input.readFully(bytes)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else null
                        cache[packageName] = MetaCache(updateTime, label, icon)
                    }
                }
            } catch (e: Exception) {
                XLog.w(TAG, "Cache file is broken", e)
            } finally {
                executor.shutdown()
            }

            lock.complete(Unit)
        }
    }

    suspend fun saveCache() {
        if (!dirty) return
        dirty = false

        withContext(Dispatchers.IO) {
            CACHE_FILE.parentFile?.mkdirs()
            AtomicFile(CACHE_FILE).use {
                val output = DataOutputStream(it.buffered())
                output.writeInt(CACHE_HEADER)
                output.writeInt(CACHE_VERSION)

                output.writeInt(cache.size())
                for ((packageName, cachedMeta) in cache.snapshot()) {
                    output.writeUTF(packageName)
                    output.writeLong(cachedMeta.updateTime)

                    val label = cachedMeta.label
                    if (label != null) {
                        output.writeBoolean(true)
                        output.writeUTF(label)
                    } else {
                        output.writeBoolean(false)
                    }

                    val icon = cachedMeta.icon
                    if (icon != null) {
                        output.writeBoolean(true)
                        val stream = ByteArrayOutputStream()
                        icon.compress(
                            Bitmap.CompressFormat.WEBP_LOSSLESS,
                            100,
                            stream
                        )

                        val bytes = stream.toByteArray()
                        output.writeInt(bytes.size)
                        output.write(bytes)
                    } else {
                        output.writeBoolean(false)
                    }
                }

                output.flush()
            }
        }
    }

    suspend fun loadLabel(
        pm: PackageManager,
        appInfo: ApplicationInfo,
        updateTime: Long
    ): String {
        return loadFromCache(
            packageName = appInfo.packageName,
            updateTime = updateTime,
            provider = { label },
            updater = { copy(label = it) },
            loader = {
                withContext(Dispatchers.IO) {
                    appInfo.loadLabel(pm).toString()
                }
            }
        )
    }

    suspend fun loadUnbadgedIcon(
        pm: PackageManager,
        appInfo: ApplicationInfo,
        updateTime: Long
    ): Bitmap {
        return loadFromCache(
            packageName = appInfo.packageName,
            updateTime = updateTime,
            provider = { icon },
            updater = { copy(icon = it) },
            loader = {
                withContext(Dispatchers.IO) {
                    appInfo.loadUnbadgedIcon(pm).toBitmap()
                }
            }
        )
    }

    private suspend inline fun <T> loadFromCache(
        packageName: String,
        updateTime: Long,
        provider: MetaCache.() -> T?,
        updater: MetaCache.(T) -> MetaCache,
        loader: suspend () -> T
    ): T {
        lock.await()

        val meta = cache[packageName]
            ?.takeIf { it.updateTime == updateTime }
            ?: MetaCache(updateTime, null, null)
        var cached = provider(meta)
        if (cached != null) {
            return cached
        }

        cached = loader()
        cache[packageName] = updater(meta, cached)
        dirty = true
        return cached
    }

    private operator fun <K, V> LruCache<K, V>.set(key: K, value: V) {
        put(key, value)
    }

    private inline fun <R> AtomicFile.use(block: (FileOutputStream) -> R): R {
        val output = startWrite()
        try {
            val result = block(output)
            finishWrite(output)
            return result
        } catch (e: Exception) {
            failWrite(output)
            throw e
        }
    }
}