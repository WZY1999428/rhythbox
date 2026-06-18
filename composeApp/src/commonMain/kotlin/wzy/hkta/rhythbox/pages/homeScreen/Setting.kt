package wzy.hkta.rhythbox.pages.homeScreen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.earphone
import rhythbox.composeapp.generated.resources.email
import rhythbox.composeapp.generated.resources.frequency
import rhythbox.composeapp.generated.resources.lang
import rhythbox.composeapp.generated.resources.lock2
import rhythbox.composeapp.generated.resources.lyrics
import rhythbox.composeapp.generated.resources.right
import rhythbox.composeapp.generated.resources.scanfolder
import rhythbox.composeapp.generated.resources.scanning2
import rhythbox.composeapp.generated.resources.theme
import rhythbox.composeapp.generated.resources.warn
import wzy.hkta.rhythbox.components.MAlertDialog
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.pages.ScanMusicScreen
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SettingManager
import wzy.hkta.rhythbox.utils.StorageManager
import kotlin.Boolean

@Composable
fun Setting(modifier: Modifier = Modifier, onClose: () -> Unit) {
    val floatLyric = Rhythbox.floatLyric
    val navigator = LocalNavigator.currentOrThrow
    val showLyric = SettingManager.showFloatLyric.collectAsState()
    val pauseOnHeadsetDisconnect = SettingManager.pauseOnHeadsetDisconnect.collectAsState()
    val isSystemInDarkTheme = SettingManager.isSystemInDarkTheme.collectAsState()
    var showAlertDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier.align(alignment = Alignment.TopStart).fillMaxSize()
                .background(Color.Black.copy(0.3f)).clickable {
                    onClose()
                }.zIndex(3f)
        )
        Column(
            modifier = Modifier.align(alignment = Alignment.TopStart).fillMaxWidth(0.85f)
                .background(Color.White, RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                .fillMaxHeight().zIndex(5f).clickable(
                    indication = null,
                    enabled = true,
                    interactionSource = remember { MutableInteractionSource() }) {}
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "设置",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().height(45.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .padding(10.dp)
            ) {

                val cardColModifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)

                SettingCard(title = "音乐库与文件管理") {
                    Column(modifier = cardColModifier) {
//                        SettingRow(
//                            icon = Res.drawable.scanfolder,
//                            content = {
//                                SettingRowTitle("管理扫描路径")
//                            },
//                        )
                        SettingRow(
                            icon = Res.drawable.scanning2,
                            showBottomBorder = false,
                            content = {
                                SettingRowTitle("扫描音乐") {
                                    navigator.push(ScanMusicScreen())
                                }
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                SettingCard(title = "播放与音质设置") {
                    Column(modifier = cardColModifier) {
//                        SettingRow(
//                            icon = Res.drawable.frequency,
//                            content = {
//                                SettingRowTitle("音质控制")
//                            },
//                        )
                        SettingRow(
                            icon = Res.drawable.lyrics,
                            content = {
                                SettingRowTitle("桌面歌词")
                            },
                            action = {
                                SettingSwitch(
                                    checked = showLyric.value,
                                    onChangeValue = { checked ->
                                        if (checked) {
                                            SettingManager.changeShowFloatLyric(floatLyric.showLyric())
                                        } else {
                                            SettingManager.changeShowFloatLyric(false)
                                            floatLyric.hideLyric(true)
                                        }
                                    })
                            }
                        )
                        SettingRow(
                            icon = Res.drawable.earphone,
                            showBottomBorder = false,
                            content = {
                                SettingRowTitle("拔出耳机时暂停/继续播放")
                            },
                            action = {
                                SettingSwitch(
                                    checked = pauseOnHeadsetDisconnect.value,
                                    onChangeValue = {
                                        SettingManager.changePauseOnHeadsetDisconnect(it)
                                    })
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
//                SettingCard(title = "界面与主题设置") {
                SettingCard(title = "主题设置") {
                    Column(modifier = cardColModifier) {
                        SettingRow(
                            icon = Res.drawable.theme,
                            content = {
                                SettingRowTitle(
                                    "App 主题",
                                    subTitle = "静谧抹茶/微醺蜜桃（默认）"
                                )
                            },
                            action = {
                                SettingSwitch(
                                    checked = isSystemInDarkTheme.value,
                                    onChangeValue = { SettingManager.changeSystemInDarkTheme(it) })
                            }
                        )
//                        SettingRow(
//                            content = {
//                                SettingRowTitle(
//                                    "动态主题",
//                                    subTitle = "根据音乐封面提取主题色(只播放界面)"
//                                )
//                            },
//                            showBottomBorder = false,
//                            action = {
//                                SettingSwitch(checked = true, onChangeValue = {})
//                            }
//                        )
//                        SettingRow(
//                            icon = Res.drawable.lang,
//                            showBottomBorder = false,
//                            content = {
//                                SettingRowTitle("全局字体大小")
//                            },
//                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                SettingCard(title = "关于与隐私") {
                    Column(modifier = cardColModifier) {
                        SettingRow(
                            icon = Res.drawable.lock2,
                            content = {
                                SettingRowTitle(
                                    "隐私政策/用户协议",
                                    onClick = { showAlertDialog = true })
                            },
                        )
                        SettingRow(
                            icon = Res.drawable.email,
                            content = {
                                SettingRowTitle("反馈与建议") {
                                    Rhythbox.scanMusic?.openBrowser("https://github.com/WZY1999428/rhythbox-release/issues")
                                }
                            },
                        )

                        SettingRow(
                            icon = Res.drawable.warn,
                            showBottomBorder = false,
                            content = {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        Rhythbox.scanMusic?.openBrowser("https://github.com/WZY1999428/rhythbox-release/releases")
                                    }) {
                                    Text("版本号", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("v0.0.1", fontSize = 12.sp, color = Color.Gray)
                                }
                            },
                            action = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "检查更新",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painterResource(Res.drawable.right),
                                        tint = Color.Gray,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                    }
                }
            }
        }

        MAlertDialog(
            show = showAlertDialog,
            title = "隐私政策/用户协议",
            content =  """
欢迎来到乐动盒（RhythBox）！

在您开启治愈的音乐之旅前，小精灵在此向您做最诚挚的【安心隐私保证】：

1. 纯本地运行：本软件是一款 100% 纯本地媒体播放工具。我们【没有服务器】，也【绝对不会】联网上传、收集、窥探或倒卖您的任何个人隐私、听歌习惯或歌单数据。
2. 存储权限说明：为了帮您唤醒藏在手机各个角落里的音频旋律，我们需要申请读取本地存储的权限。请您放心，该权限【仅用于】在本地扫描、读取和播放音乐文件。
3. 免责声明：作为纯本地工具，所有的歌单和播放数据均保存在您的本地设备中。因手机系统故障、卸载清理、或您个人误操作导致的本地音频文件及数据丢失，本软件无法为您从云端找回，亦不承担相关法律责任。

请您仔细阅读并理解上述内容。点击“同意”即代表您接受我们的《用户协议》与《隐私政策》，让我们一起摇滚吧！
""".trimIndent(),
            onConfirm = { showAlertDialog = false })
    }
}


@Composable
private fun SettingRowTitle(
    title: String,
    subTitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) { onClick?.invoke() }) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        subTitle?.let {
            Text(it, fontSize = 12.sp, color = Color.Gray)
        }
    }
}


@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(5.dp).fillMaxWidth().height(30.dp)
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
            Text(
                title,
                fontWeight = FontWeight.Bold
            )

        }
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            content()
        }
    }
}


