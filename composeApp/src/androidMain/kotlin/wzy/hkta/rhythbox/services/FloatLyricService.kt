package wzy.hkta.rhythbox.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.flow.MutableStateFlow
import wzy.hkta.rhythbox.utils.Rhythbox
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import wzy.hkta.rhythbox.expect.LyricStatus.Finished
import wzy.hkta.rhythbox.R
import wzy.hkta.rhythbox.utils.SettingManager

class FloatLyricService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private var _currentLyricText = MutableStateFlow("欢迎使用乐动盒")
    private var _nextLyricText = MutableStateFlow("")
    private var _isLock = MutableStateFlow(false)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private lateinit var layoutParams: WindowManager.LayoutParams


    //  正确的代码
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        // 激活生命周期状态
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (composeView == null) {
            initLayoutParams()
            initComposeView()
        }
        return START_STICKY
    }

    /**
     * 2. 配置专属的 3D 悬浮窗参数
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLayoutParams() {
        layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT // 宽度全屏，方便拖动或两边延展
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY // 系统级置顶层级
            // 黄金 Flag 组合：不拦截系统焦点、允许内容贴边
            this.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT // 支持 Compose 的透明背景
            gravity = Gravity.TOP or Gravity.START // 改用左上角坐标系，方便后面做拖动位置计算
            x = 0
            y = 0 // 默认距离顶部 400 像素
        }
    }

    /**
     * 3. 构建并挂载 Compose 悬浮窗视图
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initComposeView() {
        // 注入伪装的生命周期节点
        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatLyricService)
            setViewTreeSavedStateRegistryOwner(this@FloatLyricService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            val player = Rhythbox.player
            setContent {
                val currentTime = player.currentPosition.collectAsState()
                val lyricStatus = player.lyricStatus.collectAsState()
                val currentLyricText = _currentLyricText.collectAsState()
                val nextLyricText = _nextLyricText.collectAsState()
                val isLock = _isLock.collectAsState()

                val currentIndex by remember(currentTime.value) {
                    derivedStateOf {
                        var index = 0
                        val lyricStatus = lyricStatus.value
                        if (lyricStatus is Finished) {
                            val played =
                                lyricStatus.data.lyric.filter { it.timeMillis <= currentTime.value }
                            index = played.size - 1
                        }
                        if (index < 0) 0 else index
                    }
                }

                LaunchedEffect(currentIndex) {
                    val lyricStatus = lyricStatus.value
                    if (lyricStatus is Finished) {
                        _currentLyricText.value =
                            lyricStatus.data.lyric.getOrNull(currentIndex)?.text ?: ""
                        _nextLyricText.value =
                            lyricStatus.data.lyric.getOrNull((currentIndex + 1))?.text ?: ""
                    }
                }

                // 🌟 视觉重构：把原本死板的纯黑换成高级感满满的微醺磨砂胶囊
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(55.dp) // 稍微加高到 55.dp，让两行文字有足够的呼吸空间
                        .background(
                            color = Color(0xFF2F2522).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(2.dp) // 极其优雅的 16dp 温润圆角
                        )
                        .pointerInput(isLock.value) {
                            if (isLock.value) {
                                val params = this@FloatLyricService.layoutParams
                                var floatingX = params.x.toFloat()
                                var floatingY = params.y.toFloat()
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    floatingX += dragAmount.x
                                    floatingY += dragAmount.y
                                    params.x = floatingX.toInt()
                                    params.y = floatingY.toInt()
                                    composeView?.let {
                                        try {
                                            windowManager.updateViewLayout(it, params)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }) {
                    Icon(
                        painter = painterResource(if (isLock.value) R.drawable.unlock else R.drawable.lock),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(14.dp)
                            .offset(x = (10).dp, y = (10).dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                _isLock.value = !isLock.value
                            }
                    )

                    Icon(
                        painter = painterResource(R.drawable.close_white),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(12.dp)
                            .offset(x = (-10).dp, y = (10).dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                Rhythbox.floatLyric.hideLyric(true)
                            }
                    )


                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 6.dp, horizontal = 16.dp), // 增加水平内边距，防止文字贴边
                        verticalArrangement = Arrangement.Center // 垂直居中分布
                    ) {
                        // 🎵 1. 当前正在播放的歌词（主核心）
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = currentLyricText.value,
                            color = Color(0xFFFFF5F2), // 极其温润的暖白粉色，高亮显眼
                            textAlign = TextAlign.Center, // 必须居中，大厂标配！
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, // 加粗，一眼就能看清
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp)) // 留出微小的音符呼吸间距

                        // ⏳ 2. 即将播放的下一句歌词（辅助预告）
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = nextLyricText.value,
                            color = Color(0xFFFFB7A1).copy(alpha = 0.8f), // 标志性的透亮蜜桃粉！
                            textAlign = TextAlign.Center, // 同样居中，视觉一条线
                            fontSize = 12.sp, // 稍微缩小字号，主次分明
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 正式物理挂载到屏幕上
        windowManager.addView(composeView, layoutParams)
    }


    override fun onDestroy() {
        // 销毁时务必安全释放窗口，否则会导致气泡不消失且内存泄漏
        composeView?.let {
            windowManager.removeView(it)
            composeView = null
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}