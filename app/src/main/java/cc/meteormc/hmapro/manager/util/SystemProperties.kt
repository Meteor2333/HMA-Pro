package cc.meteormc.hmapro.manager.util

import android.annotation.SuppressLint
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.reflect
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object SystemProperties {
    @SuppressLint("PrivateApi")
    private val CLASS = Class.forName("android.os.SystemProperties")

    fun get(key: String, def: String): String {
        return CLASS.reflect {
            method(
                "get",
                String::class.java,
                String::class.java
            )!!.call(null, key, def)
        }
    }

    fun getInt(key: String, def: Int): Int {
        return CLASS.reflect {
            method(
                "getInt",
                String::class.java,
                Int::class.javaPrimitiveType!!
            )!!.call(null, key, def)
        }
    }

    fun getLong(key: String, def: Long): Long {
        return CLASS.reflect {
            method(
                "getLong",
                String::class.java,
                Long::class.javaPrimitiveType!!
            )!!.call(null, key, def)
        }
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return CLASS.reflect {
            method(
                "getBoolean",
                String::class.java,
                Boolean::class.javaPrimitiveType!!
            )!!.call(null, key, def)
        }
    }

    suspend fun set(key: String, value: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            Shell.exec(
                "setprop $key $value",
                root = true
            ) {
                continuation.resume(it.isSuccess)
            }
        }
    }
}