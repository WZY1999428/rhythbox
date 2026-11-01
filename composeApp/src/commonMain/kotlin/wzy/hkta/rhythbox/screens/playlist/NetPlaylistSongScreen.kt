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
import coil3.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import wzy.hkta.rhythbox.components.RhythboxSongEmptyState
import wzy.hkta.rhythbox.components.SongBottomSheet
import wzy.hkta.rhythbox.components.SongItem
import wzy.hkta.rhythbox.components.SortSongBottomSheet
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.screens.SongMetadataScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.Rhythbox.player
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.PlaylistInfo
import wzy.hkta.rhythbox.utils.fatchMusic.Song
import wzy.hkta.rhythbox.utils.formatPlayTotal
import wzy.hkta.rhythbox.utils.isCurrentPlaying
import kotlin.time.Duration.Companion.milliseconds

class NetPlaylistSongScreen(
    val title: String,
    val id: Long,
) : Screen {
    @Composable
    override fun Content() {
        val density = LocalDensity.current
        val navigator = LocalNavigator.currentOrThrow
        val key = "${title}_${id}_NetPlaylistSong"
        val vm =
            rememberScreenModel(tag = key) { NetPlaylistSongViewModel(id, title, key, navigator) }

        val songList = vm.songList.collectAsState()
        val playlistInfo = vm.playlistInfo.collectAsState()

        var showEditSheet by remember { mutableStateOf(false) }
        var showAlertDialog by remember { mutableStateOf(false) }
        var showSongBottomSheet by remember { mutableStateOf(false) }
        var showSortSongBottomSheet by remember { mutableStateOf(false) }
        var selectSong by remember { mutableStateOf<SongEntity?>(null) }
        val playUuid by player.playUuid.collectAsState()
        val isPlaying by player.isPlaying.collectAsState()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxWidth = constraints.maxWidth.toFloat()
            val maxHeight = with(density) { constraints.maxHeight.toDp() }
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(240.dp).background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFFA0B69E),
                                1.00f to Color(0xFFA0B69E).copy(alpha = 0.1f)
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
                                    tint = Color.Black,
                                    contentDescription = "更多",
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    )

                    playlistInfo.value?.let { info ->
                        PlaylistCardInfo(
                            name = info.title,
                            count = songList.value.size,
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
                                onClick = {
                                    vm.play(0)
                                }
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
                        IconButton(onClick = { showSortSongBottomSheet = true }) {
                            Icon(
                                painterResource(Res.drawable.sort1),
                                modifier = Modifier.size(16.dp),
                                contentDescription = null,
                                tint = Color(0xFFA0B69E)
                            )
                        }
                    }

                    if (songList.value.isEmpty()) {
                        RhythboxSongEmptyState(
                            describe = "该歌单没有音乐",
                            buttonText = "返回",
                            onButtonClick = {
                                navigator.pop()
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
                                    title = item.title,
                                    subTitle = item.artists.joinToString("/") { ar -> ar.artistName },
                                    duration = item.duration,
                                    isPlay = isCurrentPlaying(playUuid, item.uuid),
                                    isPlaying = isPlaying,
                                    no = it + 1,
                                    onMoreClick = {
//                                        selectSong = item
//                                        showSongBottomSheet = true
                                    },
                                    onClick = {
                                        vm.play(it)
                                    }
                                )
                            }
                        }
                        if (playUuid != null) {
                            BottomControl()
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
                onEdit = { navigator.push(SongMetadataScreen(selectSong!!.id)) },
                onDismissRequest = { showSongBottomSheet = false },
                onDeleteClick = {
//                    if (info.value?.count == 0) {
//                        navigator.pop()
//                    }
                }
            )
        }

        MAlertDialog(
            show = showAlertDialog,
            title = "提示",
            content = "确定要删除此歌单吗",
            onCancel = { showAlertDialog = false },
            onConfirm = {
                showAlertDialog = false
                showEditSheet = false
            }
        )

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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // 封面和歌单基本信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
            ) {
                if (cover != null) {
                    AsyncImage(
                        model = cover,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$count 首歌曲",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                // 播放量和收藏量
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "▶ ${formatPlayTotal(playTotal)}",
                        fontSize = 12.sp,
                        color = Color.White
                    )

                    Text(
                        text = "♡ ${formatPlayTotal(favoriteTotal)}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // 歌单简介
        Text(
            text = description.orEmpty().ifBlank { "暂无简介" },
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

class NetPlaylistSongViewModel(
    val id: Long,
    val title: String,
    val key: String,
    val navigator: Navigator
) : ScreenModel {
    private val _playlistInfo = MutableStateFlow<PlaylistInfo?>(null)
    private val _songList = MutableStateFlow<List<Song>>(emptyList())


    val playlistInfo = _playlistInfo.asStateFlow()
    val songList = _songList.asStateFlow()

    init {
        getPlayListInfo()
    }

    private val player = Rhythbox.player

    fun getPlayListInfo() {
        screenModelScope.launch {
            try {
                val infoDeferred = async { FetchMusic.getPlayListInfo(id) }
                val songListDeferred = async { FetchMusic.getPlayListSong(id) }
                _playlistInfo.value = infoDeferred.await()
                _songList.value = songListDeferred.await()
            } catch (err: Exception) {

            }
        }
    }

    fun play(index: Int = 0) {
        player.let { p ->
            p.resetMediaItems(key, SongListType.NetworkList(songList.value), false,index)
            p.prepare(index)
            p.play()
        }
    }

    fun resetMediaItems() {
        screenModelScope.launch {
            delay(50.milliseconds)
            player.resetMediaItems(key, SongListType.NetworkList(songList.value), false)
        }
    }
}

