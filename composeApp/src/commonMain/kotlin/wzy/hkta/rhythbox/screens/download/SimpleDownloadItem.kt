package wzy.hkta.rhythbox.screens.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linroid.ketch.api.DownloadState
import com.linroid.ketch.api.DownloadTask
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.ashcan
import rhythbox.composeapp.generated.resources.checkcircle
import rhythbox.composeapp.generated.resources.close_grey
import rhythbox.composeapp.generated.resources.close_pink
import rhythbox.composeapp.generated.resources.loading
import rhythbox.composeapp.generated.resources.pause2
import rhythbox.composeapp.generated.resources.play3
import rhythbox.composeapp.generated.resources.retry
import wzy.hkta.rhythbox.utils.formatFileSize

@Composable
internal fun SimpleDownloadItem(
    item: DownloadTask,
    vm: DownloadViewModel,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit
) {
    val isDownloading = item.state == DownloadState.Downloading
    val songInfo = vm.getTaskSong(item.outputPath)
    val state by item.state.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = { onTogglePause() }
        ) {
            when (state) {
                is DownloadState.Downloading -> Icon(
                    painter = painterResource(Res.drawable.pause2),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                is DownloadState.Paused -> Icon(
                    painter = painterResource(Res.drawable.play3),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFA726)
                )


                is DownloadState.Canceled -> Icon(
                    painter = painterResource(Res.drawable.close_pink),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                is DownloadState.Failed -> Icon(
                    painter = painterResource(Res.drawable.retry),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                is DownloadState.Completed -> Icon(
                    painter = painterResource(Res.drawable.checkcircle),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF4CAF50)
                )

                is DownloadState.Scheduled, is DownloadState.Queued -> Icon(
                    painter = painterResource(Res.drawable.loading),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF2196F3)
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
                    text = songInfo?.title?.trim() ?: "${item.outputPath}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 150.dp)
                )

                Spacer(Modifier.width(8.dp))
                Text(
                    text = songInfo?.artists?.joinToString("/") { it.artistName } ?: "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 150.dp),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(4.dp))

            if (isDownloading) {
                // 下载中的进度条
                val progress = (state as DownloadState.Downloading).progress
                Column {
                    LinearProgressIndicator(
                        progress = { progress.percent },
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
                            text = formatFileSize(progress.downloadedBytes),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatFileSize(progress.totalBytes),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 等待下载状态提示
                Text(
                    text = when (state) {
                        is DownloadState.Downloading -> "下载中"
                        is DownloadState.Paused -> "已暂停"
                        is DownloadState.Completed -> "已完成"
                        is DownloadState.Canceled -> "已取消"
                        is DownloadState.Failed -> "下载失败:${(state as DownloadState.Failed).error}"
                        is DownloadState.Queued -> "等待下载"
                        is DownloadState.Scheduled -> "等待调度"
                    },
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