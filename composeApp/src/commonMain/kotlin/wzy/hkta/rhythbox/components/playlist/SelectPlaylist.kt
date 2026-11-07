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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.checkcircle
import rhythbox.composeapp.generated.resources.queue_music
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.utils.Rhythbox

@Composable
fun SelectPlaylist(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (id: Long) -> Unit
) {
    var playList by remember { mutableStateOf(emptyList<PlaylistEntity>()) }
    var selected by remember { mutableLongStateOf(0L) }

    LaunchedEffect(show) {
        val playlistDao = Rhythbox.dataBase.getPlaylistDao()
        playList = playlistDao.getList()
        if (playList.isNotEmpty() && selected == 0L) {
            selected = playList[0].id
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
                        items(playList) { item ->
                            val isSelected = selected == item.id

                            // 使用轻量美观的行代替生硬的 Card
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
                                    .clickable { selected = item.id }
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
                                    text = item.name,
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
                            onClick = { onConfirm(selected) },
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