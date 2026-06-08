package wzy.hkta.rhythbox.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import wzy.hkta.rhythbox.GetCover

@Composable
fun AddPlaylist(
    show: Boolean,
    onCancel: () -> Unit,
    onConfirm: (name: String, describe: String?) -> Unit
) {
    var describe by rememberSaveable { mutableStateOf("") }
    var playListName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(show) {
        describe = ""
        playListName = ""
    }

    if (show) {
        Dialog(onDismissRequest = { onCancel() }) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(5.dp), // 焊上你的 5.dp 专属圆角
                color = Color.White // 弹窗背景色
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "新建歌单",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "歌单名称",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(6.dp))

                    CompactTextField(
                        value = playListName,
                        onValueChange = { playListName = it },
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "描述",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(6.dp))
                    CompactTextField(
                        value = describe,
                        maxLines = 3,
                        height = 80.dp,
                        onValueChange = { describe = it },
                    )

                    Spacer(Modifier.height(24.dp))

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
                            onClick = { onConfirm(playListName, describe) },
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


@Composable
fun CompactTextField(
    value: String,
    maxLines: Int = 1,
    minLines: Int = 1,
    height: Dp = 40.dp,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        maxLines = maxLines,
        minLines = minLines,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .border(1.dp, "#FBEEE5".cssToColor(), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = if (maxLines > 1) Alignment.TopStart else Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text("请输入歌单名称", color = Color.Gray, fontSize = 14.sp)
                }
                innerTextField()
            }
        }
    )
}