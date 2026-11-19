package cc.meteormc.hmapro.manager.data

import android.graphics.drawable.Drawable

data class InstalledPackage(
    val label: String,
    val icon: Drawable,
    val packageName: String,
    val isSystemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
)