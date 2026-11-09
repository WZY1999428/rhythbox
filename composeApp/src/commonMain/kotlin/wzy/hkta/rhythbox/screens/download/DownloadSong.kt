package wzy.hkta.rhythbox.screens.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.components.MAlertDialog
import wzy.hkta.rhythbox.components.song.OnlineSongBottomSheet
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.song.SongItem
import wzy.hkta.rhythbox.components.song.SortSongBottomSheet
import wzy.hkta.rhythbox.expect.deleteFile
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistSongTopBar
import wzy.hkta.rhythbox.screens.song.OnlineSongScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.utils.isCurrentPlaying


@Composable
fun DownloadSong(
    vm: DownloadViewModel,
) {
    val navigator = LocalNavigator.currentOrThrow
    val player = Rhythbox.player
    val songList by vm.songList.collectAsState()
    val sortEntity by vm.sortEntity.collectAsState()
    val playMediaId by player.playMediaId.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()
    var showSongBottomSheet by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showSortSongBottomSheet by remember { mutableStateOf(false) }
    var selectSong by remember { mutableStateOf<SongType?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        vm.getDownloadEntity()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            PlaylistSongTopBar(
                showISLike = false,
                onSortClick = { showSortSongBottomSheet = true },
                sortEntity = sortEntity,
                onPlay = { vm.play(it) }
            )

            if (songList.isEmpty()) {
                RhythboxSongEmptyState(
                    describe = "没有下载的歌曲",
                    buttonText = "去搜素",
                    onButtonClick = {
                        navigator.push(OnlineSongScreen())
                    }
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        songList.size,
                        key = { index -> songList[index].mediaId }
                    ) {
                        val item = songList[it]
                        SongItem(
                            no = it + 1,
                            title = item.name,
                            subTitle = item.artist,
                            duration = item.duration,
                            isPlay = isCurrentPlaying(playMediaId, item.mediaId),
                            isPlaying = isPlaying,
                            onMoreClick = {
                                selectSong = item
                                showSongBottomSheet = true
                            },
                            onClick = {
                                vm.play(it)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

        }
    }

    if (selectSong != null) {
        OnlineSongBottomSheet(
            show = showSongBottomSheet,
            songName = selectSong!!.name,
            cover = selectSong!!.cover,
            songId = selectSong!!.id,
            artists = selectSong!!.artists ?: emptyList(),
            onDismissRequest = { showSongBottomSheet = false },
            onDeleteClick = {
                showSongBottomSheet = false
                showDelete = true
            },
            onArtistClick = { artistId, artistName ->
                showSongBottomSheet = false
                navigator.push(OnlineArtistSongScreen(artistId, artistName))
            }
        )
    }
    var deleteFile by remember { mutableStateOf(false) }
    MAlertDialog(
        show = showDelete,
        title = "提示",
        onCancel = { showDelete = false },
        onConfirm = {
            scope.launch {
                if (deleteFile) {
                    deleteFile(selectSong!!.filePath!!)
                }
                vm.downloadDao.deleteOnFile(selectSong!!.filePath!!)
                showDelete = false
                ToastManager.show("删除成功")
                vm.getDownloadEntity()
            }
        }
    ) {
        Text("确定要删除当前任务吗")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = deleteFile,
                onCheckedChange = { deleteFile = it }
            )
            Text("删除下载的文件")
        }
    }




    SortSongBottomSheet(
        show = showSortSongBottomSheet,
        sortKey = vm.key,
        onDismissRequest = {
            showSortSongBottomSheet = false
        }
    )

}