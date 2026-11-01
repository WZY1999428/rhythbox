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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil3.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.like_grey
import rhythbox.composeapp.generated.resources.play
import rhythbox.composeapp.generated.resources.shuffle
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.SongItem
import wzy.hkta.rhythbox.database.PlayHistoryEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.fatchMusic.Artist
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.Song
import wzy.hkta.rhythbox.utils.isCurrentPlaying

class NetArtistSongScreen(
    val artistId: Long,
    val title: String
) : Screen {
    @Composable
    override fun Content() {

        val player = Rhythbox.player
        val isPlaying = player.isPlaying.collectAsState()
        val playUuid = player.playUuid.collectAsState()

        val vm = rememberScreenModel {
            NetSingerViewModel(artistId, "artist-song-$artistId")
        }

        val songList by vm.songList.collectAsState()
        val artistInfo by vm.artistInfo.collectAsState()

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
                        onShuffle = {
                            if (songList.isNotEmpty()){
                                vm.shufflePlay()
                            }
                        },
                        onFavorite = {
                            // 收藏歌手
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            Spacer(Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(songList.size, key = { songList[it].id }) { index ->
                    val song = songList[index]
                    SongItem(
                        isPlay = isCurrentPlaying(playUuid.value, song.uuid),
                        isPlaying = isPlaying.value,
                        no = index + 1,
                        title = song.title,
                        subTitle = song.artists.joinToString("/") { it.artistName },
                        duration = song.duration,
                        onMoreClick = {},
                        onClick = {
                            vm.play(index, song.id)
                        },
                    )
                }
            }
            if (isPlaying.value) {
                BottomControl()
            }
        }
    }

}


@Composable
private fun ArtistHeader(
    artist: Artist,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
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
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("播放全部")
            }

            OutlinedButton(
                onClick = onShuffle
            ) {
                Icon(
                    painter = painterResource(Res.drawable.shuffle),
                    null,
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(onClick = onFavorite) {
                Icon(
                    painter = painterResource(Res.drawable.like_grey),
                    null,
                    modifier = Modifier.size(14.dp)
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


private class NetSingerViewModel(val id: Long, val key: String) : ScreenModel {
    private val player = Rhythbox.player
    private var songDao = Rhythbox.dataBase.getSongDao()

    private var _artistInfo = MutableStateFlow<Artist?>(null)
    private var _songList = MutableStateFlow<List<Song>>(emptyList())

    val songList = _songList.asStateFlow()
    val artistInfo = _artistInfo.asStateFlow()


    fun getData() {
        screenModelScope.launch {
            try {
                val artistFuture = async {
                    FetchMusic.getArtist(id)
                }

                val songFuture = async {
                    FetchMusic.getArtistSong(id)
                }

                val artist = artistFuture.await()
                val songs = songFuture.await()

                _artistInfo.value = artist
                _songList.value = songs

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun play(index: Int = 0, id: Long = 0) {
        screenModelScope.launch {
            songDao.deleteOldPlayHistory()
            songDao.insertPlayHistory(PlayHistoryEntity(songId = id))
        }

        player.let { p ->
            p.resetMediaItems(key, SongListType.NetworkList(songList.value), true, index)
            p.play()
        }
    }

    fun shufflePlay() {
        player.let { p ->
            p.resetMediaItems(key, SongListType.NetworkList(songList.value), true)
            p.shufflePlay()
        }
    }

}