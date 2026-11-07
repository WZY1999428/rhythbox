package wzy.hkta.rhythbox.screens.playlist

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.more
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.MAlertDialog
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.playlist.EditPlaylist
import wzy.hkta.rhythbox.components.playlist.PlaylistInfoBottomSheet
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.song.SongBottomSheet
import wzy.hkta.rhythbox.components.song.SongItem
import wzy.hkta.rhythbox.components.song.SortSongBottomSheet
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.screens.ScanMusicScreen
import wzy.hkta.rhythbox.screens.SongMetadataScreen
import wzy.hkta.rhythbox.screens.song.SongScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.Rhythbox.player
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.formatDate
import wzy.hkta.rhythbox.utils.generateMediaId
import wzy.hkta.rhythbox.utils.isCurrentPlaying
import wzy.hkta.rhythbox.utils.sortSong
import kotlin.collections.map
import kotlin.time.Duration.Companion.milliseconds

enum class PlaylistType {
    PLAYLIST,
    ALBUM,
    ARTIST,
    DIR
}

class PlaylistSongScreen(
    val title: String,
    val id: Long,
    val type: PlaylistType
) : Screen {

    @Composable
    override fun Content() {
        val density = LocalDensity.current
        val navigator = LocalNavigator.currentOrThrow
        val key = "${title}_${id}_${type}"
        val vm = rememberScreenModel(tag = key) {
            PlaylistSongModel(id, type, title, key, navigator)
        }
        val playlistInfo by vm.playlistInfo.collectAsState()
        val sortEntity by vm.sortEntity.collectAsState()
        var showEditSheet by remember { mutableStateOf(false) }
        var showAlertDialog by remember { mutableStateOf(false) }
        var showSongBottomSheet by remember { mutableStateOf(false) }
        var playlistInfoBottomSheet by remember { mutableStateOf(false) }
        var showSortSongBottomSheet by remember { mutableStateOf(false) }
        var selectSong by remember { mutableStateOf<SongEntity?>(null) }

        val info = vm.info.collectAsState()
        val songList = vm.songList.collectAsState()
        val playMediaId = player.playMediaId.collectAsState()
        val isPlaying = player.isPlaying.collectAsState()

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxWidth = constraints.maxWidth.toFloat()
            val maxHeight = with(density) { constraints.maxHeight.toDp() }

            Box(modifier = Modifier.fillMaxSize()) {
                // 顶部渐变背景和头部信息
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            Brush.linearGradient(
                                colorStops = arrayOf(
                                    0f to Color(0xFFA0B69E),
                                    1.00f to Color(0xFFA0B69E).copy(alpha = 0.3f)
                                ),
                                start = Offset.Zero,
                                end = Offset(x = maxWidth, y = 300f)
                            )
                        ),
                ) {
                    NavBar(
                        title = title,
                        iconColor = Color.White,
                        titleColor = Color.White,
                        background = Color.Transparent,
                        action = {
                            if (type == PlaylistType.PLAYLIST) {
                                IconButton(onClick = { showEditSheet = true }) {
                                    Icon(
                                        painterResource(Res.drawable.more),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    )
                    info.value?.let {
                        SongInfo(it.name, it.count)
                    }
                }

                // 下方歌单内容白卡片
                Column(
                    modifier = Modifier
                        .height(maxHeight - 210.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            clip = false
                        )
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .align(Alignment.BottomStart)
                ) {

                    PlaylistSongTopBar(
                        showISLike = false,
                        onSortClick = { showSortSongBottomSheet = true },
                        sortEntity = sortEntity,
                        onPlay = { vm.play(it) }
                    )

                    if (songList.value.isEmpty()) {
                        Box(modifier = Modifier.weight(1f)) {
                            RhythboxSongEmptyState(
                                describe = when (type) {
                                    PlaylistType.ALBUM -> "这张专辑的磁带还没录入声音，快去给它填满曲目吧~"
                                    PlaylistType.ARTIST -> "这位音乐人还在创作中，舞台正静候旋律响起~"
                                    PlaylistType.DIR -> "这个文件夹静悄悄的，小精灵还没在这里搜刮到音符~"
                                    PlaylistType.PLAYLIST -> "新建的宝藏歌单空空如也，快把心爱的旋律装进盒子里吧~"
                                },
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
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(songList.value.size) { index ->
                                val item = songList.value[index]
                                SongItem(
                                    title = item.name,
                                    subTitle = item.artist ?: "未知歌手",
                                    duration = item.duration,
                                    isPlay = isCurrentPlaying(playMediaId.value, item.mediaId),
                                    isPlaying = isPlaying.value,
                                    no = index + 1,
                                    onMoreClick = {
                                        selectSong = item
                                        showSongBottomSheet = true
                                    },
                                    onClick = {
                                        vm.play(index)
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

        if (type == PlaylistType.PLAYLIST) {
            playlistInfo?.let { info ->
                PlaylistInfoBottomSheet(
                    playlistName = info.name,
                    songCount = info.count,
                    createDate = formatDate(info.createTime),
                    description = info.describe ?: "还没有简介哦",
                    show = showEditSheet,
                    onDismissRequest = { showEditSheet = false },
                    onDeleteClick = { showAlertDialog = true },
                    onEditClick = {
                        showEditSheet = false
                        playlistInfoBottomSheet = true
                    }
                )

                EditPlaylist(
                    playlistId = info.id,
                    show = playlistInfoBottomSheet,
                    onCancel = { playlistInfoBottomSheet = false },
                    onSuccess = {
                        playlistInfoBottomSheet = false
                    }
                )
            }

        }

        SortSongBottomSheet(
            show = showSortSongBottomSheet,
            sortKey = key,
            onDismissRequest = { showSortSongBottomSheet = false }
        )
    }
}

@Composable
private fun SongInfo(
    name: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 优化封面精致感
        Image(
            painterResource(Res.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$count 首音乐",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

class PlaylistSongModel(
    val id: Long,
    type: PlaylistType,
    val title: String,
    val key: String,
    val navigator: Navigator
) : ScreenModel {
    val songDao = Rhythbox.dataBase.getSongDao()
    val playlistDao = Rhythbox.dataBase.getPlaylistDao()
    val playListSortDao = Rhythbox.dataBase.getPlayListSortDao()
    val playKey = Rhythbox.player.playKey
    private val _playlistInfo = MutableStateFlow<PlaylistEntity?>(null)
    val playlistInfo = _playlistInfo.asStateFlow()
    private val _sortEntity = MutableStateFlow(PlaylistSortEntity(sortKey = key))

    val sortEntity = _sortEntity.asStateFlow()

    init {
        if (type == PlaylistType.PLAYLIST) {
            screenModelScope.launch {
                _playlistInfo.value = playlistDao.get(id)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val songList = playListSortDao.get(key).flatMapLatest { sort ->
        val sortConfig = sort ?: PlaylistSortEntity(sortKey = key)
        val songs = when (type) {
            PlaylistType.ALBUM -> songDao.getAlbum(title)
            PlaylistType.ARTIST -> songDao.getArtist(title)
            PlaylistType.DIR -> songDao.getDir(title)
            PlaylistType.PLAYLIST -> songDao.getPlayListSong(id)
        }
        _sortEntity.value = sortConfig
        songs.map { rawList ->
            val list = rawList.map { song -> song.copy(mediaId = generateMediaId(key, song)) }
            sortSong(list, sortConfig.sortName, sortConfig.sortType)
        }
    }.onEach {
        if (playKey.value == key) resetMediaItems()
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = mutableStateListOf()
    )

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

    fun play(index: Int = 0) {
        player.let { p ->
            p.resetMediaItems(key, SongListType.LocalList(songList.value.toMutableList()), false)
            p.prepare(index)
            p.play()
        }
    }

    fun resetMediaItems() {
        screenModelScope.launch {
            delay(50.milliseconds)
            player.resetMediaItems(
                key,
                SongListType.LocalList(songList.value.toMutableList()),
                false
            )
        }
    }

    fun deletePlaylist() {
        screenModelScope.launch {
            try {
                playlistDao.deletePlaylistWithSongs(id)
                navigator.pop()
            } catch (e: Exception) {
                ToastManager.show("删除失败： ${e.message}")
            }
        }
    }
}