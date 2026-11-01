package wzy.hkta.rhythbox.screens.search.netSearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.more
import rhythbox.composeapp.generated.resources.music
import rhythbox.composeapp.generated.resources.right
import wzy.hkta.rhythbox.utils.fatchMusic.MUSIC_BASE_URL

/**
 * 1. 单曲项优化：贴合主流音乐软件的单曲排版，突出标题与副标题（歌手·专辑）
 */
@Composable
fun SearchSongItem(
    title: String,
    artist: String,
    album: String? = null,
    coverUrl: String? = null,
    onClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 单曲封面（可根据需要显示，如果不需要可以去掉，这里保留一个小巧的封面）
        SearchCover(
            coverUrl = coverUrl,
            modifier = Modifier.size(46.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    append(artist)
                    if (!album.isNullOrBlank()) {
                        append(" - $album")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.more),
                contentDescription = "更多操作",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 2. 专辑项列表样式优化（若采用横向列表）
 */
@Composable
fun SearchAlbumItem(
    title: String,
    artist: String,
    coverUrl: String? = null,
    year: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchCover(
            coverUrl = coverUrl,
            modifier = Modifier.size(56.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    append(artist.take(15))
                    if (artist.length > 15) append("…")
                    if (!year.isNullOrBlank()) {
                        append(" · ")
                        append(year)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            painter = painterResource(Res.drawable.right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 3. 歌单项优化：完美对齐截图二的歌单样式（标题 + 播放量/歌曲数）
 */
@Composable
fun SearchPlaylistItem(
    title: String,
    creator: String,
    coverUrl: String? = null,
    songCount: Int? = null,
    playCountText: String? = null, // 增加播放量支持，如 "566.4万"
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchCover(
            coverUrl = coverUrl,
            modifier = Modifier.size(56.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    if (songCount != null) {
                        append("${songCount}首")
                    }
                    if (creator.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append("by $creator")
                    }
                    if (!playCountText.isNullOrBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append("播放$playCountText")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            painter = painterResource(Res.drawable.right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 4. 歌手项优化：精确对应截图一的“歌手”列表风格（圆形头像 + 艺人名 + 单曲/专辑数统计）
 */
@Composable
fun SearchArtistItem(
    name: String,
    alias: String? = null,         // 比如英文名 (Jay Chou)
    songCount: Int? = null,        // 单曲数
    albumCount: Int? = null,       // 专辑数
    avatarUrl: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圆形歌手头像
        SearchCover(
            coverUrl = avatarUrl,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 歌手名字 + 别名
            Text(
                text = buildString {
                    append(name)
                    if (!alias.isNullOrBlank()) {
                        append(" ($alias)")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 统计信息：单曲 XXX 专辑 XXX
            Text(
                text = buildString {
                    if (songCount != null) append("单曲 $songCount  ")
                    if (albumCount != null) append("专辑 $albumCount")
                    if (songCount == null && albumCount == null) append("歌手")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            painter = painterResource(Res.drawable.right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 公共封面组件
 */
@Composable
private fun SearchCover(
    coverUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!coverUrl.isNullOrBlank()) {
            AsyncImage(
                model ="$MUSIC_BASE_URL$coverUrl" ,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.music),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}