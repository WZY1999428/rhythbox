package wzy.hkta.rhythbox.components.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.checkcircle
import rhythbox.composeapp.generated.resources.queue_music
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.utils.UserManager
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.PlaylistFields
import kotlin.collections.emptyList

@Composable
fun SelectOnlinePlaylist(
    show: Boolean,
    songId: Long,
    onDismissRequest: () -> Unit,
) {
    var selected by remember { mutableLongStateOf(0L) }
    var onlinePlayList by remember { mutableStateOf(emptyList<PlaylistFields>()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(show) {
        if (show) {
            selected = 0L
            UserManager.getToken()?.let {
                scope.launch {
                    try {
                        FetchMusic.user.getUserPlaylist()?.let { list ->
                            onlinePlayList = list
                            if (list.isNotEmpty() && selected == 0L) {
                                selected = list[0].id!!
                            }
                        }
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
        }

    }

    if (show) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp), // 更现代的大圆角
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
                        text = "选择保存歌单",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    // 歌单列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp, min = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(onlinePlayList) { item ->
                            val isSelected = selected == item.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { selected = item.id!! }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 歌单前置小图标
                                Icon(
                                    painter = painterResource(Res.drawable.queue_music),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = item.title,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )

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

                    Spacer(Modifier.height(20.dp))

                    // 底部按钮
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
                                scope.launch {
                                    try {
                                        FetchMusic.user.addUserPlaylistSong(songId, selected)
                                        ToastManager.show("添加成功")
                                    } catch (e: Exception) {
                                        ToastManager.show("添加失败: ${e.message}")
                                    } finally {
                                        onDismissRequest()
                                    }
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
                            Text("确定", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}