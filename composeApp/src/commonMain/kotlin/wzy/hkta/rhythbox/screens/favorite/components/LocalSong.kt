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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import rhythbox.composeapp.generated.resources.play3
import rhythbox.composeapp.generated.resources.sort1
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.song.SongBottomSheet
import wzy.hkta.rhythbox.components.song.SongItem
import wzy.hkta.rhythbox.components.song.SortSongBottomSheet
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.screens.ScanMusicScreen
import wzy.hkta.rhythbox.screens.SongMetadataScreen
import wzy.hkta.rhythbox.screens.favorite.FavoriteViewModel
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.isCurrentPlaying

@Composable
fun LocalSong(
    vm: FavoriteViewModel,
    navigator: Navigator
) {
    val songList = vm.localSongList.collectAsState()
    val songCount = songList.value.size
    val player = Rhythbox.player
    val playMediaId by player.playMediaId.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()
    var showSongBottomSheet by remember { mutableStateOf(false) }
    var showSortSongBottomSheet by remember { mutableStateOf(false) }
    var selectSong by remember { mutableStateOf<SongType?>(null) }
    val key = "favorite-local-song";
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp)
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        modifier = Modifier.width(90.dp).height(32.dp),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        onClick = {
                            vm.play(key)
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
                IconButton(onClick = {
                    showSortSongBottomSheet = true
                }) {
                    Icon(
                        painterResource(Res.drawable.sort1),
                        modifier = Modifier.size(16.dp),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }

            if (songList.value.isEmpty()) {
                RhythboxSongEmptyState(
                    describe = "还没有收藏歌曲哪",
                    // 🌟 顺便把按钮文字也根据页面动态调校一下，体验会更细腻！
                    buttonText = "我知道了",
                    onButtonClick = {
                        navigator.push(ScanMusicScreen())
                    }
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(songList.value.size) {
                        val item = songList.value[it]
                        SongItem(
                            no = it + 1,
                            title = item.name,
                            subTitle = item.artist ?: "",
                            duration = item.duration,
                            isPlay = isCurrentPlaying(playMediaId, item.mediaId),
                            isPlaying = isPlaying,
                            onMoreClick = {
                                selectSong = item
                                showSongBottomSheet = true
                            },
                            onClick = {
                                vm.play(key, it)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

            }
        }
    }

    if (selectSong != null) {
        SongBottomSheet(
            show = showSongBottomSheet,
            songName = selectSong!!.name,
            cover = selectSong!!.cover,
            songId = selectSong!!.id,
            artistName = selectSong!!.artist ?: "未知歌手",
            onDismissRequest = { showSongBottomSheet = false },
            onEdit = { navigator.push(SongMetadataScreen(selectSong!!.id)) },
            onDeleteClick = {
                if (songCount == 0) {
                    navigator.pop()
                }
            }
        )
    }

    SortSongBottomSheet(
        show = showSortSongBottomSheet,
        sortKey = key,
        onDismissRequest = {
            showSortSongBottomSheet = false
        }
    )
}