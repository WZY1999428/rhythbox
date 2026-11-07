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
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.components.LoadMore
import wzy.hkta.rhythbox.screens.search.netSearch.OnlineSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.ArtistItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchType

@Composable
fun ArtistList(vm: OnlineSearchViewModel, navigator: Navigator) {
    val state by vm.artistList.collectAsState()
    val keyword by vm.keyword.collectAsState()
    LaunchedEffect(keyword) {
        vm.addPageIndex(SearchType.ARTIST)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.list) {
            ArtistItem(
                name = it.name,
                avatarUrl = it.avatar,
                onClick = {
                    navigator.push(OnlineArtistSongScreen(it.id, it.name))
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