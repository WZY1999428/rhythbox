package wzy.hkta.rhythbox.screens.homeScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.plus_2
import wzy.hkta.rhythbox.components.AddPlaylist
import wzy.hkta.rhythbox.components.RhythboxPlaylistEmptyState
import wzy.hkta.rhythbox.screens.playlist.PlaylistScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistType
import wzy.hkta.rhythbox.screens.homeScreen.HomeViewModel
import wzy.hkta.rhythbox.screens.playlist.PlaylistSongScreen


@Composable
 fun PlayListCards(
    vm: HomeViewModel,
    navigator: Navigator
) {
    var showAddPlaylist by remember { mutableStateOf(false) }
    val playList by vm.playlist.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("我的歌单")
            IconButton(
                onClick = { showAddPlaylist = true },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    painterResource(Res.drawable.plus_2),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (playList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                RhythboxPlaylistEmptyState(
                    describe = "还没有歌单哦，新建一个吧",
                    buttonText = "新建歌单",
                    onButtonClick = { showAddPlaylist = true }
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 5.dp)
            ) {
                items(playList.size) {
                    val item = playList[it]
                    Column(
                        modifier = Modifier.width(140.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(95.dp),
                            shape = RoundedCornerShape(5.dp),
                            shadowElevation = 3.dp
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxSize().padding(6.dp)
                                    .clip(RoundedCornerShape(5.dp)).clickable {
                                        navigator.push(
                                            PlaylistSongScreen(
                                                item.playlist.name,
                                                item.playlist.id,
                                                PlaylistType.PLAYLIST
                                            )
                                        )
                                    },
                                maxItemsInEachRow = 3,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (song in item.songs) {
                                    if (song.congCover != null) {
                                        AsyncImage(
                                            model = song.congCover,
                                            contentDescription = null,
                                            modifier = Modifier.size(38.dp)
                                                .clip(shape = RoundedCornerShape(5.dp))
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(Res.drawable.logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(38.dp)
                                                .clip(shape = RoundedCornerShape(5.dp))
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            item.playlist.name,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
    }

    AddPlaylist(
        show = showAddPlaylist,
        onCancel = { showAddPlaylist = false },
        onConfirm = { name, describe ->
            vm.addPlayList(name, describe)
            showAddPlaylist = false
        }
    )
}

