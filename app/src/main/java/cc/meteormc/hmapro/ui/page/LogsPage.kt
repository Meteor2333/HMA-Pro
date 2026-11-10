package cc.meteormc.hmapro.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.data.LogItem
import cc.meteormc.hmapro.service.ServiceClient
import cc.meteormc.hmapro.ui.util.stringResourceBy
import cc.meteormc.hmapro.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.util.LogParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.MoveFile

@Composable
fun LogsPage() {
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    var logs by remember { mutableStateOf<List<LogItem>>(emptyList()) }
    fun updateLogs() {
        scope.launch {
            logs = withContext(Dispatchers.IO) {
                try {
                    val raw = ServiceClient.getLogs().orEmpty()
                    LogParser.parse(raw)
                } finally {
                    isRefreshing = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        updateLogs()
    }

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResourceBy("title_logs"),
                scrollBehavior = scrollBehavior,
                actions = {
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

                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.MoveFile,
                            contentDescription = "Save Logs"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            PullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    updateLogs()
                },
                modifier = Modifier.padding(paddingValues),
                pullToRefreshState = refreshState
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScrollEffect(scrollBehavior),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(
                        items = logs,
                        key = { it.toString() }
                    ) {
                        LogItemComponent(it)
                    }
                }
            }
        }
    )
}

@Composable
private fun LogItemComponent(item: LogItem) {
    Text(
        """
            level=${item.level}
            date=${item.date}
            tag=${item.tag}
            message=${item.message}
        """.trimIndent()
    )

    Spacer(modifier = Modifier.height(8.dp))
}