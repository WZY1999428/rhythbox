package wzy.hkta.rhythbox.screens.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.logo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.DrawableResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.music
import rhythbox.composeapp.generated.resources.leaderboard
import rhythbox.composeapp.generated.resources.classify
import rhythbox.composeapp.generated.resources.recently
import wzy.hkta.rhythbox.screens.playlist.PlaylistScreen
import wzy.hkta.rhythbox.screens.playlist.PlaylistSquareScreen
import wzy.hkta.rhythbox.screens.song.NetSongScreen
import wzy.hkta.rhythbox.screens.search.netSearch.NetSearchScreen

private data class Playlist(
    val title: String,
    val desc: String,
    val key: String
)

@Composable
fun OnlineScreen(viewModel: HomeViewModel, navigator: Navigator) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val textSecondary = Color(0xFF92939F)
    val banners = viewModel.banner.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val songList = viewModel.songList.collectAsState()
    val playlists = listOf(
        Playlist("每日推荐", "每日推荐歌曲", "dailyRecommend"),
        Playlist("华语精选", "精选华语热门歌曲", "chinese"),
        Playlist("独立音乐", "发现小众好声音", "indie"),
        Playlist("轻音乐", "放松心情", "lightMusic")
    )

    val pageState = rememberPagerState(
        pageCount = { banners.value.size }
    )
    PullToRefreshBox(
        isRefreshing = isRefreshing.value,
        onRefresh = {
            viewModel.fetchHomeData()
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            // 顶部标题
            item {
                Column(
                    modifier = Modifier.padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "发现好音乐",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "让旋律陪伴你的每一天",
                                color = textSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "♫",
                            color = accent,
                            fontSize = 34.sp
                        )
                    }

                    // 搜索框
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardColor)
                            .clickable {
                                // TODO 打开在线搜索页
                                navigator.push(NetSearchScreen())
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⌕", color = textSecondary, fontSize = 25.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "搜索歌曲、歌手、专辑",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 轮播 Banner
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalPager(
                        state = pageState,
                        pageSpacing = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    // TODO 打开 Banner 对应详情
                                }
                        ) {

                            val banner = banners.value[page]
                            AsyncImage(
                                model = banner.image,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // 深色渐变，让标题更容易阅读
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xDD171522),
                                                Color(0x66302A48),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(22.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(9.dp)
                            ) {
                                Text(
                                    text = banner.title,
                                    color = accent,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = banner.desc,
                                    color = Color.White,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "EXPLORE MUSIC  →",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // 分页指示器
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(banners.value.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .width(
                                        if (pageState.currentPage == index)
                                            20.dp else 6.dp
                                    )
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pageState.currentPage == index)
                                            accent
                                        else
                                            Color(0xFF41414C)
                                    )
                            )
                        }
                    }
                }
            }

            // 快捷入口
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickEntry(Res.drawable.music, "每日推荐", accent) {
                        navigator.push(NetSongScreen("dailyRecommend", "每日推荐"))
                    }
                    QuickEntry(Res.drawable.leaderboard, "排行榜", accent) {
                        navigator.push(NetSongScreen("leaderboard", "排行榜"))
                    }
                    QuickEntry(Res.drawable.classify, "歌单广场", accent) {
                        navigator.push(PlaylistSquareScreen())
                    }
                    QuickEntry(Res.drawable.recently, "最近播放", accent) {
                        navigator.push(NetSongScreen("recently", "最近播放"))
                    }
                }
            }

            // 推荐歌单
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader(
                        title = "为你推荐",
                        subtitle = "精选歌单 · 总有一首适合你",
                        accent = accent,
                        onMore = {
                            navigator.push(PlaylistScreen("推荐歌单", "recommend"))
                        }
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        items(playlists.size) { index ->
                            val item = playlists[index]
                            Column(
                                modifier = Modifier
                                    .width(145.dp)
                                    .clickable {
                                        navigator.push(NetSongScreen(item.key, item.title))
                                    },
                                verticalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    Image(
                                        painter = painterResource(
                                            Res.drawable.logo
                                        ),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Text(
                                        text = "♫",
                                        color = Color.Black,
                                        fontSize = 27.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                    )
                                }

                                Text(
                                    text = item.title,
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = item.desc,
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }


                    }
                }
            }

            // 新歌推荐
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader(
                        title = "热门新歌",
                        subtitle = "最近大家都在听",
                        accent = accent,
                        onMore = {
                            // TODO 查看更多新歌
                            navigator.push(NetSongScreen("hot", "热门新歌"))
                        }
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        songList.value.forEach { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        // TODO 播放歌曲
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = song.cover,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement =
                                        Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = song.title,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = song.artists.joinToString(",") { it.artistName },
                                        color = textSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "⋯",
                                    color = textSecondary,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}


@Composable
private fun QuickEntry(
    icon: DrawableResource,
    title: String,
    accent: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(accent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = title,
            color = Color.Black.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    accent: Color,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = Color(0xFF92939F),
                fontSize = 11.sp
            )
        }

        Text(
            text = "更多  ›",
            color = accent,
            fontSize = 12.sp,
            modifier = Modifier.clickable(onClick = onMore)
        )
    }
}