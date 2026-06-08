package wzy.hkta.rhythbox.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import wzy.hkta.rhythbox.cssToColor

@Composable
fun AudioVisualizerIcon(
    modifier: Modifier = Modifier,
    isPlaying: Boolean?,
    color: Color = MaterialTheme.colorScheme.primary, // Rhythbox 的火箭紫色
    barCount: Int = 4 // 柱子的数量
) {
    // 1. 核心大招：无限循环的动画时间轴。如果暂停了，就停在 0f
    val transition = rememberInfiniteTransition(label = "Visualizer")
    val lastHeight = remember { MutableList(barCount) { 0.2f } }
    // 制造 4 个不同步的动画高度系数（0.2f 到 1.0f 之间起伏）
    val animFractions = List(barCount) { index ->
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                // 故意让每个柱子的交替时间（Duration）和延迟（Delay）不一样，制造参差不齐的乱跳感
                animation = tween(
                    durationMillis = 350 + (index * 70),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    // 2. 开始自绘
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 计算每个柱子的宽度和间距（比例：3份柱子宽，1份间距）
        val barWidth = width / (barCount + (barCount - 1) * 0.33f)
        val spacing = barWidth * 0.33f

        for (i in 0 until barCount) {
            // 拿到当前柱子的高度系数
            if (isPlaying == null || isPlaying) {
                lastHeight[i] = height * animFractions[i].value;
            }
            val x = i * (barWidth + spacing)
            // 因为 Canvas 坐标系左上角是 (0,0)，往上跳其实是 Y 轴坐标往下挪
            val y = height - lastHeight[i]
            // 画纯净的矩形（也可以用 drawRoundRect 画带圆角的，更好看）
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, lastHeight[i]),
                cornerRadius = CornerRadius(
                    x = barWidth / 2,
                    y = barWidth / 2
                )
            )
        }
    }
}