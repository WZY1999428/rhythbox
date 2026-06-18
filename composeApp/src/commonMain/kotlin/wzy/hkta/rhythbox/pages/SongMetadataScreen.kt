package wzy.hkta.rhythbox.pages


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import wzy.hkta.rhythbox.components.NavBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.album
import rhythbox.composeapp.generated.resources.artist
import rhythbox.composeapp.generated.resources.song
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.expect.editSongInfo
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SnackbarManager

class SongMetadataScreen(val id: Long) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var songName by remember { mutableStateOf("") }
        var artistName by remember { mutableStateOf("") }
        var albumName by remember { mutableStateOf("") }
        val songDao = Rhythbox.dataBase.getSongDao()
        val player = Rhythbox.player
        val scope = rememberCoroutineScope()

        var songInfo by remember { mutableStateOf<SongEntity?>(null) }

        LaunchedEffect(id) {
            songInfo = withContext(Dispatchers.IO) {
                songDao.getById(id)
            }
            if (songInfo != null) {
                songName = songInfo!!.name
                artistName = songInfo!!.artist ?: ""
                albumName = songInfo!!.album ?: ""
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp) // 为底部预留呼吸感
        ) {
            NavBar(title = "编辑音乐")
            // 📜 输入主体区：加上 verticalScroll，防止键盘弹起时遮挡内容导致报错崩溃
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 🎵 1. 歌曲名输入卡片
                MetadataInputField(
                    value = songName,
                    onValueChange = { songName = it },
                    label = "歌曲名称",
                    placeholder = "请输入歌曲名...",
                    icon = Res.drawable.song
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🎤 2. 歌手输入卡片
                MetadataInputField(
                    value = artistName,
                    onValueChange = { artistName = it },
                    label = "歌手/艺术家",
                    placeholder = "请输入歌手名字...",
                    icon = Res.drawable.artist
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 💿 3. 专辑输入卡片
                MetadataInputField(
                    value = albumName,
                    onValueChange = { albumName = it },
                    label = "所属专辑",
                    placeholder = "请输入专辑名称...",
                    icon = Res.drawable.album
                )
            }
            // 💾 4. 元气满满的“保存修改”蜜桃大胶囊按钮

            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = {
                        try {
                            scope.launch(Dispatchers.IO) {
                                songInfo?.let {
                                    val newItem = it.copy(
                                        name = songName,
                                        album = albumName,
                                        artist = artistName
                                    )
                                    editSongInfo(
                                        songInfo!!.path,
                                        songName,
                                        albumName,
                                        artistName
                                    )
                                    Rhythbox.dataBase.updateSongAndAlbumAndSinger(newItem, it)
                                    player.updateMediaItem(newItem)
                                }
                                navigator.pop() // 保存成功后乖乖退回上一页
                            }
                            SnackbarManager.show("保存成功")
                        } catch (e: Exception) {
                            navigator.pop() // 保存成功后乖乖退回上一页
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp), // 14dp 现代感大圆角
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB7A1), // 专属蜜桃粉
                        contentColor = Color.White          // 纯白文字
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "保存修改",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp // 让“保存修改”四个字微微撑开，更大气
                    )
                }
            }

        }
    }
}

// 🎨 抽离出来的高颜值、可复用表单输入框组件！
@Composable
fun MetadataInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: DrawableResource
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = Color(0xFF9E8E87)) }, // 柔和的浅咖色标签
        placeholder = { Text(text = placeholder, color = Color(0xFFC7BCB6)) },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF7D6E65) // 温润的咖色小图标
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp), // 软绵绵的现代圆角
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFB7A1),     // 聚焦时是标志性蜜桃粉！
            unfocusedBorderColor = Color(0xFFFFB7A1).copy(alpha = 0.3f), // 未聚焦时是淡淡的粉边
            focusedLabelColor = Color(0xFFFFB7A1),       // 聚焦时标签也变粉
            cursorColor = Color(0xFFFFB7A1),             // 光标也是粉粉的
            focusedTextColor = Color(0xFF4A3E3D),        // 输入文本是高级深咖色
            unfocusedTextColor = Color(0xFF4A3E3D)
        )
    )
}
