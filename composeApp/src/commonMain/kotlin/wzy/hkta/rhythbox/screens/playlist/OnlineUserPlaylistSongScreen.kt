package wzy.hkta.rhythbox.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import coil3.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.more
import rhythbox.composeapp.generated.resources.music
import wzy.hkta.rhythbox.components.MAlertDialog
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.playlist.EditOnlinePlaylist
import wzy.hkta.rhythbox.components.playlist.PlaylistInfoBottomSheet
import wzy.hkta.rhythbox.components.song.OnlineSongBottomSheet
import wzy.hkta.rhythbox.components.song.SortSongBottomSheet
import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.Rhythbox.player
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.MUSIC_BASE_URL
import wzy.hkta.rhythbox.fatchMusic.PlaylistFields
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.utils.formatDate
import wzy.hkta.rhythbox.utils.sortSong
import kotlin.time.Duration.Companion.milliseconds

class OnlineUserPlaylistSongScreen(
    val title: String,
    val id: Long,
) : Screen {
    @Composable
    override fun Content() {
        val density = LocalDensity.current
        val navigator = LocalNavigator.currentOrThrow
        val key = "${title}_${id}_OnlineUserPlaylistSong"
        val vm = rememberScreenModel(tag = key) {
            OnlineUserPlaylistSongViewModel(
                id,
                title,
                key,
                navigator
            )
        }

        val songList by vm.songList.collectAsState()
        val playlistInfo by vm.playlistInfo.collectAsState()
        val sortEntity by vm.sortEntity.collectAsState()
        var showSongBottomSheet by remember { mutableStateOf(false) }
        var showSortSongBottomSheet by remember { mutableStateOf(false) }
        var showAddOnlinePlaylist by remember { mutableStateOf(false) }
        var showPlaylistInfoBottomSheet by remember { mutableStateOf(false) }
        var showAlertDialog by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf<Song?>(null) }
        val playMediaId by player.playMediaId.collectAsState()
        val isPlaying by player.isPlaying.collectAsState()
        val scope = rememberCoroutineScope()

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxWidth = constraints.maxWidth.toFloat()
            val maxHeight = with(density) { constraints.maxHeight.toDp() }
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(290.dp).background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFFA0B69E),
                                1.00f to Color(0xFFA0B69E).copy(alpha = 0.3f)
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
                                showPlaylistInfoBottomSheet = true
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.more),
                                    tint = Color.White,
                                    contentDescription = "更多",
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    )

                    playlistInfo?.let { info ->
                        OnlineUserPlaylistCardInfo(
                            name = info.title,
                            count = songList.size,
                            cover = info.cover?.let { "$MUSIC_BASE_URL/${it.url}" },
                            description = info.description,
                            modifier = Modifier.weight(1f)
                        )
                    }

                }
                Column(
                    modifier = Modifier
                        .height(maxHeight - 260.dp)
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
                    PlaylistSongTopBar(
                        showISLike = false,
                        sortEntity = sortEntity,
                        onSortClick = { showSortSongBottomSheet = true },
                        onPlay = { vm.play(it) }
                    )
                    PlaylistSongList(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        songList = songList,
                        navigator = navigator,
                        isPlaying = isPlaying,
                        playMediaId = playMediaId,
                        onPlay = {
                            vm.play(it)
                        },
                        onMoreClick = {
                            selected = it
                            showSongBottomSheet = true
                        }
                    )
                }
            }
        }

        if (selected != null) {
            OnlineSongBottomSheet(
                show = showSongBottomSheet,
                songId = selected!!.id,
                artists = selected!!.artists,
                songName = selected!!.title,
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


        playlistInfo?.let { info ->
            PlaylistInfoBottomSheet(
                playlistName = info.title,
                songCount = songList.size,
                createDate = formatDate(info.createdAt!!),
                description = info.description,
                show = showPlaylistInfoBottomSheet,
                onDismissRequest = { showPlaylistInfoBottomSheet = false },
                onDeleteClick = { showAlertDialog = true },
                onEditClick = {
                    showPlaylistInfoBottomSheet = false
                    showAddOnlinePlaylist = true
                }
            )

            MAlertDialog(
                show = showAlertDialog,
                title = "提示",
                content = "确定要删除此歌单吗",
                onCancel = { showAlertDialog = false },
                onConfirm = {
                    showPlaylistInfoBottomSheet = false
                    showAlertDialog = false
                    scope.launch {
                        try {
                            ToastManager.showLoading("删除歌单中。。。")
                            FetchMusic.user.deletePlaylist(id)
                            ToastManager.show("删除成功")
                            navigator.pop()
                        } catch (e: Exception) {
                            ToastManager.show("删除失败：${e.message}")
                        }
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

        EditOnlinePlaylist(
            playlistId = id,
            show = showAddOnlinePlaylist,
            onCancel = { showAddOnlinePlaylist = false },
            onSuccess = {
                showAddOnlinePlaylist = false
                vm.getPlayListInfo()
            }
        )
    }


}

@Composable
private fun OnlineUserPlaylistCardInfo(
    name: String,
    count: Int,
    cover: String?,
    description: String?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 封面 + 歌单信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 歌单封面
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .shadow(8.dp, shape)
                    .clip(shape)
            ) {
                AsyncImage(
                    model = cover?.takeIf { it.isNotBlank() }
                        ?: Res.drawable.logo,
                    contentDescription = "$name 封面",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(14.dp))

            // 右侧信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // 歌曲数量
                PlaylistInfoItem(
                    icon = Res.drawable.music,
                    text = "$count 首音乐"
                )
            }
        }

        // 歌单简介
        Text(
            text = description
                ?.takeIf { it.isNotBlank() }
                ?: "暂无简介",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Color.White.copy(alpha = 0.95f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


class OnlineUserPlaylistSongViewModel(
    val id: Long,
    val title: String,
    val key: String,
    val navigator: Navigator
) : ScreenModel {
    private val _playlistInfo = MutableStateFlow<PlaylistFields?>(null)
    private val _songList = MutableStateFlow<List<Song>>(mutableStateListOf())
    private val _sortEntity = MutableStateFlow(PlaylistSortEntity(sortKey = key))
    val playlistInfo = _playlistInfo.asStateFlow()
    val sortEntity = _sortEntity.asStateFlow()
    private val player = Rhythbox.player


    init {
        getPlayListInfo()
    }


    val songList = combine(
        _songList,
        Rhythbox.dataBase.getPlayListSortDao().get(key)
    ) { songs, sortConfig ->
        if (sortConfig == null) {
            songs
        } else {
            _sortEntity.value = sortConfig
            val list = sortSong(songs.toList(), sortConfig.sortName, sortConfig.sortType)
            if (player.playKey.value == key) {
                player.sortMediaItems(SongListType.NetworkList(list.toMutableList()))
            }
            list
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun getPlayListInfo() {
        screenModelScope.launch {
            try {
                val infoDeferred = async { FetchMusic.user.getPlayListInfo(id) }
                val songListDeferred = async { FetchMusic.user.getPlayListSong(id) }
                _playlistInfo.value = infoDeferred.await()
                _songList.value = songListDeferred.await()
            } catch (err: Exception) {
                println("error : $err")
            }
        }
    }

    fun play(index: Int = 0) {
        player.let { p ->
            p.resetMediaItems(
                key,
                SongListType.NetworkList(songList.value.toMutableList()),
                false,
                index
            )
            p.prepare(index)
            p.play()
        }
    }

    fun resetMediaItems() {
        screenModelScope.launch {
            delay(50.milliseconds)
            player.resetMediaItems(
                key,
                SongListType.NetworkList(songList.value.toMutableList()),
                true
            )
        }
    }
}

