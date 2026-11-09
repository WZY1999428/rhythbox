package wzy.hkta.rhythbox.screens.search.netSearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.back
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.google.protobuf.LazyStringArrayList.emptyList
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.Tabs
import wzy.hkta.rhythbox.screens.search.EnhancedSearchBar
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.AlbumList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.ComplexList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.PlaylistList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.ArtistList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.SingleList
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.StorageManager

class OnlineSearchScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var keyword by remember { mutableStateOf("") }

        val vm = rememberScreenModel { OnlineSearchViewModel() }
        val showSearchDiscovery by vm.showSearchDiscovery.collectAsState()
        val vmKeyword by vm.keyword.collectAsState()
        val tabs = listOf("综合", "单曲", "歌单", "歌手", "专辑")
        // 1. 只使用 pagerState 作为唯一的数据源
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val playKey by Rhythbox.player.playKey.collectAsState()
        val coroutineScope = rememberCoroutineScope() // 用于协程切换 Page

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.08f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(50.dp)
                    .padding(end = 20.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        painter = painterResource(Res.drawable.back),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 2. 必须加上 weight(1f)，否则输入框宽度计算会出错导致布局异常
                Box(modifier = Modifier.weight(1f)) {
                    EnhancedSearchBar(
                        keyword = keyword,
                        onKeywordChange = {
                            keyword = it
                            if (it.isEmpty()) {
                                vm.clearKeyword()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                                vm.toggleSearchDiscovery(true)
                            }
                        },
                        onSearch = {
                            saveHistory(vm, keyword, vmKeyword)
                        }
                    )
                }
            }

            // 调试文本，确认状态无误后可注释掉
            // Text("$keyword $vmKeyword $showSearchDiscovery")

            when (showSearchDiscovery) {
                true -> SearchDiscovery(onSearch = {
                    keyword = it
                    saveHistory(vm, keyword, vmKeyword)
                })

                false -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // 确保这里撑满剩余高度
                        .padding(vertical = 10.dp)
                ) {
                    Tabs(
                        tabs = tabs,
                        selectedIndex = pagerState.currentPage, // 3. 直接读取 Pager 的当前页
                        onTabSelected = { index ->
                            // 4. 点击 Tab 时平滑滚动过去，抛弃繁琐的 LaunchedEffect 联动
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // 必须加 weight(1f)，否则 Pager 高度为 0 导致空白！
                    ) { page ->
                        when (page) {
                            0 -> ComplexList(vm, navigator)
                            1 -> SingleList(vm)
                            2 -> PlaylistList(vm, navigator)
                            3 -> ArtistList(vm, navigator)
                            4 -> AlbumList(vm, navigator)
                        }
                    }
                    playKey?.let {
                        BottomControl()
                    }
                }
            }
        }
    }
}

fun saveHistory(vm: OnlineSearchViewModel, keyword: String, vmKeyword: String) {
    if (keyword.isNotBlank() && vmKeyword != keyword) {
        val history = (StorageManager.getItem<List<String>>("search-history")
            ?: emptyList())
            .toMutableList()
            .apply {
                remove(keyword)
                add(0, keyword)
            }
        StorageManager.setItem("search-history", history)
        vm.toggleSearchDiscovery(false)
        vm.startSearch(keyword)
    }

}