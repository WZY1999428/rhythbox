package wzy.hkta.rhythbox.screens.playlist

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.like_grey
import rhythbox.composeapp.generated.resources.like_pink
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.more
import rhythbox.composeapp.generated.resources.music
import rhythbox.composeapp.generated.resources.play
import rhythbox.composeapp.generated.resources.play3
import rhythbox.composeapp.generated.resources.sort1
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.song.OnlineSongBottomSheet
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.song.SongItem
import wzy.hkta.rhythbox.components.song.SortSongBottomSheet
import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.Rhythbox.player
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.PlaylistInfo
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.formatPlayTotal
import wzy.hkta.rhythbox.utils.isCurrentPlaying
import wzy.hkta.rhythbox.utils.sortSong
import kotlin.time.Duration.Companion.milliseconds

class OnlinePlaylistSongScreen(
    val title: String,
    val id: Long,
) : Screen {
    @Composable
    override fun Content() {
        val density = LocalDensity.current
        val navigator = LocalNavigator.currentOrThrow
        val key = "${title}_${id}_OnlinePlaylistSong"
        val vm = rememberScreenModel(tag = key) {
            OnlinePlaylistSongViewModel(
                id,
                title,
                key,
                navigator
            )
        }

        val playlistInfo by vm.playlistInfo.collectAsState()
        val songList by vm.songList.collectAsState()
        val sortEntity by vm.sortEntity.collectAsState()

        var showSongBottomSheet by remember { mutableStateOf(false) }
        var showSortSongBottomSheet by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf<SongType?>(null) }
        val playMediaId by player.playMediaId.collectAsState()
        val isPlaying by player.isPlaying.collectAsState()

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
                        PlaylistCardInfo(
                            name = info.title,
                            count = songList.size,
                            cover = info.cover,
                            description = info.description,
                            playTotal = info.playTotal,
                            favoriteTotal = info.favoriteTotal,
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
                        isLike = playlistInfo?.isLike == true,
                        togglePlaylistFavorite = { vm.togglePlaylistFavorite() },
                        onSortClick = { showSortSongBottomSheet = true },
                        sortEntity = sortEntity,
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


            SortSongBottomSheet(
                show = showSortSongBottomSheet,
                sortKey = key,
                onDismissRequest = {
                    showSortSongBottomSheet = false
                }
            )

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
}


@Composable
fun PlaylistSongTopBar(
    isLike: Boolean = false,
    showISLike: Boolean = true,
    togglePlaylistFavorite: () -> Unit = {},
    onSortClick: () -> Unit = {},
    sortEntity: PlaylistSortEntity,
    onPlay: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // 增加整体左右边距，不那么贴边
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp) // 按钮之间的间距
        ) {
            // 1. 播放全部按钮
            Button(
                modifier = Modifier
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp), // 更圆润的胶囊形状
                contentPadding = PaddingValues(horizontal = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = { onPlay(0) }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.play3),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White

                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "播放全部",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

//            val isLiked = playlistInfo?.isLike == true
            if (showISLike) {
                // 2. 优化后的精致收藏按钮（带柔和的背景和圆角）
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isLike) Color(0xFFFFEBEE) // 已收藏时显示极浅的粉红色底
                            else Color(0xFFF5F5F5)        // 未收藏时显示浅灰色底
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = { togglePlaylistFavorite() }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isLike) Res.drawable.like_pink else Res.drawable.like_grey
                            ),
                            modifier = Modifier.size(18.dp),
                            contentDescription = "收藏歌单",
                            tint = if (isLike) Color(0xFFE91E63) else Color(0xFF757575)
                        )
                    }
                }
            }

        }

        // 3. 右侧排序按钮（保持风格统一）
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(0.3f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = { onSortClick() }
            ) {
                val rotate = when (sortEntity.sortType == "desc") {
                    true -> 0
                    false -> 180
                }
                Icon(
                    painter = painterResource(Res.drawable.sort1),
                    modifier = Modifier.size(16.dp).rotate(rotate.toFloat()),
                    contentDescription = "排序",
                    tint = MaterialTheme.colorScheme.primary
                )

            }
        }
    }
}


