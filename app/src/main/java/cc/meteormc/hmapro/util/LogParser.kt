package cc.meteormc.hmapro.util

import cc.meteormc.hmapro.data.LogItem
import cc.meteormc.xposedkit.XLog
import java.util.regex.Pattern

object LogParser {
    private val pattern = Pattern.compile("\\[ ?(.*)] (.*) \\((.*)\\) (.*)", Pattern.DOTALL)

    fun parse(str: String) = buildList {
        val builder = StringBuilder()
        for (line in str.split("\n")) {
            if (line.startsWith('[')) {
                parseItem(builder.toString())?.let { add(it) }
                builder.clear()
            }

            builder.append(line)
        }

        if (builder.isNotEmpty()) {
            parseItem(builder.toString())?.let { add(it) }
        }
    }

    private fun parseItem(text: String): LogItem? {
        XLog.i("LogParser", text)
        val matcher = pattern.matcher(text)
        if (!matcher.find()) return null

        val level = matcher.group(1) ?: return null
        val date = matcher.group(2) ?: return null
        val tag = matcher.group(3) ?: return null
        val message = matcher.group(4) ?: return null
        return LogItem(level, date, tag, message)
    }
}