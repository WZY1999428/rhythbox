package wzy.hkta.rhythbox.screens.homeScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import wzy.hkta.rhythbox.expect.LyricStatus
import wzy.hkta.rhythbox.expect.LyricStatus.*
import wzy.hkta.rhythbox.components.PlayerSeekBar
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.marqueeText

@Composable
 fun PlayCard(
    progress: Float? = 0f,
    duration: Long = 0,
    currentTime: Long = 0,
    currentSong: SongType? = null,
    lyricStatus: LyricStatus?,
    seekTo: (v: Long) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val currentIndex by remember(currentTime) {
        derivedStateOf {
            var index = 0
            if (lyricStatus is Finished) {
                val lyric = lyricStatus.data.lyric
                val played = lyric.filter { it.timeMillis <= currentTime }
                index = played.size - 1
            }
            if (index < 0) 0 else index
        }
    }
    LaunchedEffect(currentIndex) {
        lazyListState.animateScrollToItem(
            index = currentIndex,
            scrollOffset = -35 // 💡 这个数值你可以根据你的 260.dp 高度自己微调
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.2f to MaterialTheme.colorScheme.surfaceVariant, // 52% 的位置
                            1.00f to MaterialTheme.colorScheme.primary,  // 100% 的位置
                        ),
                        start = Offset(x = 100f, y = 800f),
                        end = Offset.Zero
                    ),
                    shape = RoundedCornerShape(20.dp)
                ).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(666.dp),
                border = BorderStroke(2.dp, Color.White),
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                if (currentSong?.cover == null) {
                    Image(
                        contentDescription = null,
                        painter = painterResource(Res.drawable.logo),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = currentSong.cover,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))



            Text(
                currentSong?.name?: "乐动盒",
                modifier = Modifier.marqueeText(),
                fontSize = 18.sp,
                maxLines = 1,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                currentSong?.artist ?: "",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.height(60.dp).fillMaxWidth()
            ) {
                when (lyricStatus) {
                    is Finished -> {
                        val lyric = lyricStatus.data.lyric
                        val tlyric = lyricStatus.data.tlyric
                        items(lyric.size) {
                            val item = lyric[it]
                            val tItem = tlyric?.getOrNull(it)
                            val tl = if (tItem != null) "\n ${tItem.text}" else ""
                            Text(
                                "${item.text} $tl",
                                fontSize = if (currentIndex != it) 14.sp else 18.sp,
                                color = if (currentIndex == it) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().marqueeText(),
                                maxLines = 1,
                                lineHeight = 30.sp
                            )
                        }
                    }

                    Init -> item {
                        Text(
                            "欢迎使用乐动盒",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Loading -> item {
                        Text(
                            "歌词加载中。。。",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is LoadErr -> item {
                        Text(
                            lyricStatus.msg,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    NotFound, null -> item {
                        Text(
                            "暂无歌词",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                items(1) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                PlayerSeekBar(
                    value = progress ?: 0f,
                    valueRange = 0f..1f,
                    duration = duration,
                    onValueChangeFinished = { seekTo(it) })
            }
        }
    }
}
