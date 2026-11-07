package wzy.hkta.rhythbox.components.song

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.more
import wzy.hkta.rhythbox.components.AudioVisualizerIcon
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.utils.formatDuration

@Composable
fun SongItem(
    isPlay: Boolean,
    isPlaying: Boolean?,
    no: Int,
    title: String,
    subTitle: String,
    duration: Long,
    onMoreClick: () -> Unit,
    onClick: (() -> Unit)?
) {
    val drw = Res.drawable
    Row(
        modifier = Modifier.padding(start = 10.dp, end = 5.dp).clip(RoundedCornerShape(10f))
            .clickable {
                onClick?.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(45.dp), contentAlignment = Alignment.Center) {
            when (isPlay) {
                true -> AudioVisualizerIcon(
                    modifier = Modifier.size(14.dp),
                    isPlaying = isPlaying ?: true
                )

                false -> Text(
                    no.toString(),
                    color = "#666666".cssToColor()
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).padding(horizontal = 5.dp)
        ) {
            Text(
                title,
                color = if (isPlay) MaterialTheme.colorScheme.primary else "#333333".cssToColor(),
                fontSize = 18.sp,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
            val color = if (isPlay) MaterialTheme.colorScheme.primary else "#666666".cssToColor()
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    formatDuration(duration),
                    modifier = Modifier.width(40.dp),
                    color = color,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subTitle,
                    modifier = Modifier.widthIn(max = 140.dp),
                    color = color,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }
        }
        Row(
            modifier = Modifier.padding(start = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onMoreClick()
            }) {
                Icon(
                    painter = painterResource(drw.more),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        }
    }
}