package wzy.hkta.rhythbox.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.ashcan
import rhythbox.composeapp.generated.resources.folder_pink
import rhythbox.composeapp.generated.resources.folder_plus
import rhythbox.composeapp.generated.resources.scan
import rhythbox.composeapp.generated.resources.scanEmpty
import wzy.hkta.rhythbox.expect.DirItem
import wzy.hkta.rhythbox.expect.ScanStatus
import wzy.hkta.rhythbox.expect.ScanStatus.*
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.song.RhythboxSongEmptyState
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.utils.Rhythbox

class ScanMusicScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel { ScanMusicViewModel(navigator) }
        val dirs = vm.dirs?.collectAsState()
        val currentPath = vm.currentPath?.collectAsState()
        val scanStatus = vm.scanStatus?.collectAsState()
        val progress = vm.progress?.collectAsState()
        val checkMap = remember { mutableStateMapOf<String, Boolean>() }
        val dirList = dirs?.value?.values?.toList() ?: emptyList()
        LaunchedEffect(dirList) {
            checkMap.clear()
            dirList.forEach { checkMap[it.fullPath] = true }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            NavBar("添加音乐")
            Spacer(modifier = Modifier.height(40.dp))
//            Step1(modifier = Modifier.weight(1f).fillMaxWidth())
            Step2(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                currentPath = currentPath?.value ?: "",
                dirs = dirList,
                progress = progress?.value ?: 0f,
                scanStatus = scanStatus?.value,
                checkMap = checkMap,
                onChange = { k, v -> checkMap[k] = v }
            )
            Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                Button(
                    modifier = Modifier.align(Alignment.Center).width(180.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    onClick = { vm.startScan(checkMap) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            painterResource(Res.drawable.scan),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        val text = when (scanStatus?.value) {
                            ScanIng -> "取消扫描"
                            WaitIng -> "开始扫描"
                            Finished -> "保存结果"
                            Cancel -> "已取消重新扫描"
                            SaveIng -> "保存中"
                            SaveFinished -> "保存完成"
                            null -> "开始扫描"
                        }
                        Text(text, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
private fun Step1(
    modifier: Modifier = Modifier
) {
    var checked by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 8.dp
            ),
            colors = CardDefaults.cardColors().copy(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.height(80.dp).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.width(180.dp)
                ) {
                    Text(
                        "系统默认音乐路径",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "/storage/emulated/0",
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(40.dp))

                Switch(
                    checked = checked,
                    thumbContent = {
                        Box(modifier = Modifier.size(10.dp))
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = "#F0A18C".cssToColor(),
                    ),
                    onCheckedChange = {
                        checked = it
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            modifier = Modifier.width(180.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            onClick = {}
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    painterResource(Res.drawable.folder_plus),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Text("添加文件夹", fontSize = 16.sp)
            }

        }
        Text(
            "已添加路径",
            lineHeight = 60.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 30.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 40.dp)
        ) {
            items(10) {
                Row(
                    modifier = Modifier.clip(
                        RoundedCornerShape(3.dp)
                    ).clickable {

                    }.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(Res.drawable.folder_pink),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(30.dp)
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp).weight(1f)
                            .drawBehind {
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.08f),
                                    start = Offset(0f, size.height + 2),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "music",
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "D:music",
                                fontSize = 13.sp,
                                color = "#666666".cssToColor()
                            )
                        }
                        Icon(
                            painterResource(Res.drawable.ashcan),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun Step2(
    modifier: Modifier = Modifier,
    scanStatus: ScanStatus?,
    dirs: List<DirItem>,
    currentPath: String,
    progress: Float,
    checkMap: Map<String, Boolean>,
    onChange: (k: String, v: Boolean) -> Unit
) {

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/glottises/Success.json").decodeToString()
        )
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (scanStatus == ScanIng) {
            Text("正在开始扫描音乐中", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        } else if (scanStatus == Finished) {
            Text("扫描完成", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            if (scanStatus == ScanIng) {
                // 外层扫描进度
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary.copy(0.5f), // 纯粹的底色
                )

                // 2. 真正的进度圆环：叠在上面跑，两端用圆角
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent, // 设为透明，露出底层的完整圆环
                    strokeCap = StrokeCap.Round     // 头部是圆润的
                )

                Text(
                    currentPath,
                    fontSize = 14.sp,
                    overflow = TextOverflow.StartEllipsis,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(0.9f),
                )
            } else if (scanStatus == Finished && dirs.isNotEmpty()) {
                Image(
                    painter = rememberLottiePainter(composition = composition, iterations = 1),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Lottie animation"
                )
            }
        }

        if (dirs.isEmpty() && scanStatus == Finished) {
            RhythboxSongEmptyState(
                describe = "小精灵搜遍了所有角落，好像没有发现音符的踪迹哦~",
                image = Res.drawable.scanEmpty, // 🌟 期待你的专属扫描空插图上线！
            )
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(dirs) {

                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp).clip(
                            RoundedCornerShape(5.dp)
                        ).clickable {
                            if (scanStatus == Finished) {
                                onChange(it.fullPath, !(checkMap[it.fullPath] ?: false))
                            }
                        }.padding(start = 10.dp).padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        if (scanStatus == ScanIng) {
                            Icon(
                                painterResource(Res.drawable.folder_pink),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        } else if (scanStatus == Finished) {
                            Checkbox(
                                checked = checkMap[it.fullPath] ?: true,
                                modifier = Modifier.scale(0.6f).size(20.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedCheckmarkColor = Color.White,
                                    checkedBoxColor = MaterialTheme.colorScheme.primary,
                                    checkedBorderColor = MaterialTheme.colorScheme.primary,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(
                                        0.6f
                                    )
                                ),
                                onCheckedChange = { ck -> onChange(it.fullPath, ck) }
                            )
                        }

                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp).weight(1f)
                                .drawBehind {
                                    drawLine(
                                        color = Color.Black.copy(alpha = 0.08f),
                                        start = Offset(0f, size.height + 2),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 5.dp)
                            ) {
                                Text(
                                    it.fullPath,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.StartEllipsis,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${it.songList.size}首音乐",
                                    fontSize = 13.sp,
                                    color = "#666666".cssToColor()
                                )
                            }
                        }
                    }
                }
            }
        }


    }
}


class ScanMusicViewModel(val navigator: Navigator) : ScreenModel {
    val scanMusic = Rhythbox.scanMusic
    val dirs = scanMusic?.dir
    val currentPath = scanMusic?.currentPath
    val scanStatus = scanMusic?.scanStatus
    val progress = scanMusic?.progress


    override fun onDispose() {
        super.onDispose()
        scanMusic?.reset()
    }

    fun startScan(checkMap: Map<String, Boolean>) {
        screenModelScope.launch {
            when (scanStatus?.value) {
                ScanIng -> scanMusic?.cancel()
                WaitIng -> scanMusic?.scan()
                Finished -> {
                    scanMusic?.save(checkMap.filter { it.value }.keys.toList())
                    navigator.pop()
                }

                Cancel -> scanMusic?.scan()
                else -> {}
            }
        }
    }
}
