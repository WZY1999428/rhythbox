package wzy.hkta.rhythbox.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.pause2
import rhythbox.composeapp.generated.resources.play
import rhythbox.composeapp.generated.resources.playlist
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.screens.playerScreen.PlayerScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.marqueeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomControl(
) {

    var showMenu by rememberSaveable { mutableStateOf(false) }
    val navigator: Navigator = LocalNavigator.currentOrThrow;
    val player = Rhythbox.player;

    val playerLoading = player.playerLoading.collectAsState();
    val progress = player.progress.collectAsState();
    val duration = player.duration.collectAsState();
    val currentSong = player.currentSong.collectAsState()
    val isPlaying = player.isPlaying.collectAsState()
    var isSeeking by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }

    val cover = when (val cur = currentSong.value) {
        is SongType.Local -> cur.song.cover
        is SongType.Network -> cur.song.cover
        null -> null
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(55.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(53.dp).align(alignment = Alignment.TopStart)
                .background(MaterialTheme.colorScheme.primary.copy(0.8f))
                .clickable {
                    navigator.push(PlayerScreen())
                }
                .padding(start = 10.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(width = 1.dp, Color.White)
            ) {
                if (cover == null) {
                    Image(contentDescription = null, painter = painterResource(Res.drawable.logo))
                } else {
                    AsyncImage(model = cover, contentDescription = null)
                }
            }

            Column(
                modifier = Modifier.weight(1f)
                    .fillMaxHeight().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.Center
            ) {

                val (name, artist) = when (val cur = currentSong.value) {
                    is SongType.Local -> cur.song.name to cur.song.artist
                    is SongType.Network -> cur.song.title to cur.song.artists.joinToString("/") { it.artistName }
                    null -> null to null
                }

                Text(
                    name ?: "",
                    color = "#0C0807".cssToColor(),
                    maxLines = 1,
                    modifier = Modifier.marqueeText(),
                    lineHeight = 12.sp,
                    fontSize = 14.sp
                )
                Text(
                    artist ?: "",
                    color = "#C08C7B".cssToColor(),
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { player.togglePlay() }) {
                when (playerLoading.value) {
                    false -> Icon(
                        painterResource(if (isPlaying.value) Res.drawable.pause2 else Res.drawable.play),
                        modifier = Modifier.size(20.dp),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )

                    true -> CircularBarSpinner()
                }
            }

            IconButton(onClick = { showMenu = true }) {
                Icon(
                    painter = painterResource(Res.drawable.playlist),
                    tint = Color.Unspecified,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        val currentValue = if (isSeeking) sliderValue else (progress.value)
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides Dp.Unspecified
        ) {
            Slider(
                modifier = Modifier.fillMaxWidth().height(2.dp)
                    .align(alignment = Alignment.TopStart),
                value = currentValue,
                valueRange = 0f..1f,
                onValueChange = {
                    isSeeking = true
                    sliderValue = it;
                },
                onValueChangeFinished = {
                    isSeeking = false
                    val total = duration.value
                    val position = (total * currentValue).toLong()
                    player.seekTo(position)
                },
                thumb = {},
                track = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // 背景轨道
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )
                        // 已播放进度
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(currentValue)
                                .fillMaxHeight()
                                .background(
                                    "#3498DB".cssToColor(), RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 0.dp,
                                        topEnd = 10.dp,
                                        bottomEnd = 10.dp
                                    )
                                )
                        )
                    }
                }
            )
        }
    }

    PlayerMenuBottomSheet(
        show = showMenu,
        onDismissRequest = { showMenu = false }
    )
}
