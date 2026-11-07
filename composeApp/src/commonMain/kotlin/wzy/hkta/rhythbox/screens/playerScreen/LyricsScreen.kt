package wzy.hkta.rhythbox.screens.playerScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.expect.LyricStatus.Finished
import wzy.hkta.rhythbox.expect.LyricStatus.Init
import wzy.hkta.rhythbox.expect.LyricStatus.LoadErr
import wzy.hkta.rhythbox.expect.LyricStatus.Loading
import wzy.hkta.rhythbox.expect.LyricStatus.NotFound
import wzy.hkta.rhythbox.theme.PeachGradientStops
import wzy.hkta.rhythbox.theme.SavannaGradientStops
import wzy.hkta.rhythbox.utils.LyricInfo
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SettingManager
import wzy.hkta.rhythbox.utils.getSongInfo
import wzy.hkta.rhythbox.utils.marqueeText
import wzy.hkta.rhythbox.utils.parseLyric


class LyricsScreen : Screen {

    @Composable
    override fun Content() {
        val player = Rhythbox.player
        val currentTime by player.currentPosition.collectAsState()
        val lyricList by player.lyricList.collectAsState()
        val lyricStatus by player.lyricStatus.collectAsState()
        val playMediaId by player.playMediaId.collectAsState()

        val lyricInfoList = remember { mutableStateListOf<LyricInfo>() }
        val currentSong by player.currentSong.collectAsState()
        val lyricOffsets = remember(lyricInfoList.size) {
            mutableStateListOf<Int>().apply {
                repeat(lyricInfoList.size) { add(0) }
            }
        }
        val lazyListStates = remember(lyricInfoList.size) {
            List(lyricInfoList.size) {
                LazyListState()
            }
        }
        val isSystemInDarkTheme by SettingManager.isSystemInDarkTheme.collectAsState()
        var showLyricsOffsetController by remember { mutableStateOf(false) }
        val pagerState = rememberPagerState(pageCount = { lyricInfoList.size })

        LaunchedEffect(lyricList, playMediaId) {
            showLyricsOffsetController = false
            lyricInfoList.clear()
            val list = lyricList.mapNotNull {
                parseLyric(it.content)?.let { lyric ->
                    LyricInfo(lyric, null)
                }
            }
            showLyricsOffsetController = true
            lyricInfoList.addAll(list)
        }


        val songInfo = getSongInfo(currentSong)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            Column(
                modifier = Modifier.fillMaxSize()
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colorStops = when (isSystemInDarkTheme) {
                                true -> PeachGradientStops
                                false -> SavannaGradientStops
                            },
                            start = Offset.Zero, // 左上角
                            end = Offset(x = widthPx, y = heightPx) // 完美的动态右下角
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NavBar(
                    title = songInfo?.name ?: "",
                    titleColor = Color.White,
                    background = Color.Transparent,
                    iconColor = Color.White,
                )
                when (lyricStatus) {
                    is Finished -> {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { page ->
                            // 【核心修复】：必须先判断 page 是否越界！
                            if (page !in lyricInfoList.indices || page !in lazyListStates.indices) {
                                return@HorizontalPager
                            }

                            val lyric = lyricInfoList[page]
                            val lazyListState = lazyListStates[page]

                            val currentIndex = lyric.lyric.indexOfLast {
                                it.timeMillis <= (currentTime + lyricOffsets.getOrElse(page) { 0 })
                            }
                            LaunchedEffect(currentIndex, pagerState.currentPage) {
                                if (pagerState.currentPage == page && currentIndex >= 0) {
                                    lazyListState.scrollToItem(currentIndex)
                                    val targetItem =
                                        lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex }
                                    if (targetItem != null) {
                                        lazyListState.scrollBy(-(targetItem.size / 2).toFloat())
                                    }
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    // 歌手信息
                                    Text(
                                        text = lyricList[page].title,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .marqueeText()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 歌词列表
                                    LazyColumn(
                                        state = lazyListState,

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        contentPadding = PaddingValues(vertical = 100.dp)
                                    ) {
                                        items(3) {
                                            Spacer(modifier = Modifier.height(30.dp))
                                        }
                                        itemsIndexed(
                                            lyric.lyric,
                                            key = { _, line -> line.timeMillis }) { index, line ->
                                            val isCurrent = currentIndex == index
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .animateItem(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCurrent) {
                                                    CurrentLyric(line.text)
                                                } else {
                                                    Text(
                                                        text = line.text,
                                                        fontSize = 17.sp,
                                                        lineHeight = 28.sp,
                                                        color = Color.White.copy(alpha = 0.55f),
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 5.dp)
                                                    )
                                                }
                                            }
                                        }
                                        items(3) {
                                            Spacer(modifier = Modifier.height(30.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // 歌词偏移控制
                                if (showLyricsOffsetController) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 4.dp)
                                    ) {
                                        LyricsOffsetController(
                                            onDelayClick = {
                                                if (page in lyricOffsets.indices) {
                                                    lyricOffsets[page] -= 500
                                                }
                                                val offset = lyricOffsets.getOrElse(page) { 0 }
                                                updateLyricsOffset(currentSong!!, offset)
                                            },
                                            onAdvanceClick = {
                                                if (page in lyricOffsets.indices) {
                                                    lyricOffsets[page] += 500
                                                }
                                                val offset = lyricOffsets.getOrElse(page) { 0 }
                                                updateLyricsOffset(currentSong!!, offset)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Init -> {
                        Text(
                            "欢迎使用乐动盒",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.7f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Loading -> {
                        Text(
                            text = "歌词加载中...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.7f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    is LoadErr -> {
                        Text(
                            text = (lyricStatus as LoadErr).msg,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFEF5350),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp).clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { player.getSongLyric() },
                            textAlign = TextAlign.Center
                        )
                    }

                    NotFound -> {
                        Text(
                            text = "暂无歌词",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Light, // 选用轻盈的字体粗细
                            color = Color.Gray.copy(alpha = 0.5f),
                            letterSpacing = 2.sp, // 拉开字间距，营造空灵、安静的氛围
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }


            }
        }
    }
}
