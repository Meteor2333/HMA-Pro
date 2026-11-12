package cc.meteormc.hmapro.manager.util

import android.util.Log
import cc.meteormc.hmapro.manager.data.LogItem
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
        val matcher = pattern.matcher(text)
        if (!matcher.find()) return null

        val level = when (matcher.group(1)?.trim()) {
            null -> null
            "VERBOSE" -> Log.VERBOSE
            "DEBUG" -> Log.DEBUG
            "INFO" -> Log.INFO
            "WARN" -> Log.WARN
            "ERROR" -> Log.ERROR
            "ASSERT" -> Log.ASSERT
            else -> null
        } ?: return null
        val date = matcher.group(2) ?: return null
        val tag = matcher.group(3) ?: return null
        val message = matcher.group(4) ?: return null
        return LogItem(level, date, tag, message, text)
    }
}