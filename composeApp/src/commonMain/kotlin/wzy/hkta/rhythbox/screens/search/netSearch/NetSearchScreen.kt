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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.components.SongBottomSheet
import wzy.hkta.rhythbox.components.SongItem
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.screens.SongMetadataScreen
import wzy.hkta.rhythbox.screens.search.EnhancedSearchBar
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.AlbumList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.ComplexList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.PlaylistList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.SingerList
import wzy.hkta.rhythbox.screens.search.netSearch.tabScreen.SingleList
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongType

class NetSearchScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var keyword by remember { mutableStateOf("") }
        val songDao = Rhythbox.dataBase.getSongDao()
        var songList by remember { mutableStateOf<List<SongEntity>>(emptyList()) }
        var selectSong by remember { mutableStateOf<SongEntity?>(null) }
        var showSearchDiscovery by remember { mutableStateOf(false) }
        var showSongBottomSheet by remember { mutableStateOf(false) }
        val tabs = listOf("综合", "单曲", "歌单", "歌手", "专辑")
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        var selectedTab by remember { mutableIntStateOf(1) } // 比如默认选中“单曲”
        val scope = rememberCoroutineScope()

        LaunchedEffect(keyword) {
            if (keyword.isEmpty()) {
                songList = emptyList()
            } else {
                scope.launch(Dispatchers.IO) {
                    songList = songDao.search(keyword)
                }
            }
        }
        LaunchedEffect(pagerState.currentPage) {
            selectedTab = pagerState.currentPage
        }


        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().drawBehind {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.08f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }.windowInsetsPadding(WindowInsets.statusBars).height(45.dp).padding(end = 20.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = { navigator.pop() }) {
                    Icon(
                        painter = painterResource(Res.drawable.back),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                EnhancedSearchBar(
                    keyword = keyword, onKeywordChange = { keyword = it })
            }

            when (showSearchDiscovery) {
                true -> SearchDiscovery()
                false -> Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    SearchTabs(
                        tabs = tabs, selectedIndex = selectedTab, onTabSelected = { index ->
                            selectedTab = index
                        })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> ComplexList()
                            1 -> SingleList()
                            2 -> PlaylistList()
                            3 -> SingerList()
                            4 -> AlbumList()
                        }
                    }
                }
            }
        }

        if (selectSong != null) {
            SongBottomSheet(
                show = showSongBottomSheet,
                songName = selectSong!!.name,
                cover = selectSong!!.cover,
                songId = selectSong!!.id,
                artistName = selectSong!!.artist ?: "未知歌手",
                onDismissRequest = { showSongBottomSheet = false },
                onEdit = { navigator.push(SongMetadataScreen(selectSong!!.id)) },
                onDeleteClick = { showSongBottomSheet = false })
        }
    }
}

