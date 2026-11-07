package wzy.hkta.rhythbox.screens.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.screens.ScanMusicScreen
import wzy.hkta.rhythbox.fatchMusic.Album
import wzy.hkta.rhythbox.fatchMusic.FetchMusic

class OnlineAlbumScreen(val id: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel("online-album-$id") { OnlineAlbumViewModel(id) }
        val albumList by vm.albumList.collectAsState()
        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(title = "专辑")
            if (albumList.isEmpty()) {
                RhythboxSongEmptyState(
                    describe = "陈列架上空空如也，还没有收藏到精美的黑胶唱片呢~",
                    buttonText = "去扫描",
                    onButtonClick = {
                        navigator.push(ScanMusicScreen())
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(90.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(albumList.size) {
                        val item = albumList[it]
                        Column(
                            modifier = Modifier.clip(RoundedCornerShape(5.dp)).clickable {
                                navigator.push(
                                    OnlineAlbumSongScreen(id = item.id, item.title)
                                )
                            }
                        ) {
                            if (item.cover != null) {
                                AsyncImage(
                                    model = item.cover,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                        .clip(RoundedCornerShape(5.dp))
                                )
                            } else {
                                Image(
                                    painterResource(Res.drawable.logo),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                        .clip(RoundedCornerShape(5.dp))
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))
                            Column(modifier = Modifier.padding(horizontal = 3.dp)) {
                                Text(
                                    item.title,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    item.artists.joinToString("/") { artist -> artist.artistName },
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }


        }

    }
}

private class OnlineAlbumViewModel(val id: Long) : ScreenModel {
    private val _albumList = MutableStateFlow<List<Album>>(emptyList())

    val albumList = _albumList.asStateFlow()

    init {
        getAlbumList()
    }

    fun getAlbumList() {
        screenModelScope.launch {
            _albumList.value = FetchMusic.music.getArtistAlbumList(id)
        }
    }
}