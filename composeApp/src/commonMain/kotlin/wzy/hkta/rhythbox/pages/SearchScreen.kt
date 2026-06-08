package wzy.hkta.rhythbox.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.back
import rhythbox.composeapp.generated.resources.search
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.components.SongBottomSheet
import wzy.hkta.rhythbox.components.SongItem
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.Rhythbox

class SearchScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var keyword by remember { mutableStateOf("") }
        val songDao = Rhythbox.dataBase.getSongDao()
        var songList by remember { mutableStateOf<List<SongEntity>>(emptyList()) }
        var selectSong by remember { mutableStateOf<SongEntity?>(null) }
        var showSongBottomSheet by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        LaunchedEffect(keyword) {
            if (keyword.isEmpty()) {
                songList = emptyList()
            } else {
                scope.launch(Dispatchers.IO) {
                    songList = songDao.search(keyword)
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().drawBehind {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.08f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }.windowInsetsPadding(WindowInsets.statusBars).height(45.dp).padding(end = 20.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = { navigator.pop() }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.back),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                EnhancedSearchBar(
                    keyword = keyword,
                    onKeywordChange = { keyword = it }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(songList.size) {
                    val item = songList[it]
                    SongItem(
                        isPlay = false,
                        isPlaying = false,
                        no = it + 1,
                        title = item.name,
                        subTitle = item.artist!!,
                        duration = item.duration,
                        onMoreClick = {
                            selectSong = item
                            showSongBottomSheet = true
                        },
                        onClick = {
                            Rhythbox.player.addSong(item)
                        }
                    )
                }

            }
        }

        if (selectSong != null) {
            SongBottomSheet(
                show = showSongBottomSheet,
                songName = selectSong!!.name,
                cover = selectSong!!.cover,
                songId = selectSong!!.id,
                artistName = selectSong!!.artist ?: "未知歌手",
                onDismissRequest = { showSongBottomSheet = false },
                onEdit = { navigator.push(SongMetadataScreen(selectSong!!.id)) },
                onDeleteClick = { showSongBottomSheet = false }
            )
        }
    }
}


@Composable
fun EnhancedSearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit
) {
    BasicTextField(
        value = keyword,
        onValueChange = onKeywordChange,
        // 1. 指定明确的高度，让搜索框显得精致大气
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        // 2. 统一正文文本样式，防止其随系统默认字体变矮
        textStyle = TextStyle(
            color = Color(0xFF2C3E50), // 沉稳的深灰色正文
            fontSize = 14.sp
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // 键盘右下角显示“搜索”
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    // 3. 改用饱满的胶囊大圆角（22.dp，高度的一半），比 3.dp 的小圆角现代感强很多
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(22.dp))
                    // 4. 细腻淡雅的浅色边框，不反客为主
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                    // 5. ✨ 极其关键：给四周加上呼吸感内边距，让文字和图标离边缘有安全距离
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically // 确保整行内容完美垂直居中
            ) {
                // 放大镜图标挪到左边（符合常规用户习惯，更加协调）
                Icon(
                    painter = painterResource(Res.drawable.search),
                    contentDescription = "搜索图标",
                    tint = Color(0xFF94A3B8), // 柔和的灰蓝色
                    modifier = Modifier.size(18.dp) // 控制图标大小合适
                )

                Spacer(modifier = Modifier.width(8.dp)) // 图标与文字的优雅间距

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (keyword.isEmpty()) {
                        Text(
                            text = "请输入搜索内容",
                            color = Color(0xFF94A3B8), // 占位符浅灰色
                            fontSize = 14.sp
                        )
                    }
                    innerTextField() // 真实的文本输入流
                }

                // 💡 进阶小细节：如果用户输入了内容，右边可以显示一个快捷清除的 "X" 图标
                if (keyword.isNotEmpty()) {
                    // 这里可以放一个可以点击的清除按钮，例如：
                    // IconButton(onClick = { onKeywordChange("") }, modifier = Modifier.size(16.dp)) { ... }
                }
            }
        }
    )
}