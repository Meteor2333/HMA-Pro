package cc.meteormc.hmapro.manager.data

import android.content.Context

data class HMAVersion(
    val fullName: String,
    val simpleName: String,
    val hash: String,
    val code: Long
) {
    companion object {
        fun current(context: Context): HMAVersion {
            val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = pkgInfo.versionName!!
            val versionCode = pkgInfo.longVersionCode
            return HMAVersion(
                fullName = versionName,
                simpleName = versionName.substringBefore(".r"),
                hash = versionName.substringAfterLast('.'),
                code = versionCode
            )
        }
    }
}