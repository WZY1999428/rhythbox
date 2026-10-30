package wzy.hkta.rhythbox.screens.download

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linroid.ketch.api.DownloadState
import com.linroid.ketch.api.DownloadTask
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.ashcan
import rhythbox.composeapp.generated.resources.play
import rhythbox.composeapp.generated.resources.recently
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun DownloadTaskItem(
    task: DownloadTask,
    state: DownloadState,
    completed: Boolean,
    onItemClick: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    val fileName = task.outputPath
        ?.substringAfterLast('/')
        ?: task.taskId

    // 计算百分比
    val progress = when (state) {
        is DownloadState.Downloading -> state.progress.percent.coerceIn(0f, 100f)
        else -> if (completed) 100f else 0f
    }

    // 解析已下载大小 / 总大小 与 实时网速
    val downloadedBytes = if (state is DownloadState.Downloading) state.progress.downloadedBytes else 0L
    val totalBytes = if (state is DownloadState.Downloading) state.progress.totalBytes else 0L
    val speedBytesPerSecond = if (state is DownloadState.Downloading) state.progress.totalBytes else 0L

    val downloadedStr = formatFileSize(downloadedBytes)
    val totalStr = formatFileSize(totalBytes)
    val speedStr = formatFileSize(speedBytesPerSecond)

    // 状态文案
    val statusText = when {
        state is DownloadState.Downloading -> if (speedStr.isNotEmpty()) "$speedStr/s" else "下载中"
        state is DownloadState.Paused -> "已暂停"
        state is DownloadState.Queued || state is DownloadState.Scheduled -> "待下载"
        state is DownloadState.Failed -> "下载失败"
        state is DownloadState.Completed || completed -> "已完成"
        state is DownloadState.Canceled -> "已取消"
        else -> "等待中"
    }

    val isDownloading = state is DownloadState.Downloading

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 左侧状态图标 (下载中 vs 等待时钟)
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isDownloading) {
                    // 下载中图标 (可用自带图标或资源图标替代)
                    Icon(
                        painter = painterResource(Res.drawable.play),
                        contentDescription = "下载中",
                        tint = Color(0xFF6A8E68),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    // 等待/时钟图标
                    Icon(
//                        imageVector = Icons.Default.Info,
                        painter = painterResource(Res.drawable.recently),
                        contentDescription = "待下载",
                        tint = Color(0xFFB0B2AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. 中间歌曲信息与进度条
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = fileName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 如果正在下载，展示纤细进度条
                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFF6A8E68),
                        trackColor = Color(0xFFE2EFE0),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 底部状态或网速、大小详情
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = if (isDownloading) Color(0xFFE59838) else Color(0xFF959593) // 下载中网速带点微黄/橙色高亮，呼应截图
                    )

                    if (isDownloading && totalBytes > 0) {
                        Text(
                            text = "$downloadedStr/$totalStr",
                            fontSize = 11.sp,
                            color = Color(0xFF959593)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. 右侧极简删除按钮 (垃圾桶)
            IconButton(
                onClick = {
                    scope.launch {
                        if (!completed && state !is DownloadState.Completed) {
                            task.cancel()
                        }
                        task.remove(deleteFiles = completed)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ashcan),
                    contentDescription = "删除",
                    tint = Color(0xFFC0C2BF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 辅助方法：将字节大小转换为可读的 KB / MB / GB 字符串
 */
private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "%.1fG".format(gb)
        mb >= 1 -> "%.1fM".format(mb)
        kb >= 1 -> "%.1fKb".format(kb)
        else -> "$bytes B"
    }
}

@Composable
fun EmptyDownload(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}