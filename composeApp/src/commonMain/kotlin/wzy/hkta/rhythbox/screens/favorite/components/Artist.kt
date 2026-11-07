package wzy.hkta.rhythbox.screens.favorite.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.nologin
import wzy.hkta.rhythbox.components.RhythboxLoadingState
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.screens.artist.OnlineArtistSongScreen
import wzy.hkta.rhythbox.screens.favorite.FavoriteType
import wzy.hkta.rhythbox.screens.favorite.FavoriteViewModel
import wzy.hkta.rhythbox.screens.favorite.LoadState
import wzy.hkta.rhythbox.screens.search.netSearch.ArtistItem
import wzy.hkta.rhythbox.screens.search.netSearch.OnlineSearchScreen
import wzy.hkta.rhythbox.screens.user.LoginScreen
import wzy.hkta.rhythbox.utils.UserManager

@Composable
fun ArtistList(vm: FavoriteViewModel, navigator: Navigator) {
    val artistList by vm.artistList.collectAsState()

    LaunchedEffect(Unit) {
        if (UserManager.getToken() != null) {
            vm.getList(FavoriteType.ARTIST)
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
                when (artistList.loading) {
                    LoadState.NotFound -> RhythboxSongEmptyState(
                        describe = "还没有收藏歌手哪",
                        buttonText = "去搜索看看",
                        onButtonClick = {
                            navigator.push(OnlineSearchScreen())
                        }
                    )

                    LoadState.Finish -> LazyColumn(modifier = Modifier.weight(1f)) {
                        items(artistList.list.size) {
                            val item = artistList.list[it]
                            ArtistItem(
                                name = item.name,
                                avatarUrl = item.avatar,
                                songCount = item.songCount,
                                onClick = {
                                    navigator.push(OnlineArtistSongScreen(item.id, item.name))
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    LoadState.Loading -> RhythboxLoadingState()

                    is LoadState.LoadError -> RhythboxSongEmptyState(
                        describe = "加载失败：${(artistList.loading as LoadState.LoadError).msg}",
                        buttonText = "重新加载",
                        onButtonClick = {
                            vm.getList(FavoriteType.ARTIST)
                        }
                    )
                }
            }
        }
    }


}