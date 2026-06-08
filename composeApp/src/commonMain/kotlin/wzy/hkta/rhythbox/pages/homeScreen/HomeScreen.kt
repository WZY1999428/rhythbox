package wzy.hkta.rhythbox.pages.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.*
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.pages.AlbumScreen
import wzy.hkta.rhythbox.pages.FolderScreen
import wzy.hkta.rhythbox.pages.SearchScreen
import wzy.hkta.rhythbox.pages.SingerScreen
import wzy.hkta.rhythbox.pages.SongScreen
import wzy.hkta.rhythbox.utils.Rhythbox

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator: Navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel { HomeViewModel() }
        val player = Rhythbox.player
        val progress by player.progress.collectAsState()
        val duration by player.duration.collectAsState()
        val currentSong by player.currentSong.collectAsState()
        val currentTime by player.currentPosition.collectAsState()
        val lyricStatus by player.lyricStatus.collectAsState()
        var showSetting by remember { mutableStateOf(false) }

        val mainRotation by animateFloatAsState(
            targetValue = if (showSetting) -20f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        val mainScale by animateFloatAsState(
            targetValue = if (showSetting) 0.85f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        val mainTranslation by animateFloatAsState(
            targetValue = if (showSetting) 280f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        val settingRotation by animateFloatAsState(
            targetValue = if (showSetting) 0f else -60f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        val settingAlpha by animateFloatAsState(
            targetValue = if (showSetting) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier// 🔥 核心魔法：处理旋转、缩放和透明度
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(1f, 0.5f)
                        rotationY = mainRotation
                        scaleX = mainScale
                        scaleY = mainScale
                        translationX = mainTranslation
                        cameraDistance = 12f * density
                    }.zIndex(6f).fillMaxSize().background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.height(45.dp).fillMaxWidth()
                        ) {
                            Text(
                                "乐动盒",
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    showSetting = true
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                            IconButton(onClick = {
                                navigator.push(SearchScreen())
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.search),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        PlayCard(
                            progress = progress,
                            duration = duration,
                            currentTime = currentTime,
                            currentSong = currentSong,
                            cover = currentSong?.cover,
                            lyricStatus = lyricStatus,
                            seekTo = { player.seekTo(it) }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryRow(navigator)
                        Spacer(modifier = Modifier.height(20.dp))
                        PlayListRow(navigator)
                        Spacer(modifier = Modifier.height(10.dp))
                        PlayListCards(vm, navigator)
                    }
                }
                BottomControl()
            }
            AnimatedVisibility(
                modifier = Modifier.zIndex(9f),
                enter = EnterTransition.None,
                exit = ExitTransition.None,
                visible = showSetting
            ) {
                Setting(
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        rotationY = settingRotation
                        alpha = settingAlpha
                        cameraDistance = 12f * density
                    }.background(Color.Black.copy(alpha = 0.5f)).zIndex(9f),
                    onClose = {
                        showSetting = false
                    }
                )
            }
        }
    }
}


@Composable
private fun CategoryRow(
    navigator: Navigator
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Category(
            "歌曲",
            modifier = Modifier.weight(1f).aspectRatio(1f),
            Res.drawable.note,
        ) {
            navigator.push(SongScreen())
        }
        Category(
            "专辑",
            modifier = Modifier.weight(1f).aspectRatio(1f),
            Res.drawable.record
        ) {
            navigator.push(AlbumScreen())
        }
        Category(
            "歌手",
            modifier = Modifier.weight(1f).aspectRatio(1f),
            Res.drawable.mic
        ) {
            navigator.push(SingerScreen())
        }

        Category(
            "文件夹",
            modifier = Modifier.weight(1f).aspectRatio(1f),
            Res.drawable.Folder
        ) {
            navigator.push(FolderScreen())
        }
    }
}


@Composable
private fun PlayListRow(navigator: Navigator) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("智能播放列表")
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.height(80.dp).fillMaxWidth().padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            val modifier = Modifier.weight(1f).height(75.dp)
            Category(
                "最近添加",
                modifier = modifier,
                icon = Res.drawable.plusaudio,
                iconSize = 20.dp,
                fontSize = 12.sp,
                onClick = {
                    navigator.push(SongScreen("RecentlyAdded", "最近添加"))
                }
            )

            Category(
                "我的收藏",
                modifier = modifier,
                icon = Res.drawable.collect,
                iconSize = 20.dp,
                fontSize = 12.sp,
                onClick = {
                    navigator.push(SongScreen("MyCollection", "我的收藏"))
                }
            )

            Category(
                "听歌排行",
                modifier = modifier,
                icon = Res.drawable.champions,
                iconSize = 20.dp,
                fontSize = 12.sp,
                onClick = {
                    navigator.push(SongScreen("MusicRankings", "听歌排行"))
                }
            )
        }
    }
}


