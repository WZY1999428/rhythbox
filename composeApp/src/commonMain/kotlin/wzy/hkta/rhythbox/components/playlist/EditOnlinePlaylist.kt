package wzy.hkta.rhythbox.components.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage // 确保使用你的项目里的图片加载组件，如果是 Painter 也可以适配
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.filekit.encodeToByteArray
import com.attafitamim.krop.filekit.toImageSrc
import com.attafitamim.krop.ui.ImageCropperDialog
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.download_line // 或者你的默认封面/加号图标
import wzy.hkta.rhythbox.BuildConfig.MUSIC_BASE_URL
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.fatchMusic.Cover
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.PlaylistFields
import wzy.hkta.rhythbox.fatchMusic.Song


@Composable
fun EditOnlinePlaylist(
    playlistId: Long? = null,
    show: Boolean,
    onCancel: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    var playlistFields by remember { mutableStateOf(PlaylistFields<Song>(title = "", description = "")) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()

    // 图片选择器
    val imagePicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = { image ->
            image?.let {
                scope.launch {
                    try {
                        val result = imageCropper.crop { it.toImageSrc() }
                        when (result) {
                            CropResult.Cancelled -> ToastManager.show("图片上传取消")
                            is CropError -> ToastManager.show("图片裁剪失败")
                            is CropResult.Success -> {
                                ToastManager.showLoading("正在上传封面...")
                                val bytes = result.bitmap.encodeToByteArray()
                                val data = FetchMusic.updateCover(bytes, it.name)
                                playlistFields =
                                    playlistFields.copy(cover = Cover(data.id, data.url))
                                ToastManager.show("封面上传成功")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ToastManager.show("图片处理异常：${e.message}")
                    }
                }
            }
        }
    )

    imageCropper.cropState?.let {
        ImageCropperDialog(
            state = it,
            style = cropperStyle(
                overlay = Color.Gray.copy(alpha = .5f),
                autoZoom = false,
                guidelines = null,
            )
        )
    }

    LaunchedEffect(show) {
        if (show) {
            playlistFields = PlaylistFields(
                title = "",
                description = ""
            )
            playlistId?.let { id ->
                FetchMusic.user.getPlayListInfo(id)?.let {
                    playlistFields = playlistFields.copy(
                        id = it.id,
                        cover = it.cover,
                        title = it.title,
                        description = it.description
                    )
                }
            }
        }
    }

    if (show) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { onCancel() },
            properties = PopupProperties(
                focusable = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
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
                        text = if(playlistId!=null) "编辑歌单" else "新建歌单",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    // ================= 1. 封面上传与预览区域 =================
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                // 唤起文件选择器
                                imagePicker.launch()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!playlistFields.cover?.url.isNullOrEmpty()) {
                            // 如果已经上传成功，显示网络封面图
                            AsyncImage(
                                model = "$MUSIC_BASE_URL${playlistFields.cover?.url}",
                                contentDescription = "歌单封面",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 未上传时显示引导图标或文案
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(Res.drawable.download_line), // 可换成你的相册/加号图标
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "选封面",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

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
                        value = playlistFields.title,
                        placeholder = "请输入歌单名称",
                        onValueChange = { playlistFields = playlistFields.copy(title = it) }
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
                        value = playlistFields.description ?: "",
                        maxLines = 3,
                        height = 84.dp,
                        placeholder = "请输入歌单描述信息...",
                        onValueChange = { playlistFields = playlistFields.copy(description = it) }
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
                            Text("取消", fontSize = 14.sp, )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    try {
                                        FetchMusic.user.savePlaylist(playlistFields)
                                        ToastManager.show("保存成功")
                                        onSuccess()
                                    } catch (e: Exception) {
                                        ToastManager.show("歌单创建失败:${e.message}")
                                    } finally {
                                        loading = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            enabled = !loading,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = CircleShape
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("确定", fontSize = 14.sp,color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}