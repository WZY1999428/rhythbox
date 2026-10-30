package wzy.hkta.rhythbox.screens.homeScreen.components

import androidx.compose.runtime.Composable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



data class Tabs(
    val label:String,
    val key : String
)


/**
 * 顶部页签导航组件
 *
 * @param tabs 页签文本列表
 * @param selectedIndex 当前选中的索引
 * @param onTabSelected 点击页签时的回调
 */
@Composable
fun TabHeader(
    tabs: List<Tabs>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    activeColor: Color = Color.Black,
    inactiveColor: Color = Color.Gray
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(12.dp,Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedIndex == index

            // 颜色与指示器动画过渡（比突变更平滑）
            val textColor by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                label = "textColor"
            )
            val indicatorWidth by animateDpAsState(
                targetValue = if (isSelected) 16.dp else 0.dp,
                label = "indicatorWidth"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clickable(
                        // 去掉默认点击水波纹，让 Tab 切换看起来更干净
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onTabSelected(index)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tab.label,
                    color = textColor,
                    fontSize = if (isSelected) 18.sp else 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )

                // 底部选中的圆角小指示条
                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .height(3.dp)
                        .background(
                            color = if (isSelected) activeColor else Color.Transparent,
                            shape = RoundedCornerShape(1.5.dp)
                        )
                )
            }
        }
    }
}