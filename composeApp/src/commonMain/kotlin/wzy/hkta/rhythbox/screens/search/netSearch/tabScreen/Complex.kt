package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import wzy.hkta.rhythbox.screens.album.NetAlbumSongScreen
import wzy.hkta.rhythbox.screens.artist.NetArtistSongScreen
import wzy.hkta.rhythbox.screens.playlist.NetPlaylistSongScreen
import wzy.hkta.rhythbox.screens.search.netSearch.NetSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.SearchAlbumItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchArtistItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchPlaylistItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchSongItem

@Composable
fun ComplexList(vm: NetSearchViewModel, navigator: Navigator) {
    val complex by vm.complex.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(complex.artist) {
            SearchArtistItem(
                name = it.name,
                alias = it.realName,
                songCount = it.sort,
                albumCount = it.sort,
                avatarUrl = it.avatar,
                onClick = {
                    navigator.push(NetArtistSongScreen(it.id, it.name))
                }
            )
        }
        items(complex.playlist) {
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
        items(complex.album) {
            SearchAlbumItem(
                title = it.title,
                artist = it.artists.joinToString("/") { ar -> ar.artistName },
                coverUrl = it.cover,
                onClick = {
                    navigator.push(NetAlbumSongScreen(it.id, it.title))
                }
            )
        }
        items(complex.song) {
            SearchSongItem(
                title = it.title,
                coverUrl = it.cover,
                artist = it.artists.joinToString("/") { it.artistName },
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

