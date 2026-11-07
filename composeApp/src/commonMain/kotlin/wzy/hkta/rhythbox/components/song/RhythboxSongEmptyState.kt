package wzy.hkta.rhythbox.components.song


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.defaultpage

@Composable
fun RhythboxSongEmptyState(
    describe: String,                        // 空状态的文案提示
    image: DrawableResource? = Res.drawable.defaultpage,
    buttonText: String? = null,              // 按钮文字（为 null 时不显示按钮）
    onButtonClick: (() -> Unit)? = null       // 按钮的点击回调事件
) {
    // 🌟 Column 铺满全屏，并让内部所有小组件水平、垂直绝对居中
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. 缺省页插图
        Image(
            painter = painterResource(image!!),
            contentDescription = "Empty State Image",
            modifier = Modifier.size(120.dp)
        )

        // 2. 左右大呼吸感间距
        Spacer(modifier = Modifier.height(60.dp))

        // 3. 右侧的文字与按钮区域
        Column(
            horizontalAlignment = Alignment.CenterHorizontally // 🌟 强行让文字和按钮左对齐，视觉上会非常整齐！
        ) {
            // 温暖治愈的咖色文案提示
            Text(
                text = describe,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp, // 稍微缩小一点点，显得更精致
                fontWeight = FontWeight.Medium
            )

            // 🌟 核心优化：在文字和按钮之间加上 12.dp 的呼吸间距！
            Spacer(modifier = Modifier.height(30.dp))
            if (buttonText != null && onButtonClick != null) {
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 13.sp, // 按钮文字调到 13.sp，秀气十足
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}