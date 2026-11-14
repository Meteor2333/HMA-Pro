package cc.meteormc.hmapro.manager.data

import cc.meteormc.xposedkit.XLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

data class UpdateInfo(
    val version: String,
    val changelog: String,
    val releaseUrl: String,
    val asset: Asset?
) {
    companion object {
        private const val TAG = "UpdateInfo"
        private const val UPDATE_URL = "https://api.github.com/repos/Dr-TSNG/Hide-My-Applist/releases/latest"

        suspend fun checkUpdate(currentVerison: HMAVersion) = withContext(Dispatchers.IO) {
            val connection = URL(UPDATE_URL).openConnection() as HttpURLConnection
            val json = try {
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val code = connection.responseCode
                if (code != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP $code")
                }

                connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }
                    .let { JSONObject(it) }
            } catch (t: Throwable) {
                XLog.w(TAG, "Failed to check for updates", t)
                return@withContext null
            } finally {
                connection.disconnect()
            }

            var asset: Asset? = null
            val assets = json.optJSONArray("assets")
            val latestVersion = json.optString("tag_name").takeIf { it.isNotEmpty() } ?: return@withContext null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val item = assets.optJSONObject(i) ?: continue
                    val name = item.optString("name")
                    asset = Asset(
                        name,
                        item.optString("browser_download_url"),
                        item.optString("digest")
                    )

                    if (!name.startsWith("HMA") && !name.endsWith(".apk")) continue

                    val versionCode = name
                        .substringAfter(".r").substringBefore('.')
                        .toLongOrNull()
                        ?: continue
                    if (currentVerison.code < versionCode) {
                        break
                    } else {
                        return@withContext null
                    }
                }
            } else if (!isNewerVersion(currentVerison.simpleName, latestVersion)) {
                return@withContext null
            }

            UpdateInfo(
                latestVersion,
                json.optString("body"),
                json.optString("html_url"),
                asset
            )
        }

        private fun isNewerVersion(current: String, other: String): Boolean {
            val currentArr = current.split('.')
            val otherArr = other.split('.')
            for (i in 0 until max(currentArr.size, otherArr.size)) {
                val n1 = currentArr.getOrNull(i)?.toIntOrNull() ?: 0
                val n2 = otherArr.getOrNull(i)?.toIntOrNull() ?: 0
                if (n1 > n2) return false
                if (n1 < n2) return true
            }

            return false
        }
    }

    data class Asset(
        val name: String,
        val downloadUrl: String,
        val digest: String
    )
}