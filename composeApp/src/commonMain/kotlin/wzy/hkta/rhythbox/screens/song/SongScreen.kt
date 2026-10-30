package wzy.hkta.rhythbox.screens.song

import androidx.compose.foundation.layout.*
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
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.play3
import rhythbox.composeapp.generated.resources.scanning
import rhythbox.composeapp.generated.resources.sort1
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.SongBottomSheet
import wzy.hkta.rhythbox.components.SongItem
import wzy.hkta.rhythbox.components.SortSongBottomSheet
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.screens.ScanMusicScreen
import wzy.hkta.rhythbox.screens.SongMetadataScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.generateMediaId
import wzy.hkta.rhythbox.utils.getSortType
import wzy.hkta.rhythbox.utils.isCurrentPlaying
import kotlin.collections.emptyList
import kotlin.time.Duration.Companion.milliseconds

class SongScreen(
    override val key: String = "songScreen",
    val title: String = "歌曲",
    val dir: String = ""
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel(tag = "${title}_${key}") { SongViewModel(key, dir) }
        val songList = vm.songList.collectAsState()
        val songCount = vm.songCount.collectAsState()
        val player = Rhythbox.player
        val playUuid = player.playUuid.collectAsState()
        val isPlaying = player.isPlaying.collectAsState()
        var showSongBottomSheet by remember { mutableStateOf(false) }
        var showSortSongBottomSheet by remember { mutableStateOf(false) }
        var selectSong by remember { mutableStateOf<SongEntity?>(null) }

        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(title = title) {
                IconButton(
                    onClick = {
                        navigator.push(ScanMusicScreen())
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.scanning),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

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
                                vm.play()
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
                            "(${songCount.value})首音乐",
                            fontSize = 14.sp,
                            color = "#999999".cssToColor()
                        )
                    }
                    IconButton(onClick = {
                        showSortSongBottomSheet=true
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
                        describe = when (key) {
                            "songScreen" -> "乐动盒里空空如也，快去唤醒藏在手机里的旋律吧~"
                            "RecentlyAdded" -> "最近没有新歌入库哦，快让小精灵帮你去翻箱倒柜吧~"
                            "MusicRankings" -> "排行榜还是一片空白呢，快去听歌留下你的专属足迹吧~"
                            "MyCollection" -> "还没有让你一见钟情的旋律吗？点亮红心就能收藏哦~"
                            else -> "这里什么都没有哦~"
                        },
                        // 🌟 顺便把按钮文字也根据页面动态调校一下，体验会更细腻！
                        buttonText = when (key) {
                            "songScreen", "RecentlyAdded" -> "立即添加"
                            "MusicRankings" -> "去听歌"
                            "MyCollection" -> "去发现"
                            else -> "我知道了"
                        },
                        onButtonClick = {
                            when (key) {
                                "MusicRankings", "MyCollection" -> {
                                    navigator.pop() // 或者跳回你的主页列表
                                }

                                else -> {
                                    navigator.push(ScanMusicScreen())
                                }
                            }
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
                                isPlay = isCurrentPlaying(playUuid.value, item.uuid),
                                isPlaying = isPlaying.value,
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
                    BottomControl()
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
                    if (songCount.value == 0) {
                        navigator.pop()
                    }
                }
            )
        }

        SortSongBottomSheet(
            show = showSortSongBottomSheet,
            currentSort = "",
            onSortSelected = {

            },
            onDismissRequest = {
                showSortSongBottomSheet = false
            }
        )

    }
}

private class SongViewModel(val key: String, dir: String?) : ScreenModel {
    var songDao = Rhythbox.dataBase.getSongDao()
    val playListSortDao = Rhythbox.dataBase.getPlayListSortDao()

    private val player = Rhythbox.player
    val playKey = player.playKey

    @OptIn(ExperimentalCoroutinesApi::class)
    val songList = playListSortDao.get(key).flatMapLatest {
        val sort = getSortType(it?.sortType)
        when (key) {
            "songScreen" -> songDao.find(sort)
            "RecentlyAdded" -> songDao.getRecentlyAddedSongs(sort)
            "MusicRankings" -> songDao.getPlayCountList()
            "FolderScreen$dir" -> songDao.getDir(dir!!, sort)
            "MyCollection" -> songDao.findCollect(sort)
            else -> songDao.find(sort)
        }
    }.map { rawList ->
        rawList.map { song -> song.copy(uuid = generateMediaId(key, song)) }
    }.onEach {
        if (playKey.value == key) resetMediaItems()
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    val songCount = when (key) {
        "songScreen" -> songDao.count()
        "RecentlyAdded" -> songDao.getRecentlyAddedCount()
        "MusicRankings" -> songDao.getPlayCount()
        "FolderScreen$dir" -> songDao.getDirCount(dir!!)
        "MyCollection" -> songDao.collectCount()
        else -> songDao.count()
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun play(index: Int = 0) {
        player.let { p ->
            p.resetMediaItems(key, SongListType.LocalList(songList.value), false)
            p.prepare(index)
            p.play()
        }
    }

    fun resetMediaItems() {
        screenModelScope.launch {
            delay(50.milliseconds)
            player.resetMediaItems(key, SongListType.LocalList(songList.value), false)
        }
    }
}