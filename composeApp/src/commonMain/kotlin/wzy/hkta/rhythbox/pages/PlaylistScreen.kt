package wzy.hkta.rhythbox.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.more
import rhythbox.composeapp.generated.resources.play3
import rhythbox.composeapp.generated.resources.sort1
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.MAlertDialog
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.PlaylistInfoBottomSheet
import wzy.hkta.rhythbox.components.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.SongBottomSheet
import wzy.hkta.rhythbox.components.SongItem
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.Rhythbox.player
import wzy.hkta.rhythbox.utils.SnackbarManager
import wzy.hkta.rhythbox.utils.formatDate
import wzy.hkta.rhythbox.utils.generateMediaId
import wzy.hkta.rhythbox.utils.isCurrentPlaying

enum class PlaylistType {
    PLAYLIST,
    ALBUM,
    ARTIST,
    DIR
}

class PlaylistScreen(
    val title: String,
    val id: Long,
    val type: PlaylistType
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val density = LocalDensity.current
        val navigator = LocalNavigator.currentOrThrow
        val key = "${title}_${id}_${type}"
        val vm = rememberScreenModel(tag = key) {
            PlaylistViewModel(id, type, title, key, navigator)
        }
        val playlistInfo = vm.playlistInfo.collectAsState()
        var showEditSheet by remember { mutableStateOf(false) }
        var showAlertDialog by remember { mutableStateOf(false) }
        var showSongBottomSheet by remember { mutableStateOf(false) }
        var selectSong by remember { mutableStateOf<SongEntity?>(null) }
        val info = vm.info.collectAsState()
        val songList = vm.songList.collectAsState()
        val playUuid = player.playUuid.collectAsState()
        val isPlaying = player.isPlaying.collectAsState()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxWidth = constraints.maxWidth.toFloat()
            val maxHeight = with(density) { constraints.maxHeight.toDp() }
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(240.dp).background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0f to "#CA799A".cssToColor(),
                                1.00f to "#EFEFEF".cssToColor(),
                            ),
                            start = Offset.Zero,
                            end = Offset(x = maxWidth, y = 250f)
                        )
                    ),
                ) {
                    NavBar(
                        title = title,
                        iconColor = Color.White,
                        titleColor = Color.White,
                        background = Color.Transparent,
                        action = {
                            IconButton(onClick = {
                                if (type == PlaylistType.PLAYLIST) {
                                    showEditSheet = true
                                } else {
                                    println(123)
                                }
                            }) {
                                if (type == PlaylistType.PLAYLIST) {
                                    Icon(
                                        painterResource(Res.drawable.more),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                        contentDescription = null,
                                    )
                                }

                            }
                        }
                    )
                    info.value?.let {
                        SongInfo(it.name, it.count, modifier = Modifier.weight(1f))
                    }

                }
                Column(
                    modifier = Modifier
                        .height(maxHeight - 210.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp
                            ),
                            clip = false
                        )
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.White)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 10.dp)
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
                                onClick = { vm.play(0) }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(Res.drawable.play3),
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("播放全部")
                                }
                            }
                        }