@Composable
private fun SettingRow(
    icon: DrawableResource? = null,
    content: @Composable () -> Unit,
    showBottomBorder: Boolean = true,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f).padding(start = 10.dp).padding(vertical = 2.dp)
                .drawBehind {
                    if (showBottomBorder) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.08f),
                            start = Offset(0f, size.height + 2),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }.padding(horizontal = 5.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) { content() }
            if (action != null) {
                action()
            } else {
                Icon(
                    painterResource(Res.drawable.right),
                    tint = Color.Gray,
                    contentDescription = null
                )
            }
        }
    }
}


@Composable
private fun SettingSwitch(
    checked: Boolean,
    onChangeValue: (Boolean) -> Unit
) {
    // 🎨 【在这里自由修改你想要的尺寸】
    val trackWidth = 40.dp  // 轨道宽度（默认是52，我们缩到40更精致）
    val trackHeight = 22.dp // 轨道高度
    val thumbSize = 16.dp   // 滑块大小
    val padding = 3.dp      // 滑块离轨道的内边距

    // 🔄 计算滑块的水平位移：开启时靠右，关闭时靠左
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) (trackWidth - thumbSize - padding) else padding
    )

    // 🎨 动态颜色联动
    val trackColor = if (checked) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
    val thumbColor = Color.White

    Box(
        modifier = Modifier
            .size(width = trackWidth, height = trackHeight)
            .background(color = trackColor, shape = CircleShape) // 胶囊形状轨道
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 禁用丑陋的默认点击水波纹
            ) {
                onChangeValue(!checked)
            },
        contentAlignment = Alignment.CenterStart // 从左侧开始排列滑块
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset) // 靠动画驱动的横向偏移
                .size(thumbSize)
                .background(color = thumbColor, shape = CircleShape) // 圆形滑块
        )
    }
}