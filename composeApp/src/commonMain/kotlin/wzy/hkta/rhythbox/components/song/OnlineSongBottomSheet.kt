package wzy.hkta.rhythbox.components.song

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.collect
import rhythbox.composeapp.generated.resources.download_line
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.playlist
import rhythbox.composeapp.generated.resources.ring
import wzy.hkta.rhythbox.components.playlist.SelectOnlinePlaylist
import wzy.hkta.rhythbox.screens.album.ArtistName
import wzy.hkta.rhythbox.fatchMusic.SongArtist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineSongBottomSheet(
    show: Boolean = false,
    songId: Long,
    songName: String = "今日重到苏澜桥",       // 动态传入当前歌曲名字 🍑
    artists: List<SongArtist>,     // 动态传入歌手名字
    cover: String? = null,
    isLike: Boolean = false,
    onSetRingClick: () -> Unit = {},
    onDismissRequest: () -> Unit,
    onArtistClick: (Long, String) -> Unit,
) {
    var showSelectPlaylist by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArtistName(
                                artists,
                                textColor = Color(0xFF9E8E87),
                                onArtistClick = onArtistClick
                            )
                        }
                    }
                }

                // 🌟 分割线，让层次感更分明
                HorizontalDivider(color = Color(0xFFFFB7A1).copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                // 🛠️ 2. 操作功能列表（带图标的行组件）
                BottomSheetActionRow(Res.drawable.download_line, "下载歌曲") {
                    onDismissRequest()
                }

                BottomSheetActionRow(Res.drawable.playlist, "添加到歌单") {
                    showSelectPlaylist = true
                }
                BottomSheetActionRow(Res.drawable.collect, if (isLike) "取消收藏" else "收藏歌曲") {
                    scope.launch {
                    }
                }

                BottomSheetActionRow(Res.drawable.ring, "设为铃声") { onSetRingClick() }

                Spacer(modifier = Modifier.height(20.dp))

            }
        }
    }

    SelectOnlinePlaylist(
        songId = songId,
        show = showSelectPlaylist,
        onDismissRequest = { showSelectPlaylist = false },
    )
}
