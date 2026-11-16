package cc.meteormc.hmapro.manager.data

data class UpdateInfo(
    val version: String,
    val changelog: String,
    val releaseUrl: String,
    val asset: Asset?
) {
    data class Asset(
        val name: String,
        val downloadUrl: String,
        val digest: String
    )
}