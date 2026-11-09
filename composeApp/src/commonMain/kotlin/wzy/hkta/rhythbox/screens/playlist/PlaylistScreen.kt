package wzy.hkta.rhythbox.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.PlaylistCard
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.PlaylistInfo


class PlaylistScreen(
    val title: String,
    val id: String,
) : Screen {
    @Composable
    override fun Content(){
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel(tag = "local-playlist-${title}_${id}") { PlaylistViewModel() }
         val playlist = vm.playlist.collectAsState()
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NavBar(title = title)
            LazyVerticalGrid(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 15.dp),
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(playlist.value.size) {
                    val item= playlist.value[it]
                    PlaylistCard(
                        cover =item.cover,
                        title = item.title,
                        subtitle = item.description,
                        playCount = item.playTotal,
                        onClick = {
                            navigator.push(OnlinePlaylistSongScreen(title = item.title, id = item.id))
                        }
                    )
                }
            }
        }
    }
}


class PlaylistViewModel : ScreenModel {
  private val _playlist = MutableStateFlow<List<PlaylistInfo>>(emptyList())
  val playlist = _playlist.asStateFlow()
  var total: Long = 0

   init {
       getList()
   }

    fun getList(){
        screenModelScope.launch {
            try {
             val data = FetchMusic.music.getPlayList()
             total = data.total;
             _playlist.value= data.list
            }catch (err: Exception){
                println("errerrerrerr$err")
            }
        }
    }
}