package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import wzy.hkta.rhythbox.screens.album.OnlineAlbumSongScreen
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.screens.playlist.OnlinePlaylistSongScreen
import wzy.hkta.rhythbox.screens.search.netSearch.OnlineSearchViewModel
import wzy.hkta.rhythbox.screens.search.netSearch.AlbumItem
import wzy.hkta.rhythbox.screens.search.netSearch.ArtistItem
import wzy.hkta.rhythbox.screens.search.netSearch.PlaylistItem
import wzy.hkta.rhythbox.screens.search.netSearch.SearchSongItem

@Composable
fun ComplexList(vm: OnlineSearchViewModel, navigator: Navigator) {
    val complex by vm.complex.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(complex.artist) {
            ArtistItem(
                name = it.name,
                alias = it.realName,
                songCount = it.sort,
                albumCount = it.sort,
                avatarUrl = it.avatar,
                onClick = {
                    navigator.push(OnlineArtistSongScreen(it.id, it.name))
                }
            )
        }
        items(complex.playlist) {
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
        items(complex.album) {
            AlbumItem(
                title = it.title,
                artist = it.artists.joinToString("/") { ar -> ar.artistName },
                coverUrl = it.cover,
                onClick = {
                    navigator.push(OnlineAlbumSongScreen(it.id, it.title))
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

