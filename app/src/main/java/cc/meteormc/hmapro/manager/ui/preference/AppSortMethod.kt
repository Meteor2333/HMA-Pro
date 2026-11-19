package cc.meteormc.hmapro.manager.ui.preference

import cc.meteormc.hmapro.manager.data.InstalledPackage
import java.text.Collator

enum class AppSortMethod(val comparator: Comparator<InstalledPackage>) {
    BY_LABEL(
        Comparator { o1, o2 ->
            collator.compare(o1.label, o2.label)
        }
    ),
    BY_PACKAGE_NAME(
        Comparator { o1, o2 ->
            val n1 = o1.packageName
            val n2 = o2.packageName
            n1.compareTo(n2, ignoreCase = true)
        }
    ),
    BY_INSTALL_TIME(
        compareByDescending {
            it.firstInstallTime
        }
    ),
    BY_UPDATE_TIME(
        compareByDescending {
            it.lastUpdateTime
        }
    );

    private companion object {
        val collator = Collator.getInstance()!!.apply {
            strength = Collator.PRIMARY
            decomposition = Collator.CANONICAL_DECOMPOSITION
        }
    }
}