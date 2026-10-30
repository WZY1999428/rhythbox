package wzy.hkta.rhythbox.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.more
import rhythbox.composeapp.generated.resources.music
import wzy.hkta.rhythbox.components.NavBar

/**
 * 网络歌单广场
 *
 * 独立于本地歌单详情页。
 * 目前使用模拟数据，接入接口后替换数据源即可。
 */
class PlaylistSquareScreen : Screen {

    @Composable
    override fun Content() {
        PlaylistSquareContent()
    }
}

private data class PlaylistSquareItem(
    val id: Long,
    val name: String,
    val description: String,
    val playCount: String,
    val category: String,
    val coverColors: List<Color>
)

private val playlistCategories = listOf(
    "推荐", "流行", "轻音乐", "华语", "古风", "摇滚", "民谣", "电子"
)

private val demoPlaylists = listOf(
    PlaylistSquareItem(
        1, "深夜独处的 BGM", "一个人的夜晚，也要好好听歌",
        "12.8万", "轻音乐",
        listOf(Color(0xFF665B95), Color(0xFFB9A7D8))
    ),
    PlaylistSquareItem(
        2, "开车就听这些歌", "让每一段路都有自己的节奏",
        "8.6万", "流行",
        listOf(Color(0xFFD58D72), Color(0xFFF2C5A0))
    ),
    PlaylistSquareItem(
        3, "放空时刻 · 轻音乐", "适合阅读、工作与发呆",
        "5.2万", "轻音乐",
        listOf(Color(0xFF6F9E9A), Color(0xFFBEDBC8))
    ),
    PlaylistSquareItem(
        4, "华语宝藏旋律", "那些值得单曲循环的好歌",
        "3.9万", "华语",
        listOf(Color(0xFFB66F8D), Color(0xFFE9B6C7))
    ),
    PlaylistSquareItem(
        5, "古风里的江湖梦", "听风、听雨，也听故事",
        "3.1万", "古风",
        listOf(Color(0xFF567C77), Color(0xFFA8C5AB))
    ),
    PlaylistSquareItem(
        6, "周末快乐充电站", "把烦恼暂时放在一边",
        "2.7万", "流行",
        listOf(Color(0xFFD49A46), Color(0xFFF4D69C))
    )
)

@Composable
fun PlaylistSquareContent(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onPlaylistClick: (Long) -> Unit = {},
    onPlayPlaylist: (Long) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("推荐") }

    val visiblePlaylists = remember(selectedCategory) {
        if (selectedCategory == "推荐") {
            demoPlaylists
        } else {
            demoPlaylists.filter { it.category == selectedCategory }
        }
    }

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

            // 精选推荐 Banner
            item {
                FeaturedPlaylist(
                    onClick = { onPlaylistClick(100L) },
                    onPlayClick = { onPlayPlaylist(100L) }
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
                    playlistCategories.forEach { category ->
                        val selected = category == selectedCategory

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (selected) Color(0xFFD889A3)
                                    else Color.White
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 17.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
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

                    if (visiblePlaylists.isEmpty()) {
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
                        visiblePlaylists.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(13.dp)
                            ) {
                                rowItems.forEach { playlist ->
                                    PlaylistCard(
                                        playlist = playlist,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onPlaylistClick(playlist.id)
                                        }
                                    )
                                }

                                // 单数张歌单时保留右侧空位
                                if (rowItems.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(15.dp))
                        }
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
                        demoPlaylists.sortedBy { it.id }.take(5).forEach { playlist ->
                            Column(
                                modifier = Modifier
                                    .width(138.dp)
                                    .clickable {
                                        onPlaylistClick(playlist.id)
                                    }
                            ) {
                                PlaylistCover(
                                    playlist = playlist,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = playlist.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333039),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${playlist.playCount} 次播放",
                                    fontSize = 11.sp,
                                    color = Color(0xFF99939D)
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
private fun FeaturedPlaylist(
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(186.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF795B89),
                        Color(0xFFC28DA8),
                        Color(0xFFF0C9C8)
                    )
                )
            )
            .clickable(onClick = onClick)
    ) {
        // 装饰图形
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 22.dp)
                .size(138.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 43.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Text(
                text = "♫",
                fontSize = 62.sp,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp, end = 120.dp)
        ) {
            Text(
                text = "EDITOR'S PICK",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "把日子调成\n温柔模式",
                fontSize = 22.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(9.dp))

            Text(
                text = "治愈系 · 30 首歌曲",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                onClick = onPlayClick,
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
    playlist: PlaylistSquareItem,
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
            text = playlist.name,
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
                text = "${playlist.playCount} 播放",
                fontSize = 10.sp,
                color = Color(0xFF8F8994)
            )
        }
    }
}

@Composable
private fun PlaylistCover(
    playlist: PlaylistSquareItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(playlist.coverColors)
            )
    ) {
        // 装饰圆环
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.13f))
        ) {
            Icon(painter = painterResource(Res.drawable.music), contentDescription = null, tint = Color.White, modifier = Modifier.size(35.dp).align(Alignment.Center))
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(9.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.18f)
        ) {
            Text(
                text = playlist.category,
                modifier = Modifier.padding(
                    horizontal = 7.dp,
                    vertical = 4.dp
                ),
                fontSize = 10.sp,
                color = Color.White
            )
        }
        Icon(
            painter = painterResource(Res.drawable.music),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
            .align(Alignment.BottomEnd)
            .padding(end = 5.dp, bottom = 10.dp)
        )
    }
}
