package cc.meteormc.hmapro.data

data class LogItem(
    val level: Int,
    val date: String,
    val tag: String,
    val message: String,
    val raw: String
)
