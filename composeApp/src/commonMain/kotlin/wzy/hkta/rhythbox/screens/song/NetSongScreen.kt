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
import rhythbox.composeapp.generated.resources.defaultpage
import rhythbox.composeapp.generated.resources.error
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
import wzy.hkta.rhythbox.database.PlayHistoryEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.screens.ScanMusicScreen
import wzy.hkta.rhythbox.screens.SongMetadataScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistSquareScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.fatchMusic.Banner
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.Song
import wzy.hkta.rhythbox.utils.generateMediaId
import wzy.hkta.rhythbox.utils.getSortType
import wzy.hkta.rhythbox.utils.isCurrentPlaying
import kotlin.collections.emptyList
import kotlin.time.Duration.Companion.milliseconds

class NetSongScreen(
    override val key: String = "songScreen",
    val title: String = "歌曲",
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel(tag = "${title}_${key}") { NetSongViewModel(key) }
        val songList = vm.songList.collectAsState()
        val songCount = vm.songCount.collectAsState()
        val isError = vm.isError.collectAsState()
        val isLoading = vm.isLoading.collectAsState()

        val player = Rhythbox.player
        val playUuid = player.playUuid.collectAsState()
        val isPlaying = player.isPlaying.collectAsState()

        var showSongBottomSheet by remember { mutableStateOf(false) }
        var showSortSongBottomSheet by remember { mutableStateOf(false) }
        var selectSong by remember { mutableStateOf<Song?>(null) }

        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(title = title)
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
                }

                if (songList.value.isEmpty()) {
                    when(isLoading.value){
                        true->{

                        }
                        false->{
                            RhythboxSongEmptyState(
                                describe = when(isError.value){
                                    true->"请求失败，无法连接服务器~"
                                    false -> "这里什么都没有哦~"
                                },
                                image = when(isError.value){
                                    true->Res.drawable.error
                                    false -> Res.drawable.defaultpage
                                },
                                buttonText = when (key) {
                                    "recently" -> "去广场看看"
                                    else -> if(isError.value){"刷新页面"}else{"我知道了"}
                                },
                                onButtonClick = {
                                    when(isError.value){
                                        true->vm.getSongList()
                                        false -> when (key) {
                                            "recently" -> navigator.push(PlaylistSquareScreen())
                                            else -> navigator.pop()
                                        }
                                    }
                                }
                            )
                        }
                    }

                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(songList.value.size) {
                            val item = songList.value[it]
                            SongItem(
                                no = it + 1,
                                title = item.title,
                                subTitle = item.artists.joinToString("/") {ar-> ar.artistName },
                                duration = item.duration,
                                isPlay = isCurrentPlaying(playUuid.value, item.uuid),
                                isPlaying = isPlaying.value,
                                onMoreClick = {
                                    selectSong = item
                                    showSongBottomSheet = true
                                },
                                onClick = {
                                    vm.play(it,item.id)
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
                songName = selectSong!!.title,
                cover = selectSong!!.cover,
                songId = selectSong!!.id,
                artistName = selectSong!!.artists.joinToString("/") {ar-> ar.artistName },
                onDismissRequest = { showSongBottomSheet = false },
                onEdit = { navigator.push(SongMetadataScreen(selectSong!!.id)) },
                onDeleteClick = {
                    if (songCount.value == 0) {
                        navigator.pop()
                    }
                }
            )
        }
    }
}

private class NetSongViewModel(val key: String) : ScreenModel {
    private val player = Rhythbox.player
    var songDao = Rhythbox.dataBase.getSongDao()
    private val _songList = MutableStateFlow<List<Song>>(emptyList())
    private val _isError = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _songCount = MutableStateFlow(0)

    val isLoading=_isLoading.asStateFlow()
    val songList=_songList.asStateFlow()
    val songCount=_songCount.asStateFlow()
    val isError= _isError.asStateFlow()

    init {
        getSongList()
    }

    fun getSongList(){
        screenModelScope.launch {
           try {
               _isError.value=false
               val songs= when(key){
                   "dailyRecommend" -> FetchMusic.fetchRecommendationsDaily()
                   "leaderboard" -> FetchMusic.getLeaderboard()
                   "chinese","indie","lightMusic" -> FetchMusic.getPlaylistFeatured(key)
                   "recently" -> getRecently()
                   else -> emptyList()
               }
               _songList.value= songs
               _songCount.value=songs.size
           }catch (err: Exception){
               _isError.value=true
               println(err)
           }
        }
    }

    fun play(index: Int = 0,id: Long=0) {
        screenModelScope.launch {
            songDao.deleteOldPlayHistory()
            songDao.insertPlayHistory(PlayHistoryEntity(songId = id))
        }

        player.let { p ->
            p.resetMediaItems(key,SongListType.NetworkList(songList.value),true,index)
            p.play()
        }
    }

    suspend fun getRecently(): List<Song> {
        val historyList= songDao.getPlayHistory()
        if(historyList.isNotEmpty()){
            val ids=historyList.map { it.songId }
            return FetchMusic.getSongByIds(ids)
        }
        return emptyList()
    }
}