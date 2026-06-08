package wzy.hkta.rhythbox.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistInfoBottomSheet(
    show: Boolean = false,
    onDismissRequest: () -> Unit,
    playlistName: String = "郭德纲相声集",
    songCount: Int = 120,
    createDate: String = "2025-01-05",
    description: String = "编辑歌单简介...",
    onDeleteClick: () -> Unit
) {
    if (show) {
        ModalBottomSheet(
            dragHandle = null,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // 稍微圆润一点更有质感
            onDismissRequest = onDismissRequest,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp) // 留出底部系统导航栏的呼吸感
            ) {
                // --- 1. 头部区域（封面 + 歌单名） ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 歌单微缩封面
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Image(
                            painterResource(Res.drawable.logo),
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = null
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlistName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(
                    Modifier,
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.2f)
                )

                // --- 2. 核心信息列表区域 ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // 歌曲数量行
                    InfoRow(label = "歌曲数量", value = "${songCount}首") {}

                    // 创建日期行
                    InfoRow(label = "创建日期", value = createDate)

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- 3. 危险操作区域（删除按钮） ---
                    // 极客设计：危险按钮不宜用宽大的实色块，用带有红边或红字的轻量按钮，高级且不突兀
                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(
                                Color.Red.copy(
                                    alpha = 0.3f
                                )
                            )
                        )
                    ) {
                        Text("删除歌单", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = Color.DarkGray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, fontSize = 14.sp, color = Color.Gray)
        }
    }
}