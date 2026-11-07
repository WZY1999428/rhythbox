package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import wzy.hkta.rhythbox.screens.playlist.OnlinePlaylistSongScreen
import wzy.hkta.rhythbox.components.LoadMore
import wzy.hkta.rhythbox.screens.search.netSearch.OnlineSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.PlaylistItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchType

@Composable
fun PlaylistList(vm: OnlineSearchViewModel, navigator: Navigator) {
    val state by vm.playlist.collectAsState()
    val keyword by vm.keyword.collectAsState()
    LaunchedEffect(keyword) {
        vm.addPageIndex(SearchType.PLAYLIST)
    }


    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.list) {
            PlaylistItem(
                title = it.title,
                creator = it.description,
                coverUrl = it.cover,
                songCount = it.favoriteTotal.toInt(),
                playCountText = it.playTotal.toString(),
                onClick = {
                    navigator.push(OnlinePlaylistSongScreen(it.title, it.id))
                }
            )
        }
        item {
            LoadMore(
                state.loading,
                onRetry = { vm.retry(SearchType.PLAYLIST) },
                onLoadMore = {
                    vm.addPageIndex(SearchType.PLAYLIST)
                })
        }
    }

}