package wzy.hkta.rhythbox.screens.artist


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.album
import rhythbox.composeapp.generated.resources.like_grey
import rhythbox.composeapp.generated.resources.like_pink
import rhythbox.composeapp.generated.resources.play
import rhythbox.composeapp.generated.resources.sort1
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.song.OnlineSongBottomSheet
import wzy.hkta.rhythbox.components.song.SongItem
import wzy.hkta.rhythbox.components.song.SortSongBottomSheet
import wzy.hkta.rhythbox.database.PlayHistoryEntity
import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.screens.album.OnlineAlbumScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.fatchMusic.Artist
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.isCurrentPlaying
import wzy.hkta.rhythbox.utils.sortSong

class OnlineArtistSongScreen(
    val artistId: Long,
    val title: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val player = Rhythbox.player
        val isPlaying = player.isPlaying.collectAsState()
        val playMediaId = player.playMediaId.collectAsState()
        val key = "artist-song-$artistId"
        val vm = rememberScreenModel(key) {
            OnlineSingerViewModel(artistId, key)
        }

        val songList by vm.songList.collectAsState()
        val artistInfo by vm.artistInfo.collectAsState()
        val sortEntity by vm.sortEntity.collectAsState()

        var showSongBottomSheet by remember { mutableStateOf(false) }
        var showSortSongBottomSheet by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf<SongType?>(null) }
        LaunchedEffect(artistId) {
            vm.getData()
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box {
                NavBar(
                    title = title,
                    background = Color.Transparent,
                    modifier = Modifier.zIndex(1f)
                )
                if (artistInfo != null) {
                    ArtistCover(artistInfo?.cover)
                    ArtistHeader(
                        artist = artistInfo!!,
                        onPlayAll = {
                            if (songList.isNotEmpty()) {
                                vm.play(0, songList[0].id)
                            }
                        },
                        onToAlbums = {
                            navigator.push(OnlineAlbumScreen(artistId))
                        },
                        onFavorite = {
                            vm.togglePlaylistFavorite()
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = "热门歌曲",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "${songList.size} 首",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = { showSortSongBottomSheet = true }
                    ) {
                        val rotate = when (sortEntity.sortType == "desc") {
                            true -> 0
                            false -> 180
                        }
                        Icon(
                            painter = painterResource(Res.drawable.sort1),
                            modifier = Modifier.size(16.dp).rotate(rotate.toFloat()),
                            contentDescription = "排序",
                            tint = Color(0xFFA0B69E)
                        )

                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(songList.size, key = { songList[it].id }) { index ->
                    val song = songList[index]
                    SongItem(
                        isPlay = isCurrentPlaying(playMediaId.value, song.mediaId),
                        isPlaying = isPlaying.value,
                        no = index + 1,
                        title = song.name,
                        subTitle = song.artist ?: "",
                        duration = song.duration,
                        onMoreClick = {
                            selected = song
                            showSongBottomSheet = true
                        },
                        onClick = {
                            vm.play(index, song.id)
                        },
                    )
                }
            }
            if (playMediaId.value != null) {
                BottomControl()
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


@Composable
private fun ArtistHeader(
    artist: Artist,
    onPlayAll: () -> Unit,
    onToAlbums: () -> Unit,
    onFavorite: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = artist.avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artist.name,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = listOfNotNull(
                        artist.gender,
                        artist.pinyin
                    ).joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = artist.realName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = artist.description,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable {
                expanded = !expanded
            }
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.play),
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(4.dp))
                Text("播放全部", color = Color.White)
            }
            OutlinedButton(
                onClick = onToAlbums // 改为跳转到专辑的回调
            ) {
                Icon(
                    painter = painterResource(Res.drawable.album), // 换成你的专辑图标
                    contentDescription = "专辑",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("歌手专辑", fontSize = 13.sp) // 如果空间够，可以带上文字“专辑”；如果空间紧张，也可以只保留图标
            }
            val isLiked = artist.isLike
            IconButton(onClick = onFavorite) {
                Icon(
                    painter = painterResource(
                        if (isLiked) Res.drawable.like_pink else Res.drawable.like_grey
                    ),
                    modifier = Modifier.size(18.dp),
                    contentDescription = "收藏歌单",
                    tint = if (isLiked) Color(0xFFE91E63) else Color(0xFF757575)
                )
            }
        }
    }
}


@Composable
private fun ArtistCover(cover: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        AsyncImage(
            model = cover,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}


private class OnlineSingerViewModel(val id: Long, val key: String) : ScreenModel {
    private val player = Rhythbox.player
    private var songDao = Rhythbox.dataBase.getSongDao()

    private var _artistInfo = MutableStateFlow<Artist?>(null)
    private var _songList = MutableStateFlow<List<SongType>>(emptyList())
    private val _sortEntity = MutableStateFlow(PlaylistSortEntity(sortKey = key))

    val sortEntity = _sortEntity.asStateFlow()
    val artistInfo = _artistInfo.asStateFlow()


    fun getData() {
        screenModelScope.launch {
            try {
                val artistFuture = async {
                    FetchMusic.music.getArtist(id)
                }

                val songFuture = async {
                    FetchMusic.music.getArtistSong(id)
                }

                val artist = artistFuture.await()
                val songs = songFuture.await()

                _artistInfo.value = artist
                _songList.value = songs.map { SongType.Network(it) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    fun togglePlaylistFavorite() {
        try {
            screenModelScope.launch {
                val status = FetchMusic.music.toggleArtistFavorite(id)
                _artistInfo.value = artistInfo.value?.copy(isLike = status)
            }
        } catch (err: Exception) {
            println("err $err")
        }
    }

    fun play(index: Int = 0, id: Long = 0) {
        screenModelScope.launch {
            songDao.deleteOldPlayHistory()
            songDao.insertPlayHistory(PlayHistoryEntity(songId = id))
        }
        player.resetMediaItems(key, songList.value, false) {
            player.prepare(index)
            player.play()
        }
    }

}