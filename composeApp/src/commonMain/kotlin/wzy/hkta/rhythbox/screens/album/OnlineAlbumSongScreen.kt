package wzy.hkta.rhythbox.screens.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.like_grey
import rhythbox.composeapp.generated.resources.like_pink
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.play3
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.song.OnlineSongBottomSheet
import wzy.hkta.rhythbox.components.song.SongItem
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.fatchMusic.Album
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.fatchMusic.SongArtist
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.isCurrentPlaying

class OnlineAlbumSongScreen(
    val id: Long,
    val title: String? = "专辑"
) : Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val key = "${title}_${id}_NetAlbumSong"

        val vm = rememberScreenModel(tag = key) {
            OnlineAlbumViewSongModel(id, key)
        }

        val albumInfo by vm.albumInfo.collectAsState()
        val songList by vm.songList.collectAsState()
        val loading by vm.loading.collectAsState()

        val playMediaId by Rhythbox.player.playMediaId.collectAsState()
        val isPlaying by Rhythbox.player.isPlaying.collectAsState()

        var showSongBottomSheet by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf<SongType?>(null) }

        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFA0B69E))
        ) {
            // 顶部的导航栏，独立出来确保返回按钮永远可见
            NavBar(
                title = "${title}专辑",
                iconColor = Color.White,
                titleColor = Color.White,
                background = Color.Transparent
            )
            albumInfo?.let {
                AlbumCardInfo(
                    navigator = navigator,
                    albumInfo = it,
                    count = songList.size
                )
            }
            // 用一个统一可滚动的 LazyColumn，把“专辑头部信息”作为第0个 item
            // 这样既能完美滚动，又绝不会发生文字被遮挡、显示不全的问题！
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                // 播放全部和排序栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        modifier = Modifier
                            .width(110.dp)
                            .height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            vm.play(0)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.play3),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("播放全部", fontSize = 13.sp)
                        }
                    }

                    val isLiked = albumInfo?.isLike ?: false
                    IconButton(onClick = {
                        vm.togglePlaylistFavorite()
                    }) {
                        Icon(
                            painter = painterResource(
                                if (isLiked) Res.drawable.like_pink else Res.drawable.like_grey
                            ),
                            modifier = Modifier.size(18.dp),
                            contentDescription = "收藏专辑",
                            tint = if (isLiked) Color(0xFFE91E63) else Color(0xFF757575)
                        )
                    }
                }

                when {
                    loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    songList.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            RhythboxSongEmptyState(
                                describe = "该专辑暂无歌曲",
                                buttonText = "返回",
                                onButtonClick = {
                                    navigator.pop()
                                }
                            )
                        }
                    }

                    else -> {
                        // 歌曲列表项
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        ) {
                            items(songList.size) { index ->
                                val song = songList[index]
                                SongItem(
                                    title = song.name,
                                    subTitle = song.artist ?: "",
                                    duration = song.duration,
                                    isPlay = isCurrentPlaying(
                                        playMediaId,
                                        song.mediaId
                                    ),
                                    isPlaying = isPlaying,
                                    no = index + 1,
                                    onMoreClick = {
                                        selected = song
                                        showSongBottomSheet = true
                                    },
                                    onClick = {
                                        vm.play(index)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (playMediaId != null) {
                BottomControl()
            }
        }

        if (selected != null) {
            OnlineSongBottomSheet(
                show = showSongBottomSheet,
                artists = selected!!.artists!!,
                songName = selected!!.name,
                songId = selected!!.id,
                cover = selected!!.cover,
                onArtistClick = { artistId, artistName ->
                    showSongBottomSheet = false
                    navigator.push(OnlineArtistSongScreen(artistId, artistName))
                },
                onDismissRequest = {
                    showSongBottomSheet = false
                    selected = null
                }
            )
        }
    }
}

@Composable
private fun AlbumCardInfo(
    albumInfo: Album,
    count: Int,
    navigator: Navigator,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 专辑封面
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                if (!albumInfo.cover.isNullOrBlank()) {
                    AsyncImage(
                        model = albumInfo.cover,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // 右侧信息，增加行间距与更清晰的文字样式
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = albumInfo.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${albumInfo.releaseDate.ifBlank { "未知日期" }} · $count 首歌曲",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1
                )

                Text(
                    text = albumInfo.company.ifBlank { "未知唱片公司" },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = listOf(albumInfo.type, albumInfo.genre.joinToString("/"))
                        .joinToString(" · ")
                        .ifBlank { "流行 · 综合" },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 歌手
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "歌手：",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            ArtistName(
                albumInfo.artists,
                onArtistClick = { artistId, artistName ->
                    navigator.push(OnlineArtistSongScreen(artistId, artistName))
                }
            )
        }

        Spacer(Modifier.height(6.dp))

        // 简介（给足行高，稍微放宽最大行数或不限制让其看得更清）
        Text(
            text = albumInfo.description.ifBlank { "暂无专辑简介说明..." },
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 3, // 稍微多显示一行
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(16.dp))
    }
}


@Composable
fun ArtistName(
    artists: List<SongArtist>,
    onArtistClick: (Long, String) -> Unit,
    textColor: Color = Color.White
) {
    artists.forEachIndexed { index, ar ->
        if (index > 0) {
            Text(
                text = " / ",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Text(
            text = ar.artistName,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                onArtistClick(ar.artistId, ar.artistName)
            }.padding(horizontal = 5.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
