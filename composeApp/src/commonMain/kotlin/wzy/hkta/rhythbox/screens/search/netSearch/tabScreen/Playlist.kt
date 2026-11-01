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
import wzy.hkta.rhythbox.screens.playlist.NetPlaylistSongScreen
import wzy.hkta.rhythbox.screens.search.netSearch.LoadMore
import wzy.hkta.rhythbox.screens.search.netSearch.NetSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.SearchPlaylistItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchType

@Composable
fun PlaylistList(vm: NetSearchViewModel, navigator: Navigator) {
    val state by vm.playlist.collectAsState()
    val keyword by vm.keyword.collectAsState()
    LaunchedEffect(keyword) {
        vm.addPageIndex(SearchType.PLAYLIST)
    }


    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.list) {
            SearchPlaylistItem(
                title = it.title,
                creator = it.description,
                coverUrl = it.cover,
                songCount = it.favoriteTotal.toInt(),
                playCountText = it.playTotal.toString(),
                onClick = {
                    navigator.push(NetPlaylistSongScreen(it.title, it.id))
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