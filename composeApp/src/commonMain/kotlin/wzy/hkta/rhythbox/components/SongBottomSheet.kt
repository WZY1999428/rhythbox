package wzy.hkta.rhythbox.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.FavoriteBorder
//import androidx.compose.material.icons.filled.List
//import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.collect
import rhythbox.composeapp.generated.resources.delete
import rhythbox.composeapp.generated.resources.edit
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.playlist
import rhythbox.composeapp.generated.resources.ring
import wzy.hkta.rhythbox.database.CollectEntity
import wzy.hkta.rhythbox.database.PlaylistSongEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SnackbarManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongBottomSheet(
    show: Boolean = false,
    songId: Long,
    songName: String = "今日重到苏澜桥",       // 动态传入当前歌曲名字 🍑
    artistName: String = "三无 Marblue",     // 动态传入歌手名字
    cover: String? = null,
    onEdit: () -> Unit = {},
    onSetRingClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var showSelectPlaylist by remember { mutableStateOf(false) }
    val dao = Rhythbox.dataBase.getPlaylistDao();
    val collectDao = Rhythbox.dataBase.getCollectDao();
    val scope = rememberCoroutineScope()
    var isLike by remember { mutableStateOf(false) }
    LaunchedEffect(show) {
        isLike = collectDao.isSongCollected(songId)
    }
    if (show) {
        ModalBottomSheet(
            dragHandle = {
                // 🌟 自定义一个精致的蜜桃粉小提手线
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(36.dp, 4.dp)
                        .background(Color(0xFFFFB7A1).copy(alpha = 0.4f), CircleShape)
                )
            },
            containerColor = Color(0xFFFFFBF9), // 温暖的微醺米白色底色，比纯白温柔百倍
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // 更圆润的现代弧度
            onDismissRequest = { onDismissRequest() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp), // 为系统全面屏手势留足呼吸间距
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🎵 1. 顶部歌曲信息高级卡片区
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 歌曲封面（这里套上我们萌萌的乐动盒 Logo 或者是二次元封面）
                    if (cover == null) {
                        Image(
                            painter = painterResource(Res.drawable.logo), // 🌟 抓过来我们的小精灵 Logo
                            contentDescription = "Song Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        AsyncImage(
                            model = cover,
                            contentDescription = "Song Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // 歌名与歌手信息
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = songName,
                            color = Color(0xFF4A3E3D), // 深咖色，比纯黑高级
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = artistName,
                            color = Color(0xFF9E8E87), // 柔和的浅灰咖
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 🌟 分割线，让层次感更分明
                HorizontalDivider(color = Color(0xFFFFB7A1).copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                // 🛠️ 2. 操作功能列表（带图标的行组件）
                BottomSheetActionRow(Res.drawable.edit, "编辑歌曲") {
                    onDismissRequest()
                    onEdit()
                }
                BottomSheetActionRow(Res.drawable.playlist, "添加到歌单") {
                    showSelectPlaylist = true
                }
                BottomSheetActionRow(Res.drawable.collect, if (isLike) "取消收藏" else "收藏歌曲") {
                    scope.launch {
                        if (isLike) collectDao.delete(songId)
                        else collectDao.insert(CollectEntity(songId = songId))
                        onDismissRequest()
                        SnackbarManager.show(if (isLike) "取消成功" else "添加成功")
                    }
                }

                BottomSheetActionRow(Res.drawable.ring, "设为铃声") { onSetRingClick() }

                Spacer(modifier = Modifier.height(20.dp))

                // 🚨 3. 极具设计感的删除按钮
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    Rhythbox.dataBase.deleteSongAndCleanUpTotally(songId)
                                }
                                onDeleteClick()
                                onDismissRequest()
                                SnackbarManager.show("删除成功")
                            } catch (e: Exception) {
                                println(e)
                                onDismissRequest()
                                SnackbarManager.show("歌曲删除失败")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFF0EE),
                        contentColor = Color(0xFFFF5252)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        painterResource(Res.drawable.delete),
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除这首歌曲", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    SelectPlaylist(
        show = showSelectPlaylist,
        onCancel = { showSelectPlaylist = false },
        onConfirm = { id ->
            scope.launch {
                try {
                    dao.addSong(
                        PlaylistSongEntity(songId = songId, playlistId = id, congCover = cover)
                    )
                    showSelectPlaylist = false
                    onDismissRequest()
                    SnackbarManager.show("添加成功")
                } catch (e: Exception) {
                    showSelectPlaylist = false
                    onDismissRequest()
                    val msg = e.message
                    if (msg?.startsWith("Error code: 2067") == true) {
                        SnackbarManager.show("歌曲已存在此歌单")
                    } else {
                        SnackbarManager.show("添加失败")
                    }
                }

            }
        }
    )

}

// 🎨 抽离出来的单个操作行组件，高内聚低耦合！
@Composable
fun BottomSheetActionRow(
    icon: DrawableResource,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(icon),
            contentDescription = text,
            tint = Color(0xFF7D6E65), // 温润的咖色图标
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color(0xFF5D4E46), // 中咖色文字
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}