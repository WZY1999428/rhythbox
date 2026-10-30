package wzy.hkta.rhythbox.screens.playerScreen

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wzy.hkta.rhythbox.expect.LyricStatus
import wzy.hkta.rhythbox.expect.LyricStatus.*

@Composable
internal fun Lyrics(
    modifier: Modifier = Modifier,
    height: Int = 260,
    currentTime: Long,
    offset: Int = 0,
    lyricStatus: LyricStatus?,
    onRetry: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    val currentIndex by remember(currentTime) {
        derivedStateOf {
            var index = 0
            if (lyricStatus is Finished) {
                val lyric = lyricStatus.data.lyric
                val played = lyric.filter { it.timeMillis <= (currentTime + offset) }
                index = played.size - 1
            }
            if (index < 0) 0 else index
        }
    }


    // 🎯 2. 核心：监听 currentIndex 的变化，一旦切歌或走到下一句，自动触发平滑滚动
    LaunchedEffect(currentIndex) {
        lazyListState.animateScrollToItem(
            index = currentIndex,
            scrollOffset = -135 // 💡 这个数值你可以根据你的 260.dp 高度自己微调
        )
    }
    Box(
        modifier = Modifier.height(height.dp).fillMaxWidth().then(modifier),
    ) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (lyricStatus) {
                is Finished -> {
                    val lyric = lyricStatus.data.lyric
                    val tlyric = lyricStatus.data.tlyric

                    items(lyric.size) {
                        val item = lyric[it]
                        val tItem = tlyric?.getOrNull(it)
                        val tl = if (tItem != null) "\n ${tItem.text}" else ""
                        if (currentIndex == it) {
                            CurrentLyric("${item.text} $tl")
                        } else {
                            Text(
                                "${item.text} $tl",
                                fontSize = 18.sp, lineHeight = 30.sp,
                                textAlign = TextAlign.Center,
                                modifier = modifier.fillMaxWidth(0.8f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    items(3) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                Init -> item {
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

                Loading -> item {
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

                is LoadErr -> item {
                    Text(
                        text = lyricStatus.msg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFEF5350),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp).clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onRetry() },
                        textAlign = TextAlign.Center
                    )
                }

                NotFound, null -> item {
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


@Composable
fun CurrentLyric(
    text: String,
) {
    var targetScale by remember {
        mutableFloatStateOf(1f)
    }

    LaunchedEffect(Unit) {
        targetScale = 1.1f
    }

    val scaleAnim by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        ),
        label = "lyricScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth(0.8f) // 保持宽度限制
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
    ) {
        Text(
            text = text,
            fontSize = 25.sp, // 字号保持一致，靠上面的 scaleAnim 来变大
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                drawStyle = Stroke(
                    width = 8f
                )
            ),
            color  = MaterialTheme.colorScheme.primary,
        )
        // 正常文字层 (细描边)
        Text(
            text = text,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            color  = MaterialTheme.colorScheme.surface,
            style = TextStyle(
                drawStyle = Stroke(
                    width = 2f
                )
            ),
        )
    }
}

