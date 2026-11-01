package wzy.hkta.rhythbox.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.music
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.screens.search.netSearch.NetSearchScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.fatchMusic.Playlist
import wzy.hkta.rhythbox.utils.fatchMusic.PlaylistBanner
import wzy.hkta.rhythbox.utils.formatPlayTotal

/**
 * 网络歌单广场
 *
 * 独立于本地歌单详情页。
 * 目前使用模拟数据，接入接口后替换数据源即可。
 */
class PlaylistSquareScreen : Screen {

    @Composable
    override fun Content() {
        val vm = rememberScreenModel { PlaylistSquareViewModel() }
        val navigator = LocalNavigator.currentOrThrow
        PlaylistSquareContent(
            vm,
            onSearchClick = {
                navigator.push(NetSearchScreen())
            },
            onPlayPlaylist = { title, id ->
                navigator.push(NetPlaylistSongScreen(title, id))
            },
            onPlaylistClick = { title, id ->
                navigator.push(NetPlaylistSongScreen(title, id))
            }
        )
    }
}

@Composable
fun PlaylistSquareContent(
    vm: PlaylistSquareViewModel,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onPlaylistClick: ((title: String, id: Long) -> Unit)? = null,
    onPlayPlaylist: ((title: String, id: Long) -> Unit)? = null
) {
    val playUuid by Rhythbox.player.playUuid.collectAsState()
    val playlist by vm.recommendPlaylist.collectAsState()
    val playlistTabs by vm.playlistTabs.collectAsState()
    val playlistType by vm.playlistType.collectAsState()
    val hotPlaylist by vm.hotPlaylist.collectAsState()
    val banner by vm.banner.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F9))
    ) {

        NavBar(
            title = "歌单广场",
            iconColor = Color.Black,
            titleColor = Color.Black,
            background = Color.Transparent,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 28.dp, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            item {
                // 搜索框
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            onSearchClick()
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⌕", color = Color(0xFF92939F), fontSize = 25.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "搜索歌曲、歌手、专辑",
                        color = Color(0xFF92939F),
                        fontSize = 14.sp
                    )
                }
            }
            // 精选推荐 Banner
            item {
                FeaturedPlaylist(
                    onClick = { },
                    onPlayClick = { title, id -> onPlayPlaylist?.invoke(title, id) },
                    banner = banner
                )
            }
            // 分类栏
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    playlistTabs.forEach { category ->
                        val selected = playlistType == category.key

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (selected) Color(0xFFD889A3)
                                    else Color.White
                                )
                                .clickable { vm.setPlaylistType(category.key) }
                                .padding(horizontal = 17.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                                color = if (selected) {
                                    Color.White
                                } else {
                                    Color(0xFF66616B)
                                }
                            )
                        }
                    }
                }
            }
            // 精选歌单双列
            item {
                Column {
                    SectionHeader(
                        title = "为你精选",
                        subtitle = "总有一份旋律适合现在的你"
                    )

                    if (playlist.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "这个分类还没有歌单，换个分类看看吧～",
                                fontSize = 13.sp,
                                color = Color(0xFF99939D)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(13.dp)
                        ) {
                            playlist.forEach { playlist ->
                                PlaylistCard(
                                    playlist = playlist,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onPlaylistClick?.invoke(playlist.title, playlist.id)
                                    }
                                )
                            }
                            // 单数张歌单时保留右侧空位
                            if (playlist.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(15.dp))
                    }
                }
            }

            // 热门歌单
            item {
                Column {
                    SectionHeader(
                        title = "热门歌单",
                        subtitle = "大家最近都在听",
                        action = "更多"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        hotPlaylist.forEach { playlist ->
                            Column(
                                modifier = Modifier
                                    .width(138.dp)
                                    .clickable {
                                        onPlaylistClick?.invoke(playlist.title, playlist.id)
                                    }
                            ) {
                                PlaylistCover(
                                    playlist = playlist,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = playlist.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333039),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${formatPlayTotal(playlist.playTotal)} 次播放",
                                    fontSize = 11.sp,
                                    color = Color(0xFF99939D)
                                )
                            }
                        }

                    }
                }
            }
        }

        if (playUuid != null) {
            BottomControl()
        }
    }
}


@Composable
private fun FeaturedPlaylist(
    banner: List<PlaylistBanner>,
    onClick: () -> Unit,
    onPlayClick: ((title: String, id: Long) -> Unit)? = null
) {
    if (banner.isEmpty()) return

    val state = rememberPagerState(
        pageCount = { banner.size }
    )

    HorizontalPager(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 12.dp
    ) { page ->
        val playlist = banner[page]

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(186.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onClick)
        ) {
            // 封面
            AsyncImage(
                model = playlist.cover,
                contentDescription = playlist.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 底部渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            // 歌单标题和描述
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 48.dp
                    )
            ) {
                Text(
                    text = playlist.title,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = playlist.desc.orEmpty(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 播放按钮
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 12.dp),
                onClick = { onPlayClick?.invoke(playlist.title, playlist.value.toLong()) },
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 7.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "▶",
                        fontSize = 11.sp,
                        color = Color(0xFFB57592)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "立即聆听",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF76556B)
                    )
                }
            }

            // 页码指示器
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banner.size) { index ->
                    val selected = state.currentPage == index

                    Box(
                        modifier = Modifier
                            .width(if (selected) 16.dp else 5.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    action: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF29262E)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF99939D)
            )
        }

        if (action != null) {
            Text(
                text = "$action  ›",
                fontSize = 12.sp,
                color = Color(0xFF8F8994)
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        PlaylistCover(
            playlist = playlist,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(9.dp))

        Text(
            text = playlist.title,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF302D35),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = playlist.description,
            fontSize = 11.sp,
            color = Color(0xFF99939D),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "▶",
                fontSize = 9.sp,
                color = Color(0xFFD18AA3)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "${formatPlayTotal(playlist.playTotal)} 播放",
                fontSize = 10.sp,
                color = Color(0xFF8F8994)
            )
        }
    }
}

@Composable
private fun PlaylistCover(
    playlist: Playlist,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF665B95), Color(0xFFB9A7D8)))
            )
    ) {
        // 装饰圆环/黑胶唱片效果
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White) // 圆盘底色
                .border(2.dp, Color.White.copy(0.8f), CircleShape) // 可选：加个边缘
        ) {
            AsyncImage(
                // 注意：如果你的 playlist.cover 是对象，这里需要取其 url，如 playlist.cover?.url
                model = playlist.cover,
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 安全渲染标签：只有当 tags 不为空时才展示
        val firstTag = playlist.tags.firstOrNull()
        if (firstTag != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(9.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.35f) // 稍微加深一点透明度，让字更清晰
            ) {
                Text(
                    text = firstTag.title,
                    modifier = Modifier.padding(
                        horizontal = 7.dp,
                        vertical = 4.dp
                    ),
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }

        // 右下角小图标
        Icon(
            painter = painterResource(Res.drawable.music),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
        )
    }
}