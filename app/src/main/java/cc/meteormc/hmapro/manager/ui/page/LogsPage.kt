package cc.meteormc.hmapro.manager.ui.page

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.data.LogItem
import cc.meteormc.hmapro.manager.service.ServiceClient
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.weight.CancelSearchButton
import cc.meteormc.hmapro.manager.util.LogParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

private object LogCache {
    var raw = ""
    var parsed = emptyList<LogItem>()
}

@Composable
fun LogsPage() {
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    var logs by remember { mutableStateOf(LogCache.parsed) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableIntStateOf(0) } // todo: 持久化
    suspend fun updateLogs() = withContext(Dispatchers.IO) {
        val raw = ServiceClient.getLogs().orEmpty()
        logs = LogParser.parse(raw)
        isRefreshing = false

        LogCache.raw = raw
        LogCache.parsed = logs
    }

    LaunchedEffect(Unit) {
        delay(500.milliseconds)
        updateLogs()
    }

    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val dropdownItems = remember(filter) {
        listOf(
            DropdownItem(
                text = "ALL",
                selected = filter == 0,
                onClick = { filter = 0 }
            ),
            DropdownItem(
                text = "VERBOSE",
                selected = filter == Log.VERBOSE,
                onClick = { filter = Log.VERBOSE }
            ),
            DropdownItem(
                text = "DEBUG",
                selected = filter == Log.DEBUG,
                onClick = { filter = Log.DEBUG }
            ),
            DropdownItem(
                text = "INFO",
                selected = filter == Log.INFO,
                onClick = { filter = Log.INFO }
            ),
            DropdownItem(
                text = "WARNING",
                selected = filter == Log.WARN,
                onClick = { filter = Log.WARN }
            ),
            DropdownItem(
                text = "ERROR",
                selected = filter == Log.ERROR,
                onClick = { filter = Log.ERROR }
            ),
            DropdownItem(
                text = "ASSERT",
                selected = filter == Log.ASSERT,
                onClick = { filter = Log.ASSERT }
            )
        )
    }

    val failedMessage = stringResourceBy("home_export_failed")
    val successfulMessage = stringResourceBy("logs_saved")
    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/x-log")
    ) {
        if (it == null) return@rememberLauncherForActivityResult
        context.contentResolver.openOutputStream(it).use { output ->
            if (output == null) {
                Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
                return@use
            }

            output.write(LogCache.raw.toByteArray())
            Toast.makeText(context, successfulMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResourceBy("title_logs"),
                scrollBehavior = scrollBehavior,
                actions = {
                    OverlayIconDropdownMenu(
                        entry = DropdownEntry(dropdownItems)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Filter,
                            contentDescription = "Filter"
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    ServiceClient.clearLogs()
                                }

                                updateLogs()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Clear,
                            contentDescription = "Clear Logs"
                        )
                    }

                    IconButton(
                        onClick = {
                            val millis = System.currentTimeMillis()
                            val date = SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.getDefault()).format(millis)
                            safLauncher.launch("hma_logs_$date.log")
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.MoveFile,
                            contentDescription = "Save"
                        )
                    }
                },
                defaultWindowInsetsPadding = false
            ) {
                LogsSearchBar(
                    logs = logs,
                    onQuery = { query = it.trim() }
                )
            }
        }
    ) { paddingValues ->
        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch { updateLogs() }
            },
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            pullToRefreshState = refreshState,
            refreshTexts = listOf(
                stringResourceFromModule(R.string.pull_to_refresh),
                stringResourceFromModule(R.string.release_to_refresh),
                stringResourceFromModule(R.string.refreshing),
                stringResourceFromModule(R.string.refreshed)
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollEffect(scrollBehavior),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                val items = logs.toMutableList()
                if (query.isNotBlank()) {
                    items.removeAll {
                        it.raw.contains(query, ignoreCase = true)
                    }
                }

                if (filter > 0) {
                    items.removeAll {
                        it.level != filter
                    }
                }

                items(
                    items = items,
                    key = { it.toString() }
                ) {
                    LogItemComponent(it)
                }
            }
        }
    }
}

@Composable
private fun LogsSearchBar(
    logs: List<LogItem>,
    onQuery: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    SearchBar(
        inputField = {
            InputField(
                query = query,
                onQueryChange = {
                    query = it
                    onQuery(it)
                },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                label = stringResourceBy("search")
            )
        },
        modifier = Modifier.padding(vertical = 12.dp),
        onExpandedChange = { expanded = it },
        expanded = expanded,
        outsideEndAction = {
            CancelSearchButton {
                query = ""
                expanded = false
                onQuery("")
            }
        }
    ) {

    }
}

@Composable
private fun LogItemComponent(item: LogItem) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val copiedMessage = stringResourceFromModule(R.string.log_copied)
    Card(
        modifier = Modifier.fillMaxWidth(),
        onLongPress = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            val entry = ClipEntry(
                ClipData.newPlainText("log", item.raw)
            )
            scope.launch {
                clipboard.setClipEntry(entry)
                Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
            }
        },
        showIndication = true
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(16.dp),
                    cornerRadius = 4.dp,
                    colors = CardDefaults.defaultColors(
                        color = when (item.level) {
                            Log.VERBOSE -> Color(0xFFD6D6D6)
                            Log.DEBUG -> Color(0xFF305D78)
                            Log.INFO -> Color(0xFF6A8759)
                            Log.WARN -> Color(0xFFBBB529)
                            Log.ERROR -> Color(0xFFCF5B56)
                            Log.ASSERT -> Color(0xFF8B3C3C)
                            else -> Color.White
                        }
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (item.level) {
                                Log.VERBOSE -> "V"
                                Log.DEBUG -> "D"
                                Log.INFO -> "I"
                                Log.WARN -> "W"
                                Log.ERROR -> "E"
                                Log.ASSERT -> "A"
                                else -> "?"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = item.tag,
                    modifier = Modifier.basicMarquee(),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.date,
                color = MiuixTheme.colorScheme.onSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.message,
                fontSize = 15.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}