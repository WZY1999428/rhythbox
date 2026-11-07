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
import wzy.hkta.rhythbox.screens.album.OnlineAlbumSongScreen
import wzy.hkta.rhythbox.components.LoadMore
import wzy.hkta.rhythbox.screens.search.netSearch.OnlineSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.AlbumItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchType

@Composable
fun AlbumList(vm: OnlineSearchViewModel, navigator: Navigator) {

    val state by vm.albumList.collectAsState()
    val keyword by vm.keyword.collectAsState()
    LaunchedEffect(keyword) {
        vm.addPageIndex(SearchType.ALBUM)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.list) {
            AlbumItem(
                title = it.title,
                artist = it.artists.joinToString("/") { ar -> ar.artistName },
                year = it.releaseDate,
                coverUrl = it.cover,
                onClick = {
                    navigator.push(OnlineAlbumSongScreen(it.id, it.title))
                }
            )
        }
        item {
            LoadMore(
                state.loading,
                onRetry = { vm.retry(SearchType.ALBUM) },
                onLoadMore = {
                    vm.addPageIndex(SearchType.ALBUM)
                })
        }
    }
}



