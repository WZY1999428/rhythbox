package wzy.hkta.rhythbox.screens.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.scanning
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.RhythboxSongEmptyState
import wzy.hkta.rhythbox.screens.playlist.PlaylistScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistType
import wzy.hkta.rhythbox.screens.ScanMusicScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistSongScreen
import wzy.hkta.rhythbox.utils.Rhythbox

class NetAlbumScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel { NetAlbumViewModel() }
        val albumList = vm.albumList.collectAsState()
        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(title = "专辑") {
                IconButton(
                    onClick = { navigator.push(ScanMusicScreen()) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.scanning),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if(albumList.value.isEmpty()){
                RhythboxSongEmptyState(
                    describe = "陈列架上空空如也，还没有收藏到精美的黑胶唱片呢~",
                    buttonText = "去扫描",
                    onButtonClick = {
                        navigator.push(ScanMusicScreen())
                    }
                )
            }else{
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(90.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(albumList.value.size) {
                        val item = albumList.value[it]
                        Column(
                            modifier = Modifier.clip(RoundedCornerShape(5.dp)).clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                navigator.push(
                                    PlaylistSongScreen(
                                        item.name,
                                        id = item.id,
                                        type = PlaylistType.ALBUM
                                    )
                                )
                            }
                        ) {
                            Image(
                                painterResource(Res.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Column(modifier = Modifier.padding(horizontal = 3.dp)) {
                                Text(
                                    item.name,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    item.artist,
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


private class NetAlbumViewModel : ScreenModel {
    val albumDao = Rhythbox.dataBase.getAlbumDao()
    val albumList = albumDao.find().stateIn(
        started = SharingStarted.WhileSubscribed(5000),
        scope = screenModelScope,
        initialValue = emptyList()
    )


//    val albumList=
}