//                        IconButton(onClick = {}) {
//                            Icon(
//                                painterResource(Res.drawable.sort1),
//                                modifier = Modifier.size(16.dp),
//                                contentDescription = null,
//                                tint = "#E6A699".cssToColor(),
//                            )
//                        }
                    }

                    if (songList.value.isEmpty()) {
                        RhythboxSongEmptyState(
                            describe = when (type) {
                                PlaylistType.ALBUM -> "这张专辑的磁带还没录入声音，快去给它填满曲目吧~"
                                PlaylistType.ARTIST -> "这位音乐人还在创作中，舞台正静候旋律响起~"
                                PlaylistType.DIR -> "这个文件夹静悄悄的，小精灵还没在这里搜刮到音符~"
                                PlaylistType.PLAYLIST -> "新建的宝藏歌单空空如也，快把心爱的旋律装进盒子里吧~" // 🌟 顺手帮你纠正了 PLAYlIST 的大小写拼写~
                            },
                            // 🌟 按钮文案微调：如果是歌单空，引导用户“去选歌”；如果是其他类型空，引导用户去“扫描/添加”
                            buttonText = when (type) {
                                PlaylistType.PLAYLIST -> "去选歌"
                                else -> "去扫描"
                            },
                            onButtonClick = {
                                when (type) {
                                    PlaylistType.PLAYLIST -> navigator.push(SongScreen())
                                    else -> navigator.push(ScanMusicScreen())
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(songList.value.size) {
                                val item = songList.value[it]
                                SongItem(
                                    title = item.name,
                                    subTitle = item.artist ?: "未知歌手",
                                    duration = item.duration,
                                    isPlay = isCurrentPlaying(playUuid.value, item.uuid),
                                    isPlaying = isPlaying.value,
                                    no = it + 1,
                                    onMoreClick = {
                                        selectSong = item
                                        showSongBottomSheet = true
                                    },
                                    onClick = {
                                        vm.play(it)
                                    }
                                )
                            }
                        }
                        BottomControl()
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
                onEdit = { navigator.push(SongMetadataScreen(selectSong!!.id)) },
                onDismissRequest = { showSongBottomSheet = false },
                onDeleteClick = {
                    if (info.value?.count == 0) {
                        navigator.pop()
                    }
                }
            )
        }

        MAlertDialog(
            show = showAlertDialog,
            title = "提示",
            content = "确定要删除此歌单吗",
            onCancel = { showAlertDialog = false },
            onConfirm = {
                vm.deletePlaylist()
                showAlertDialog = false
                showEditSheet = false
            }
        )

        if (type == PlaylistType.PLAYLIST && playlistInfo.value != null) {
            val info = playlistInfo.value!!
            PlaylistInfoBottomSheet(
                playlistName = info.name,
                songCount = info.count,
                createDate = formatDate(info.createTime),
                description = info.describe ?: "还没有简介哦",
                show = showEditSheet,
                onDismissRequest = { showEditSheet = false },
                onDeleteClick = { showAlertDialog = true }
            )
        }
    }
}

@Composable
private fun SongInfo(
    name: String,
    count: Int,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 5.dp),
    ) {
        Image(
            painterResource(Res.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
                .shadow(10.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier.weight(1f).height(100.dp)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                lineHeight = 45.sp,
                fontWeight = FontWeight.Bold
            )
            Text("$count 首音乐", fontSize = 16.sp)
        }
    }
}

class PlaylistViewModel(
    val id: Long,
    type: PlaylistType,
    val title: String,
    val key: String,
    val navigator: Navigator
) :
    ScreenModel {
    val songDao = Rhythbox.dataBase.getSongDao()
    val playlistDao = Rhythbox.dataBase.getPlaylistDao()
    private val _playlistInfo = MutableStateFlow<PlaylistEntity?>(null)
    val playlistInfo = _playlistInfo.asStateFlow()

    init {
        if (type == PlaylistType.PLAYLIST) {
            screenModelScope.launch {
                _playlistInfo.value = playlistDao.get(id)
            }
        }
    }

    private val player = Rhythbox.player
    val info = when (type) {
        PlaylistType.ALBUM -> songDao.getByAlbumId(id)
        PlaylistType.ARTIST -> songDao.getBySingerId(id)
        PlaylistType.DIR -> songDao.getDirId(id)
        PlaylistType.PLAYLIST -> songDao.getPlayListId(id)
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val songList = when (type) {
        PlaylistType.ALBUM -> songDao.getAlbum(title)
        PlaylistType.ARTIST -> songDao.getArtist(title)
        PlaylistType.DIR -> songDao.getDir(title)
        PlaylistType.PLAYLIST -> songDao.getPlayListSong(id)
    }.onEach {
        resetMediaItems()
    }.map { rawList ->
        rawList.map { song ->
            song.copy(
                uuid = generateMediaId(key, song)
            )
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun play(index: Int = 0) {
        player.let { p ->
            p.resetMediaItems(key, songList.value, false)
            p.prepare(index)
            p.play()
        }
    }

    fun resetMediaItems() {
        screenModelScope.launch {
            delay(50)
            player.resetMediaItems(key, songList.value, false)
        }
    }

    fun deletePlaylist() {
        screenModelScope.launch {
            try {
                playlistDao.deletePlaylistWithSongs(id)
                navigator.pop()
            } catch (e: Exception) {
                SnackbarManager.show("删除失败： ${e.message}")
            }
        }
    }
}

