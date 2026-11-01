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
import wzy.hkta.rhythbox.screens.artist.NetArtistSongScreen
import wzy.hkta.rhythbox.screens.search.netSearch.LoadMore
import wzy.hkta.rhythbox.screens.search.netSearch.NetSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.SearchArtistItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchType

@Composable
fun ArtistList(vm: NetSearchViewModel, navigator: Navigator) {
    val state by vm.artistList.collectAsState()
    val keyword by vm.keyword.collectAsState()
    LaunchedEffect(keyword) {
        vm.addPageIndex(SearchType.ARTIST)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.list) {
            SearchArtistItem(
                name = it.name,
                avatarUrl = it.avatar,
                onClick = {
                    navigator.push(NetArtistSongScreen(it.id, it.name))
                }
            )
        }
        item {
            LoadMore(
                state.loading,
                onRetry = { vm.retry(SearchType.ARTIST) },
                onLoadMore = {
                    vm.addPageIndex(SearchType.ARTIST)
                })
        }
    }
}