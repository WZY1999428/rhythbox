package wzy.hkta.rhythbox.screens.homeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import wzy.hkta.rhythbox.screens.homeScreen.components.PlayCard
import wzy.hkta.rhythbox.screens.homeScreen.components.PlayListCards
import wzy.hkta.rhythbox.utils.Rhythbox

@Composable
fun  LocalScreen(
    vm: HomeViewModel,
    navigator: Navigator
){
    val player = Rhythbox.player
    val progress by player.progress.collectAsState()
    val duration by player.duration.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val currentTime by player.currentPosition.collectAsState()
    val lyricStatus by player.lyricStatus.collectAsState()

    Column {
        Spacer(modifier = Modifier.height(6.dp))
        PlayCard(
            progress = progress,
            duration = duration,
            currentTime = currentTime,
            currentSong = currentSong,
            lyricStatus = lyricStatus,
            seekTo = { player.seekTo(it) }
        )
        Spacer(modifier = Modifier.height(20.dp))
        CategoryRow(navigator)
        Spacer(modifier = Modifier.height(20.dp))
        PlayListRow(navigator)
        Spacer(modifier = Modifier.height(10.dp))
        PlayListCards(vm, navigator)
    }
}