@Composable
fun PlaylistSongList(
    songList: List<SongType>,
    navigator: Navigator,
    playMediaId: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlay: (Int) -> Unit = {}, // onPlay
    onMoreClick: (SongType) -> Unit = {},
) {
    if (songList.isEmpty()) {
        RhythboxSongEmptyState(
            describe = "该歌单没有音乐",
            buttonText = "返回",
            onButtonClick = {
                navigator.pop()
            }
        )
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(songList.size) {
                val item = songList[it]
                SongItem(
                    title = item.name,
                    subTitle = item.artist,
                    duration = item.duration,
                    isPlay = isCurrentPlaying(playMediaId, item.mediaId),
                    isPlaying = isPlaying,
                    no = it + 1,
                    onMoreClick = { onMoreClick(item) },
                    onClick = { onPlay(it) }
                )
            }
        }
        if (playMediaId != null) {
            BottomControl()
        }
    }
}

@Composable
private fun PlaylistCardInfo(
    name: String,
    count: Int,
    cover: String?,
    description: String?,
    playTotal: Long,
    favoriteTotal: Long,
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

                // 播放量 + 收藏量
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlaylistInfoItem(
                        icon = Res.drawable.play,
                        text = formatPlayTotal(playTotal)
                    )

                    PlaylistInfoItem(
                        icon = Res.drawable.like_grey,
                        text = formatPlayTotal(favoriteTotal)
                    )
                }
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

/**
 * 歌单统计信息
 */
@Composable
fun PlaylistInfoItem(
    icon: DrawableResource,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(12.dp)
        )

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

class OnlinePlaylistSongViewModel(
    val id: Long,
    val title: String,
    val key: String,
    val navigator: Navigator
) : ScreenModel {
    private val _playlistInfo = MutableStateFlow<PlaylistInfo?>(null)
    private val _songList = MutableStateFlow<List<SongType>>(mutableStateListOf())
    private val _sortEntity = MutableStateFlow(PlaylistSortEntity(sortKey = key))

    val playlistInfo = _playlistInfo.asStateFlow()
    val sortEntity = _sortEntity.asStateFlow()

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
                player.sortMediaItems(list)
            }
            list
        }
    }.onEach {
        if (player.playKey.value == key) {
            resetMediaItems()
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val player = Rhythbox.player
    private val playKey = player.playKey;
    fun getPlayListInfo() {
        screenModelScope.launch {
            try {
                // 1. 同时发起两个异步请求，不立刻 await，让它们并发运行
                val infoDeferred = async { FetchMusic.music.getPlayListInfo(id) }
                val songListDeferred = async { FetchMusic.music.getPlayListSong(id) }
                _playlistInfo.value = infoDeferred.await()
                _songList.value = songListDeferred.await().map { song -> SongType.Network(song) }
            } catch (err: Exception) {

            }
        }
    }


    fun togglePlaylistFavorite() {
        try {
            screenModelScope.launch {
                val status = FetchMusic.music.togglePlaylistFavorite(id)
                _playlistInfo.value = playlistInfo.value?.copy(isLike = status)
            }
        } catch (err: Exception) {
            println("err $err")
        }
    }

    fun play(index: Int = 0) {
        if (playKey.value != key) {
            screenModelScope.launch {
                FetchMusic.music.incrementPlaylistPlayTotal(id)
            }
        }
        player.resetMediaItems(key, songList.value, false) {
            player.prepare(index)
            player.play()
        }
    }

    fun resetMediaItems() {
        screenModelScope.launch {
            delay(50.milliseconds)
            player.resetMediaItems(key, songList.value, true)
        }
    }
}

