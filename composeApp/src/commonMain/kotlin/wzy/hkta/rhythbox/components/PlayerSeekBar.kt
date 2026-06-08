package wzy.hkta.rhythbox.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wzy.hkta.rhythbox.cssToColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSeekBar(
    value: Float,
    duration: Long,
    modifier: Modifier = Modifier,
    onValueChangeFinished: ((time: Long) -> Unit)? = null,
    showThumb: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    height: Dp = 6.dp
) {
    var isSeeking by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    val currentValue = if (isSeeking) sliderValue else value;
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified
    ) {
        Slider(
            modifier = Modifier.fillMaxWidth().height((height + 14.dp)).then(modifier),
            value = currentValue,
            onValueChange = {
                isSeeking = true
                sliderValue = it
            },
            onValueChangeFinished = {
                isSeeking = false
                onValueChangeFinished?.invoke((duration * sliderValue).toLong())
            },
            valueRange = valueRange,
            thumb = {},
            track = {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val trackWidth = maxWidth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height + 9.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {

                        // 背景轨道
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height)
                                .clip(RoundedCornerShape(999.dp))
                                .background( MaterialTheme.colorScheme.primary.copy(0.24f))
                        )

                        // 已播放进度
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(currentValue)
                                .height(height)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        if (showThumb) {

                            val offsetX = trackWidth * currentValue - ((height + 8.dp) / 2);
                            Box(
                                modifier = Modifier
                                    .offset(x = offsetX)
                                    .size(height + 8.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = CircleShape,
                                        ambientColor = Color.Red,
                                        clip = false
                                    )
                                    .background(
                                        Color.White,
                                        CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                }
            }
        )
    }
}
