package wzy.hkta.rhythbox.theme

import androidx.compose.ui.graphics.Color
import wzy.hkta.rhythbox.cssToColor

// 微醺蜜桃（Peach Sensation）
val PeachSensationPrimary = "#FFFFB7A1".cssToColor()
val PeachSensationContainer = "#FFFFEBE3".cssToColor()
val PeachSensationBackground = "#FFFFFBF9".cssToColor()
val PeachSensationTextOrIcon = "#FF4A3E3D".cssToColor()

// 大草原的微风（Savanna Breeze）
val SavannaBreezePrimary = "#FF99B898".cssToColor()
val SavannaBreezeContainer = "#FFE2ECC8".cssToColor()
val SavannaBreezeBackground = "#FFF9FBF7".cssToColor()
val SavannaBreezeTextOrIcon = "#FF3D4A41".cssToColor()

val PeachGradientStops = arrayOf(
    0f to "#81B3B4".cssToColor(),
    0.5f to "#C4A5A3".cssToColor(),
    1.00f to "#FC9B94".cssToColor()
)

val SavannaGradientStops = arrayOf(
    0f to "#4A6E5D".cssToColor(),   // 0f：顶部深邃的林间暮色
    0.5f to "#BCCFB1".cssToColor(), // 0.5f：中间通透的晨雾浅绿
    1.00f to "#99B898".cssToColor() // 1.00f：底部温暖的青枫绿
)

//
//        val PeachCustomColors = RhythBoxCustomColors(
//            playerGradientStops = arrayOf(
//                0f to "#81B3B4".cssToColor(),
//                0.5f to "#C4A5A3".cssToColor(),
//                1.00f to "#FC9B94".cssToColor()
//            ),
//            lyricCurrent = Color(0xFF4A3E3D),
//            lyricTotalTrack = Color(0xFFFFB7A1).copy(alpha = 0.24f)
//        )
//
//// 🍃 【大草原的微风】专属：深林暮绿 -> 晨雾浅绿 -> 静谧青枫绿（本设计师熬夜为你调配！）
//        val SavannaCustomColors = RhythBoxCustomColors(
//            playerGradientStops = arrayOf(
//                0f to "#4A6E5D".cssToColor(),   // 0f：顶部深邃的林间暮色
//                0.5f to "#BCCFB1".cssToColor(), // 0.5f：中间通透的晨雾浅绿
//                1.00f to "#99B898".cssToColor() // 1.00f：底部温暖的青枫绿
//            ),
//            lyricCurrent = Color(0xFF1E2621),
//            lyricTotalTrack = Color(0xFF99B898).copy(alpha = 0.24f)
//        )