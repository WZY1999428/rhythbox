package wzy.hkta.rhythbox.screens.artist


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import rhythbox.composeapp.generated.resources.right
import rhythbox.composeapp.generated.resources.scanning
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.RhythboxSongEmptyState
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.screens.playlist.PlaylistType
import wzy.hkta.rhythbox.screens.ScanMusicScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistSongScreen
import wzy.hkta.rhythbox.utils.Rhythbox

class ArtistScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel { SingerViewModel() }
        val singerList = vm.singerList.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(title = "歌手") {
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
            if (singerList.value.isEmpty()) {
                RhythboxSongEmptyState(
                    describe = "群星闪烁的舞台正静悄悄，等待属于你的音乐人登场~",
                    buttonText = "去扫描",
                    onButtonClick = {
                        navigator.push(ScanMusicScreen())
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(singerList.value.size) {
                        val item = singerList.value[it]
                        Row(
                            modifier = Modifier.clip(
                                RoundedCornerShape(3.dp)
                            ).clickable {
                                navigator.push(
                                    PlaylistSongScreen(
                                        item.name,
                                        item.id,
                                        PlaylistType.ARTIST
                                    )
                                )
                            }.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painterResource(Res.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(50.dp))
                            )
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp).weight(1f)
                            ) {
                                Text(
                                    item.name,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${item.count}首音乐",
                                    fontSize = 13.sp,
                                    color = "#666666".cssToColor()
                                )
                            }
                            Icon(
                                painterResource(Res.drawable.right),
                                tint = Color.Unspecified,
                                contentDescription = null
                            )
                        }
                    }
                }
                BottomControl()
            }
        }
    }
}


private class SingerViewModel : ScreenModel {
    val singerDao = Rhythbox.dataBase.getSingerDao()

    val singerList = singerDao.find().stateIn(
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
        scope = screenModelScope
    )

}