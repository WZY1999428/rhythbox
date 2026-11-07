package wzy.hkta.rhythbox.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 仿主流音乐APP的搜索结果顶部 Tab 栏
 *
 * @param tabs 标签列表，例如 listOf("综合", "单曲", "歌单", "歌手", "专辑", "视频")
 * @param selectedIndex 当前选中的索引
 * @param onTabSelected 切换标签的回调
 */
@Composable
fun Tabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp), // 标签之间的间距
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index

            // 可以根据喜好选择：
            // 样式 A：经典文字加粗 + 底部小横条指示器（类似于网易云/QQ音乐）
            // 样式 B：胶囊背景色高亮

            SearchTabItem(
                title = title,
                isSelected = isSelected,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
private fun SearchTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            fontSize = 15.sp
        )

        // 选中状态下的底部小圆点或小横条指示器
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                )
        )
    }
}