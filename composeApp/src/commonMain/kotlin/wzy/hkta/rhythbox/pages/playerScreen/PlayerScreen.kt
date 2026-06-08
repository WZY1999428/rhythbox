package wzy.hkta.rhythbox.pages.playerScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import cafe.adriel.voyager.core.model.rememberScreenModel
import coil3.compose.AsyncImage
import coil3.toBitmap
import com.kmpalette.rememberPaletteState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rhythbox.composeapp.generated.resources.more
import wzy.hkta.rhythbox.expect.LyricStatus
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.theme.PeachGradientStops
import wzy.hkta.rhythbox.theme.SavannaGradientStops
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SettingManager
import wzy.hkta.rhythbox.utils.SnackbarManager
import wzy.hkta.rhythbox.utils.marqueeText


class PlayerScreen : Screen {

    @Composable
    override fun Content() {

        val vm = rememberScreenModel { PlayerViewModel() }
        val player = Rhythbox.player
        val progress = player.progress.collectAsState()
        val currentTime = player.currentPosition.collectAsState()
        val duration = player.duration.collectAsState()
        val playMode = player.playMode.collectAsState()
        val currentSong = player.currentSong.collectAsState()
        val isPlaying = player.isPlaying.collectAsState()
        val lyricStatus = player.lyricStatus.collectAsState()
        var offset by remember { mutableIntStateOf(0) }
        val isSystemInDarkTheme = SettingManager.isSystemInDarkTheme.collectAsState()
        var showLyricsOffsetController by remember { mutableStateOf(false) }
        LaunchedEffect(lyricStatus.value) {
            offset = 0
            showLyricsOffsetController = false
            if (lyricStatus.value is LyricStatus.Finished) {
                val data = (lyricStatus.value as LyricStatus.Finished).data
                offset = data.offset
                if (!data.lyric.isEmpty()) {
                    showLyricsOffsetController = true
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            Box(
                modifier = Modifier.fillMaxSize()
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colorStops = when (isSystemInDarkTheme.value) {
                                true -> PeachGradientStops
                                false -> SavannaGradientStops
                            },
                            start = Offset.Zero, // 左上角
                            end = Offset(x = widthPx, y = heightPx) // 完美的动态右下角
                        )
                    ),
            ) {
                NavBar(
                    title = currentSong.value?.name ?: "音乐盒",
                    titleColor = Color.White,
                    background = Color.Transparent,
                    iconColor = Color.White,
                    action = {
                        IconButton(onClick = {}) {
                            Icon(
                                painterResource(Res.drawable.more),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    }
                )
                Surface(
                    modifier = Modifier.align(alignment = Alignment.TopCenter)
                        .offset(y = 100.dp)
                        .size(180.dp),
                    border = BorderStroke(2.dp, Color.White),
                    shape = RoundedCornerShape(999.dp),
                    shadowElevation = 6.dp,
                ) {
                    val cover = currentSong.value?.cover
                    when (cover.isNullOrBlank()) {
                        true -> Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = null
                        )

                        false -> AsyncImage(
                            model = cover,
                            contentDescription = null,
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(0.8f)
                        .align(alignment = Alignment.TopCenter)
                        .offset(y = 300.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        currentSong.value?.name ?: "",
                        color = Color.White,
                        lineHeight = 80.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(1f, 4f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier.fillMaxWidth().marqueeText(),
                        maxLines = 1,
                        fontSize = 35.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        currentSong.value?.artist ?: "",
                        lineHeight = 50.sp,
                        color = Color.White,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().marqueeText(),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(1f, 4f),
                                blurRadius = 4f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                    Lyrics(
                        currentTime = currentTime.value,
                        lyricStatus = lyricStatus.value,
                        offset = offset,
                        onRetry = { player.getSongLyric() }
                    )
                }
                if (showLyricsOffsetController) {
                    Box(
                        modifier = Modifier.align(alignment = Alignment.CenterEnd)
                    ) {
                        LyricsOffsetController(
                            onDelayClick = {
                                offset -= 500
                                updateLyricsOffset(currentSong.value!!.id, offset)
                            },
                            onAdvanceClick = {
                                offset += 500
                                updateLyricsOffset(currentSong.value!!.id, offset)
                            }
                        )
                    }
                }


                PlayerBottom(
                    modifier = Modifier.align(Alignment.BottomStart),
                    progress = progress.value,
                    currentTime = currentTime.value,
                    duration = duration.value,
                    isPlay = isPlaying.value,
                    togglePlay = { player.togglePlay() },
                    playMode = playMode.value,
                    showToolbar = currentSong.value != null,
                    isLike = currentSong.value?.isLike ?: false,
                    prev = { player.prev() },
                    next = { player.next() },
                    seekTo = { player.seekTo(it) },
                    onRing = { player.setRhythboxRingtone() },
                    onChangePlayMode = { player.onChangePlayMode() },
                    onLike = {
                        vm.toggleCollect(
                            currentSong.value!!,
                            !(currentSong.value?.isLike ?: false)
                        )
                    },
                    onSelectFolder = {
                        vm.addFolderSong(
                            it,
                            currentSong.value!!.id,
                            currentSong.value!!.cover
                        )
                    }
                )
            }
        }
    }

}

private fun updateLyricsOffset(songId: Long, offset: Int) {
    SnackbarManager.show("歌词调整 ${offset.toFloat() / 1000.0} 秒")
    CoroutineScope(Dispatchers.IO).launch {
        Rhythbox.dataBase.getSongDao().updateLyricOffset(songId, offset)
    }
}

@Composable
fun LyricsOffsetController(
    onDelayClick: () -> Unit,  // 点击 -0.5s（歌词延后）
    onAdvanceClick: () -> Unit // 点击 +0.5s（歌词提前）
) {
    // 🌟 整体变成一个优雅的高级悬浮小胶囊
    Column(
        modifier = Modifier
            .padding(top = 16.dp, end = 16.dp) // 留出边缘间距，不要死贴着屏幕边缘
            .width(35.dp)                      // 稍微加宽一点，更方便单手点击
            .height(80.dp)                     // 撑开高度，给两个按钮留足空间
            .clip(RoundedCornerShape(14.dp))   // 软绵绵的大圆角
            .background(
                // 🌟 核心视觉：高级的深棕咖色半透明磨砂感底色（或者换成 Color(0xFFFFEBE3).copy(alpha = 0.9f) 蜜桃粉底色）
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ⏳ 1. 上半部分：-0.5s 按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable { onDelayClick() }, // 注入点击事件与优雅的水波纹
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "-\n0.5s",
                color = MaterialTheme.colorScheme.onSurface, // 标志性的亮蜜桃粉，在深色底上非常精致
                fontSize = 13.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }

        // 🌟 中间的分隔线，让边界更清晰
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.15f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // 🚀 2. 下半部分：+0.5s 按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable { onAdvanceClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "0.5s\n+",
                color = Color(0xFFFFEBE3), // 温润的淡粉白
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}