package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import wzy.hkta.rhythbox.components.LoadMore
import wzy.hkta.rhythbox.screens.search.netSearch.OnlineSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.SearchSongItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchType
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongType

@Composable
fun SingleList(vm: OnlineSearchViewModel) {

    val state by vm.songList.collectAsState()
    val keyword by vm.keyword.collectAsState()
    LaunchedEffect(keyword) {
        vm.addPageIndex(SearchType.SONG)
    }


    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.list) { song ->
            SearchSongItem(
                title = song.title,
                artist = song.artists.joinToString("/") { ar -> ar.artistName },
                coverUrl = song.cover,
                onClick = {
                    Rhythbox.player.addSong(SongType.Network(song))
                }
            )
        }
        item {
            LoadMore(
                state.loading,
                onRetry = { vm.retry(SearchType.SONG) },
                onLoadMore = {
                    vm.addPageIndex(SearchType.SONG)
                })
        }
    }
}