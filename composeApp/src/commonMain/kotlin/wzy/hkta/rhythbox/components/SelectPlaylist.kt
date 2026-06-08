package wzy.hkta.rhythbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import wzy.hkta.rhythbox.cssToColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.checkcircle
import rhythbox.composeapp.generated.resources.queue_music
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.utils.Rhythbox

@Composable
fun SelectPlaylist(
    show: Boolean,
    onCancel: () -> Unit,
    onConfirm: (id:Long) -> Unit
) {
    var playList by remember { mutableStateOf(emptyList<PlaylistEntity>()) }
    var selected by remember { mutableLongStateOf(0L) }
    LaunchedEffect(show) {
        val playlistDao = Rhythbox.dataBase.getPlaylistDao()
        playList = playlistDao.getList()
        if (playList.isNotEmpty()) selected = playList[0].id
    }

    if (show) {
        Dialog(onDismissRequest = { onCancel() }) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(5.dp), // 焊上你的 5.dp 专属圆角
                color = Color.White // 弹窗背景色
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "选择保存歌单",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(24.dp))
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(playList) { item ->
                            val isSelected = selected == item.id
                            Card(
                                onClick = { selected = item.id },
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 8.dp,
                                            vertical = 12.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        painter = painterResource(
                                            if (isSelected)
                                                Res.drawable.checkcircle
                                            else
                                                Res.drawable.queue_music
                                        ),
                                        modifier = Modifier.size(16.dp),
                                        contentDescription = null
                                    )
                                   Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.name,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedButton(
                            onClick = { onCancel() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = { onConfirm(selected) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = "#e4a086".cssToColor()
                            ),
                            shape = CircleShape
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}
