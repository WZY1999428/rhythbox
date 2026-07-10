package wzy.hkta.rhythbox.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSongBottomSheet(
    show: Boolean = false,
    currentSort: String = "时间降序", // 🌟 进来一个当前选中的排序状态，用来做高亮
    onDismissRequest: () -> Unit,
    onSortSelected: (String) -> Unit // 🌟 切歌排序的点击回调
) {
    if (show) {
        ModalBottomSheet(
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(36.dp, 4.dp)
                        .background(Color(0xFFFFB7A1).copy(alpha = 0.4f), CircleShape)
                )
            },
            containerColor = Color(0xFFFFFBF9), // 温暖的微醺米白色底色
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), // 升级为大圆角，更具有二次元的软萌感
            onDismissRequest = { onDismissRequest() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp)
            ) {
                // 🏷️ 弹窗顶部的治愈系小标题
                Text(
                    text = "歌曲排序",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A3E3D).copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(start = 8.dp, bottom = 16.dp)
                )

                // 🗂️ 排序数据源，我们用一个优雅的循环把它们渲染出来
                val sortOptions = listOf("时间升序", "时间降序", "名称升序", "名称降序", "歌手升序", "歌手降序")

                sortOptions.forEach { option ->
                    val isSelected = option == currentSort

                    // 🌟 每一个选项都是一个精致的软果冻卡片
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            // 如果被选中，就赏赐它一层淡淡的蜜桃粉微醺背景，没选中就保持全透明
                            .background(if (isSelected) Color(0xFFFFB7A1).copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                onSortSelected(option)
                                onDismissRequest()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 🎵 文本显示：选中的项要加粗，颜色要深一点！
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF4A3E3D) else Color(0xFF4A3E3D).copy(alpha = 0.7f)
                            )

                            // 🎯 选中状态的甜美小指示器
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFFC9B94), CircleShape) // 经典蜜桃粉底端色圆点
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}