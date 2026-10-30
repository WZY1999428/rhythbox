package wzy.hkta.rhythbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.engine.KtorHttpEngine
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.expect.FloatLyric
import wzy.hkta.rhythbox.expect.RhythboxPlayer
import wzy.hkta.rhythbox.expect.ScanMusic
import wzy.hkta.rhythbox.screens.homeScreen.HomeScreen
import wzy.hkta.rhythbox.theme.*
import wzy.hkta.rhythbox.utils.DownloadEngine
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SettingManager
import wzy.hkta.rhythbox.utils.SnackbarManager


// 🍑 【微醺蜜桃】 沉浸深色调性皮肤
private val PeachSensationScheme = darkColorScheme(
    primary = PeachSensationPrimary,               // 亮蜜桃金粉（进度条、高亮文字、主要按钮）
    surface = PeachSensationBackground,            // 微醺米白大背景（Scaffold 会自动读取它作为整屏底色）
    surfaceVariant = PeachSensationContainer,       // 半透明渗透粉（主页大卡片、智能列表小卡片、歌单格子的标准容器色）
    onSurface = PeachSensationTextOrIcon,          // 高级深咖色（大背景上的主要文字和细线条图标）
    onSurfaceVariant = PeachSensationTextOrIcon    // 卡片容器上的文字和图标颜色
)

// 🍃 【大草原的微风】 清爽明亮调性皮肤
private val SavannaBreezeScheme = lightColorScheme(
    primary = SavannaBreezePrimary,                 // 静谧青枫绿（进度条、高亮状态、主要按钮）
    surface = SavannaBreezeBackground,              // 晨雾青白大背景（整屏的大底色）
    surfaceVariant = SavannaBreezeContainer,        // 淡抹茶渗透色（各个卡片容器的底色）
    onSurface = SavannaBreezeTextOrIcon,            // 深森林绿（背景上的主要文字和图标）
    onSurfaceVariant = SavannaBreezeTextOrIcon      // 卡片容器上的文字和图标
)

@Composable
fun App(
    rhythboxPlayer: RhythboxPlayer,
    database: AppDatabase,
    scanMusic: ScanMusic,
    floatLyric: FloatLyric,
    downloadEngine: Ketch
) {
    val show = remember { mutableStateOf(false) }
    val currentSong = rhythboxPlayer.currentSong.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isSystemInDarkTheme = SettingManager.isSystemInDarkTheme.collectAsState()

    LaunchedEffect(Unit) {
        SnackbarManager.bind(snackbarHostState)
        Rhythbox.init(rhythboxPlayer, database, scanMusic, floatLyric)
        DownloadEngine.initEngine(downloadEngine)
        show.value = true
    }
    LaunchedEffect(currentSong.value, show.value) {
        if (!show.value) return@LaunchedEffect
        currentSong.value?.let {
            rhythboxPlayer.getSongLyric()
        }
    }

    if (show.value) {
        MaterialTheme(
            colorScheme = when (isSystemInDarkTheme.value) {
                true -> PeachSensationScheme
                false -> SavannaBreezeScheme
            }
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Navigator(HomeScreen()) { navigator ->
                        SlideTransition(navigator)
                    }
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.Center).zIndex(9999999f)
                    ) { data ->
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp))
                                .widthIn(min = 100.dp, max = 200.dp)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = data.visuals.message,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
            }
        }
    } else {
        Spacer(
            modifier = Modifier.fillMaxSize().background("#FCF8F2".cssToColor()), // 可以和你的主调色对齐
        )
    }
}

