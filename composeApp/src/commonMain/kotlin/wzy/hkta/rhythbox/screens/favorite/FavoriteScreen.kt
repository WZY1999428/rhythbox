package wzy.hkta.rhythbox.screens.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.Tabs
import wzy.hkta.rhythbox.screens.favorite.components.AlbumList
import wzy.hkta.rhythbox.screens.favorite.components.ArtistList
import wzy.hkta.rhythbox.screens.favorite.components.LocalSong
import wzy.hkta.rhythbox.screens.favorite.components.OnlineSong
import wzy.hkta.rhythbox.screens.favorite.components.Playlist
import wzy.hkta.rhythbox.utils.Rhythbox

class FavoriteScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val player = Rhythbox.player
        val tabs = listOf("本地歌曲", "在线歌曲", "歌单", "歌手", "专辑")
        val vm = rememberScreenModel { FavoriteViewModel() }
        val playMediaId by player.playMediaId.collectAsState()
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val coroutineScope = rememberCoroutineScope() // 用于协程切换 Page
        Column(modifier = Modifier.fillMaxSize()) {
            NavBar("我的收藏")
            Tabs(
                tabs = tabs,
                selectedIndex = pagerState.currentPage, // 3. 直接读取 Pager 的当前页
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { page ->
                when (page) {
                    0 -> LocalSong(vm, navigator)
                    1 -> OnlineSong(vm, navigator)
                    2 -> Playlist(vm, navigator)
                    3 -> ArtistList(vm, navigator)
                    4 -> AlbumList(vm, navigator)
                }
            }

            if (playMediaId != null) {
                BottomControl()
            }
        }
    }
}
