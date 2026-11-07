package wzy.hkta.rhythbox.screens.favorite.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.nologin
import wzy.hkta.rhythbox.components.RhythboxLoadingState
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.screens.favorite.FavoriteType
import wzy.hkta.rhythbox.screens.favorite.FavoriteViewModel
import wzy.hkta.rhythbox.screens.favorite.LoadState
import wzy.hkta.rhythbox.screens.playlist.OnlinePlaylistSongScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistSquareScreen
import wzy.hkta.rhythbox.screens.search.netSearch.PlaylistItem
import wzy.hkta.rhythbox.screens.user.LoginScreen
import wzy.hkta.rhythbox.utils.UserManager

@Composable
fun Playlist(
    vm: FavoriteViewModel,
    navigator: Navigator
) {
    val playlist = vm.playlist.collectAsState()

    LaunchedEffect(Unit) {
        if (UserManager.getToken() != null) {
            vm.getList(FavoriteType.PLAYLIST)
        }
    }

    if (UserManager.getToken() == null) {
        RhythboxSongEmptyState(
            describe = "还没有登录，无法查看收藏的歌单",
            image = Res.drawable.nologin,
            buttonText = "去登录",
            onButtonClick = {
                navigator.push(LoginScreen())
            })
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                when (playlist.value.loading) {
                    LoadState.NotFound -> RhythboxSongEmptyState(
                        describe = "还没有收藏歌单哪",
                        // 🌟 顺便把按钮文字也根据页面动态调校一下，体验会更细腻！
                        buttonText = "去广场看看",
                        onButtonClick = {
                            navigator.push(PlaylistSquareScreen())
                        }
                    )

                    LoadState.Finish -> LazyColumn(modifier = Modifier.weight(1f)) {
                        items(playlist.value.list.size) {
                            val item = playlist.value.list[it]
                            PlaylistItem(
                                title = item.title,
                                creator = item.description,
                                coverUrl = item.cover,
                                songCount = item.songCount,
                                playCountText = item.playTotal.toString(),
                                onClick = {
                                    navigator.push(OnlinePlaylistSongScreen(item.title, item.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    LoadState.Loading -> RhythboxLoadingState()

                    is LoadState.LoadError -> RhythboxSongEmptyState(
                        describe = "加载失败：${(playlist.value.loading as LoadState.LoadError).msg}",
                        buttonText = "重新加载",
                        onButtonClick = {
                            vm.getList(FavoriteType.PLAYLIST)
                        }
                    )
                }
            }
        }
    }
}