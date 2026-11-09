package wzy.hkta.rhythbox.components.playlist

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.ToastManager

@Composable
fun EditPlaylist(
    playlistId: Long? = null,
    show: Boolean,
    onCancel: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    var describe by rememberSaveable { mutableStateOf("") }
    var playListName by rememberSaveable { mutableStateOf("") }
    val playlistDao = Rhythbox.dataBase.getPlaylistDao()

    val scope = rememberCoroutineScope()

    LaunchedEffect(show) {
        describe = ""
        playListName = ""
        playlistId?.let { id ->
            playlistDao.get(id)?.let {
                describe = it.describe ?: ""
                playListName = it.name
            }

        }
    }

    if (show) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(
                focusable = true,
                usePlatformDefaultWidth = false
            ),
            onDismissRequest = { onCancel() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp), // 现代大圆角
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "新建歌单",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(20.dp))

                    // 歌单名称
                    Text(
                        text = "歌单名称",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    CompactTextField(
                        value = playListName,
                        placeholder = "请输入歌单名称",
                        onValueChange = { playListName = it }
                    )

                    Spacer(Modifier.height(16.dp))

                    // 描述
                    Text(
                        text = "描述（选填）",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    CompactTextField(
                        value = describe,
                        maxLines = 3,
                        height = 84.dp,
                        placeholder = "请输入歌单描述信息...",
                        onValueChange = { describe = it }
                    )

                    Spacer(Modifier.height(24.dp))

                    // 底部操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onCancel() },
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
                                        when (playlistId) {
                                            null -> {
                                                playlistDao.insert(
                                                    PlaylistEntity(
                                                        name = playListName,
                                                        describe = describe
                                                    )
                                                )
                                                ToastManager.show("添加成功")
                                            }

                                            else -> {
                                                playlistDao.updateByName(
                                                    playlistId,
                                                    playListName,
                                                    describe
                                                )
                                                ToastManager.show("更新成功")
                                            }
                                        }
                                        onSuccess()
                                    } catch (err: Exception) {
                                        if (err.message?.startsWith("Error code: 2067") == true) {
                                            ToastManager.show("当前歌单已存在此歌曲")
                                        } else {
                                            ToastManager.show("创建失败")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
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

@Composable
fun CompactTextField(
    value: String,
    maxLines: Int = 1,
    minLines: Int = 1,
    height: Dp = 44.dp,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        maxLines = maxLines,
        minLines = minLines,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = if (maxLines > 1) Alignment.TopStart else Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}