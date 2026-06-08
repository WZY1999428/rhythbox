package wzy.hkta.rhythbox

import androidx.compose.ui.graphics.Color

fun String.cssToColor(): Color {
    // 1. 去掉 # 号
    val raw = this.replace("#", "")

    // 2. 将其转为 Long，并加上 0xFF000000 (处理 Alpha 通道)
    // hexRadix = 16 进制
    val colorLong = raw.toLong(16) or 0xFF000000L

    return Color(colorLong)
}

