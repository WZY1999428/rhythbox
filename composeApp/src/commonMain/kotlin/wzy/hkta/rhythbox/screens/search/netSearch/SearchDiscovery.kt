package wzy.hkta.rhythbox.screens.search.netSearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.champions
import rhythbox.composeapp.generated.resources.close_grey
import rhythbox.composeapp.generated.resources.delete
import rhythbox.composeapp.generated.resources.recently


@Composable
fun SearchDiscovery(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        SearchHistory(onSearch = onSearch)
        HotKeyword(onSearch = onSearch)
    }
}

@Composable
fun SearchHistory(
    onSearch: (String) -> Unit = {}
) {
    var history by remember {
        mutableStateOf(
            listOf("周杰伦", "林俊杰", "晴天", "稻香", "告白气球")
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.recently),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "搜索历史",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { history = emptyList() }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.delete),
                        contentDescription = "清空搜索历史",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (history.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                history.forEach { keyword ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                                    .copy(alpha = 0.55f)
                            )
                            .clickable { onSearch(keyword) }
                            .padding(
                                horizontal = 12.dp,
                                vertical = 9.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = keyword,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.width(6.dp))

                        Icon(
                            painter = painterResource(Res.drawable.close_grey),
                            contentDescription = "删除 $keyword",
                            modifier = Modifier
                                .size(12.dp)
                                .clickable {
                                    history = history - keyword
                                },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HotKeyword(
    onSearch: (String) -> Unit = {}
) {
    val keywords = listOf(
        "周杰伦",
        "热门新歌",
        "华语精选",
        "林俊杰",
        "轻音乐",
        "独立音乐",
        "陈奕迅",
        "纯音乐",
        "经典老歌",
        "治愈系"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.champions),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp)
            )

            Text(
                text = "热门搜索",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            keywords.forEachIndexed { index, keyword ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (index < 3) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                                    .copy(alpha = 0.55f)
                            }
                        )
                        .clickable { onSearch(keyword) }
                        .padding(
                            horizontal = 13.dp,
                            vertical = 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (index < 3) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.width(7.dp))
                    }

                    Text(
                        text = keyword,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (index < 3) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}