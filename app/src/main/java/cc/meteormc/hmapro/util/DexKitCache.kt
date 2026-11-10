package cc.meteormc.hmapro.util

import android.annotation.SuppressLint
import cc.meteormc.hmapro.HmaProModule
import cc.meteormc.xposedkit.util.AtomicBooleanDelegate
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.annotations.DexKitExperimentalApi
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@OptIn(DexKitExperimentalApi::class)
object DexKitCache : DexKitCacheBridge.Cache {
    private const val MAGIC = 0x444B43
    private const val VERSION = 1
    @SuppressLint("SdCardPath")
    private const val CACHE_FILE = "/data/data/${HmaProModule.HMA_PACKAGE}/files/hmapro_cache.dat"

    private var dirty by AtomicBooleanDelegate(false)
    private val single = ConcurrentHashMap<String, String>()
    private val list = ConcurrentHashMap<String, List<String>>()

    private val file by lazy {
        val file = File(CACHE_FILE)
        if (!file.exists() || file.length() > 10 * 1024 * 1024) {
            try {
                // 创建文件或清空文件
                file.writeBytes(ByteArray(0))
            } catch (e: IOException) {
                throw IllegalStateException("Failed to create DexKit cache file!", e)
            }
        }

        return@lazy file
    }

    fun load() {
        if (file.length() <= 0) return
        DataInputStream(file.inputStream()).use { input ->
            if (input.readInt() != MAGIC) {
                throw IllegalStateException("DexKit cache file has invalid magic number, invalidating it")
            }

            if (input.readInt() != VERSION) {
                throw IllegalStateException("DexKit cache file has invalid version, invalidating it")
            }

            single.clear()
            repeat(input.readInt()) {
                val key = input.readUTF()
                val value = input.readUTF()
                single[key] = value
            }

            list.clear()
            repeat(input.readInt()) {
                val key = input.readUTF()
                val size = input.readInt()
                val value = ArrayList<String>(size)
                repeat(size) { value.add(input.readUTF()) }
                list[key] = value
            }
        }
    }

    fun save() {
        DataOutputStream(file.outputStream()).use { output ->
            output.writeInt(MAGIC)
            output.writeInt(VERSION)

            output.writeInt(single.size)
            for ((key, value) in single) {
                output.writeUTF(key)
                output.writeUTF(value)
            }

            output.writeInt(list.size)
            for ((key, value) in list) {
                output.writeUTF(key)
                output.writeInt(value.size)
                value.forEach { output.writeUTF(it) }
            }
        }
    }

    fun trySave() {
        if (!dirty) return
        Thread { save() }.start()
    }

    override fun getString(key: String, default: String?): String? {
        return single[key] ?: default
    }

    override fun putString(key: String, value: String) {
        dirty = true
        single[key] = value
    }

    override fun getStringList(key: String, default: List<String>?): List<String>? {
        return list[key] ?: default
    }

    override fun putStringList(key: String, value: List<String>) {
        dirty = true
        list[key] = value
    }

    override fun remove(key: String) {
        dirty = true
        single.remove(key)
        list.remove(key)
    }

    override fun getAllKeys(): Collection<String> {
        return single.keys + list.keys
    }

    override fun clearAll() {
        dirty = true
        single.clear()
        list.clear()
    }
}