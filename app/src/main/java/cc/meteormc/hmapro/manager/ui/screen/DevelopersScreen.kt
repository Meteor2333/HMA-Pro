package cc.meteormc.hmapro.manager.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.meteormc.hmapro.manager.ui.util.painterResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.weight.BackButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class Developer(
    val name: String,
    val description: String,
    val avatarRes: String,
    val link: String
)

private val developers = listOf(
    Developer(
        "\ud835\udd93\ud835\udd9a\ud835\udd91\ud835\udd91\ud835\udd95\ud835\udd99\ud835\udd97",
        "Developer",
        "cont_author",
        "https://github.com/Dr-TSNG"
    ),
    Developer(
        "Ketal",
        "Collaborator",
        "cont_k",
        "https://github.com/keta1"
    ),
    Developer(
        "5ec1cff",
        "Collaborator",
        "cont_5ec1cff",
        "https://github.com/5ec1cff"
    ),
    Developer(
        "aviraxp",
        "Collaborator",
        "cont_aviraxp",
        "https://github.com/aviraxp"
    ),
    Developer(
        "辉少菌",
        "Icon Designer",
        "cont_icon_designer",
        "http://www.coolapk.com/u/1560270"
    ),
    Developer(
        "LoveSy",
        "Idea Provider",
        "cont_cpp_master",
        "https://github.com/yujincheng08"
    )
)

@Composable
fun DevelopersScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("about_developer"),
                navigationIcon = { BackButton() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .verticalScrollEffect(),
            contentPadding = paddingValues
        ) {
            items(
                items = developers,
                key = { it.name }
            ) {
                DeveloperCard(it)
            }
        }
    }
}

@Composable
private fun DeveloperCard(developer: Developer) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        onClick = { uriHandler.openUri(developer.link) },
        showIndication = true
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResourceBy(developer.avatarRes),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(CardDefaults.CornerRadius))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = developer.name,
                    modifier = Modifier.basicMarquee(),
                    fontSize = 18.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = developer.description,
                    color = MiuixTheme.colorScheme.onSecondary,
                    maxLines = 1
                )
            }
        }
    }
}