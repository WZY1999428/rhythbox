package wzy.hkta.rhythbox.screens.favorite.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.nologin
import rhythbox.composeapp.generated.resources.play3
import wzy.hkta.rhythbox.components.song.OnlineSongBottomSheet
import wzy.hkta.rhythbox.components.RhythboxLoadingState
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.song.SongItem
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.screens.favorite.FavoriteType
import wzy.hkta.rhythbox.screens.favorite.FavoriteViewModel
import wzy.hkta.rhythbox.screens.favorite.LoadState
import wzy.hkta.rhythbox.screens.search.netSearch.OnlineSearchScreen
import wzy.hkta.rhythbox.screens.user.LoginScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.UserManager
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.isCurrentPlaying

@Composable
fun OnlineSong(
    vm: FavoriteViewModel,
    navigator: Navigator
) {
    val player = Rhythbox.player
    val songList by vm.netSongList.collectAsState()
    val songCount = songList.total
    val playMediaId by player.playMediaId.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()
    var selected by remember { mutableStateOf<SongType?>(null) }
    var showSongBottomSheet by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (UserManager.getToken() != null) {
            vm.getList(FavoriteType.SONG)
        }
    }

    if (UserManager.getToken() == null) {
        RhythboxSongEmptyState(
            describe = "还没有登录，无法查看收藏的歌曲",
            image = Res.drawable.nologin,
            buttonText = "去登录",
            onButtonClick = {
                navigator.push(LoginScreen())
            })
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp)
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            modifier = Modifier.width(90.dp).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                            onClick = {
                                if (songCount > 0) {
                                    vm.netPlay(0, songList.list[0].id)
                                }
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(Res.drawable.play3),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("播放全部", color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "(${songCount})首音乐",
                            fontSize = 14.sp,
                            color = "#999999".cssToColor()
                        )
                    }
                }

                when (songList.loading) {
                    LoadState.NotFound -> RhythboxSongEmptyState(
                        describe = "还没有收藏歌曲哪",
                        // 🌟 顺便把按钮文字也根据页面动态调校一下，体验会更细腻！
                        buttonText = "去搜索",
                        onButtonClick = {
                            navigator.push(OnlineSearchScreen())
                        }
                    )

                    LoadState.Finish -> LazyColumn(modifier = Modifier.weight(1f)) {
                        items(songList.list.size) {
                            val item = songList.list[it]
                            SongItem(
                                no = it + 1,
                                title = item.name,
                                subTitle = item.artist!!,
                                duration = item.duration,
                                isPlay = isCurrentPlaying(playMediaId, item.mediaId),
                                isPlaying = isPlaying,
                                onMoreClick = {
                                    selected = item
                                    showSongBottomSheet = true
                                },
                                onClick = {
                                    vm.netPlay(it, item.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    LoadState.Loading -> RhythboxLoadingState()

                    is LoadState.LoadError -> RhythboxSongEmptyState(
                        describe = "加载失败：${(songList.loading as LoadState.LoadError).msg}",
                        buttonText = "重新加载",
                        onButtonClick = {
                            vm.getList(FavoriteType.SONG)
                        }
                    )
                }
            }
        }
        if (selected != null) {
            OnlineSongBottomSheet(
                show = showSongBottomSheet,
                songId = selected!!.id,
                artists = selected!!.artists!!,
                songName = selected!!.name,
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