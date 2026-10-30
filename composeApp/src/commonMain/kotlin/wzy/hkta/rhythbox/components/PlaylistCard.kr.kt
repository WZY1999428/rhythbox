package wzy.hkta.rhythbox.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.logo
import rhythbox.composeapp.generated.resources.play
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.utils.formatPlayTotal

@Composable
fun PlaylistCard(
    cover: String?,
    title: String,
    subtitle: String,
    playCount: Long, // 推荐用 String，方便传入 "5.2k" 或 "3.7w" 这种格式
    onPlay: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick?.invoke() }
            .padding(4.dp)
    ) {
        // 封面与右下角播放按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // 保持 1:1 正方形
                .clip(RoundedCornerShape(10.dp))
        ) {
            if(cover==null){
                Image(
                    painter = painterResource(Res.drawable.logo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    contentDescription = title,
                )
            }else{
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    contentDescription = title,
                    model = cover
                )
            }


            // 右下角浮动播放按钮
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background("#E2EFE0".cssToColor()) // 柔和的浅绿色背景
                    .clickable { onPlay?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.play),
                    contentDescription = "播放",
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFF6A8E68)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 歌单标题
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color(0xFF2C2C2C)
        )

        // 歌单副标题 / 简介
        Text(
            text = subtitle,
            fontSize = 12.sp,
            maxLines = 1,
            color = Color(0xFF959593),
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(3.dp))

        // 播放量统计行
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.play),
                contentDescription = null,
                tint = Color(0xFF979893),
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${formatPlayTotal(playCount)} Plays",
                fontSize = 11.sp,
                color = Color(0xFF979893)
            )
        }
    }
}
