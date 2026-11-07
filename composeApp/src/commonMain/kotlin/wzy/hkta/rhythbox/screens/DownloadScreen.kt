package wzy.hkta.rhythbox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.ashcan
import rhythbox.composeapp.generated.resources.close_grey
import rhythbox.composeapp.generated.resources.download_line
import rhythbox.composeapp.generated.resources.pause2
import rhythbox.composeapp.generated.resources.play3
import wzy.hkta.rhythbox.components.NavBar

private enum class DownloadStatus {
    DOWNLOADING, WAITING, COMPLETED, FAILED
}

private data class DownloadItem(
    val id: Long,
    val title: String,
    val artist: String,
    val progress: Float = 0f,
    val speed: String = "",
    val size: String = "",
    val status: DownloadStatus = DownloadStatus.WAITING,
    val paused: Boolean = false
)

class DownloadScreen : Screen {

    @Composable
    override fun Content() {

        var downloads by remember {
            mutableStateOf(
                listOf(
                    DownloadItem(
                        1, "孤勇者", "陈奕迅",
                        progress = 0.65f,
                        speed = "320.00kb/s",
                        size = "16.8M/50.8M",
                        status = DownloadStatus.DOWNLOADING
                    ),
                    DownloadItem(
                        2,
                        "爱情转移",
                        "陈奕迅",
                        status = DownloadStatus.WAITING,
                        size = "期待下载"
                    ),
                    DownloadItem(
                        3,
                        "浮夸",
                        "陈奕迅",
                        status = DownloadStatus.WAITING,
                        size = "期待下载"
                    ),
                    DownloadItem(
                        4,
                        "好久不见",
                        "陈奕迅",
                        status = DownloadStatus.WAITING,
                        size = "期待下载"
                    ),
                    DownloadItem(
                        5,
                        "因为爱情",
                        "陈奕迅 / 王菲",
                        status = DownloadStatus.WAITING,
                        size = "期待下载"
                    ),
                    DownloadItem(
                        6,
                        "红玫瑰",
                        "陈奕迅",
                        status = DownloadStatus.WAITING,
                        size = "期待下载"
                    ),
                    DownloadItem(
                        7,
                        "淘汰",
                        "陈奕迅",
                        status = DownloadStatus.WAITING,
                        size = "期待下载"
                    )
                )
            )
        }

        val downloadingCount =
            downloads.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.WAITING }
        val allPaused =
            downloads.filter { it.status == DownloadStatus.DOWNLOADING }.all { it.paused }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            // 顶部导航栏，右侧带“全部暂停”和“清空”
            NavBar("正在下载")

            // 列表头部计数信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "正在下载 $downloadingCount",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 全部暂停 / 继续
                    Row(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            downloads = downloads.map {
                                if (it.status == DownloadStatus.DOWNLOADING) {
                                    it.copy(paused = !allPaused)
                                } else it
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(if (allPaused) Res.drawable.play3 else Res.drawable.pause2),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (allPaused) "全部继续" else "全部暂停",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 清空按钮
                    Row(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            downloads = emptyList()
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close_grey),
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "清空",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 列表主体
            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无下载任务",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(downloads, key = { it.id }) { item ->
                        SimpleDownloadItem(
                            item = item,
                            onTogglePause = {
                                downloads = downloads.map {
                                    if (it.id == item.id) it.copy(paused = !it.paused) else it
                                }
                            },
                            onDelete = {
                                downloads = downloads.filterNot { it.id == item.id }
                            }
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleDownloadItem(
    item: DownloadItem,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit
) {
    val isDownloading = item.status == DownloadStatus.DOWNLOADING

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧状态图标（下载中显示动态转圈或箭头，等待中显示时钟）
        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = {}
        ) {
            when (isDownloading) {
                true -> Icon(
                    painter = painterResource(Res.drawable.download_line),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                false -> Icon(
                    painter = painterResource(Res.drawable.pause2),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }

        Spacer(Modifier.width(6.dp))

        // 中间歌曲信息与进度条
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, false)
                )

                // 歌手名称（小字靠后或直接省略）
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(4.dp))

            if (isDownloading) {
                // 下载中的进度条
                Column {
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.speed,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.size,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 等待下载状态提示
                Text(
                    text = "待下载",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // 右侧删除按钮
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ashcan),
                contentDescription = "删除",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}