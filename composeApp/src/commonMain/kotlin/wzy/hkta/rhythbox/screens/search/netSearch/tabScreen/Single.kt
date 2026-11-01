package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import wzy.hkta.rhythbox.screens.search.netSearch.LoadMore
import wzy.hkta.rhythbox.screens.search.netSearch.NetSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.SearchSongItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchType

@Composable
fun SingleList(vm: NetSearchViewModel) {

    val state by vm.songList.collectAsState()
    val keyword by vm.keyword.collectAsState()
    LaunchedEffect(keyword) {
        vm.addPageIndex(SearchType.SONG)
    }


    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.list) {
            SearchSongItem(
                title = it.title,
                artist = it.artists.joinToString("/") {ar-> ar.artistName },
                coverUrl = it.cover,
                onClick = { /* 打开歌曲详情 */ }
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