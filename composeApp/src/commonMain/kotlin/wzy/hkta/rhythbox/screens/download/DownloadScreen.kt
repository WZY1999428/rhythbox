package wzy.hkta.rhythbox.screens.download
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.Tabs
import wzy.hkta.rhythbox.utils.Rhythbox


class DownloadScreen : Screen {

    @Composable
    override fun Content() {
        val key = "download-song"
        val player = Rhythbox.player
        val tabs = listOf("下载中", "下载完成")
        val vm = rememberScreenModel { DownloadViewModel(key) }
        val playMediaId by player.playMediaId.collectAsState()
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val coroutineScope = rememberCoroutineScope() // 用于协程切换 Page
        Column(modifier = Modifier.fillMaxSize()) {
            NavBar("下载管理")
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
                    0 -> DownloadContent(vm)
                    1 -> DownloadSong(vm)
                }
            }

            if (playMediaId != null) {
                BottomControl()
            }

        }
    }
}

