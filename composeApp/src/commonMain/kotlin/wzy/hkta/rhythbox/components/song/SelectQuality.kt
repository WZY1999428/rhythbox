package wzy.hkta.rhythbox.components.song

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.checkcircle
import wzy.hkta.rhythbox.components.CircularBarSpinner
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.SongAudioItem

@Composable
fun SelectQuality(
    show: Boolean,
    songId: Long,
    onDismissRequest: () -> Unit,
    onConfirm: (selectedAudio: SongAudioItem) -> Unit
) {


    val audioList = remember { mutableStateListOf<SongAudioItem>() }
    var loading by remember { mutableStateOf(true) }
    var selectedAudioId by remember { mutableLongStateOf(0L) }
    val scape = rememberCoroutineScope()

    LaunchedEffect(show) {
        if (show) {
            scape.launch {
                loading = true
                try {
                    val data = FetchMusic.music.getSongQuality(songId)
                    audioList.clear()
                    audioList.addAll(data)
                    selectedAudioId = audioList.firstOrNull()?.id ?: 0L
                } catch (e: Exception) {
                    ToastManager.show("加载失败:$e")
                    onDismissRequest()
                }
                loading = false
            }
        }
    }


    if (show) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp), // 现代大圆角
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    Text(
                        text = "选择下载音质",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    // 音质列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp, min = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (loading) {
                            true -> item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) { CircularBarSpinner() }
                            }

                            false -> {
                                items(audioList) { audio ->
                                    val isSelected = selectedAudioId == audio.id

                                    // 格式化比特率展示，例如 128000 -> 128kbps
                                    val kbps = audio.bitrate / 1000
                                    val descText = "${audio.format} · ${kbps}kbps"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected)
                                                    MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.3f
                                                    )
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(
                                                        alpha = 0.1f
                                                    )
                                            )
                                            .clickable { selectedAudioId = audio.id }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = audio.qualityTitle, // 如：标准、HQ高品质、SQ无损
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = descText, // 如：MPEG 1 Layer 3 · 128kbps
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                painter = painterResource(Res.drawable.checkcircle),
                                                modifier = Modifier.size(18.dp),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 底部操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onDismissRequest() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = CircleShape
                        ) {
                            Text("取消", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                audioList.find { it.id == selectedAudioId }?.let {
                                    onConfirm(it)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                            shape = CircleShape
                        ) {
                            Text("确定下载", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}