package wzy.hkta.rhythbox.screens.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.*
import wzy.hkta.rhythbox.components.BottomControl
import wzy.hkta.rhythbox.screens.download.DownloadScreen
import wzy.hkta.rhythbox.screens.album.AlbumScreen
import wzy.hkta.rhythbox.screens.FolderScreen
import wzy.hkta.rhythbox.screens.artist.ArtistScreen
import wzy.hkta.rhythbox.screens.favorite.FavoriteScreen
import wzy.hkta.rhythbox.screens.song.SongScreen
import wzy.hkta.rhythbox.screens.homeScreen.components.*;


class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator: Navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel { HomeViewModel() }
        var showSetting by remember { mutableStateOf(false) }
        var selectedIndex by remember { mutableIntStateOf(0) }
//        "本地", "发现", "在线
        val tabs = listOf(Tabs("本地","local"),Tabs("在线","online"))

        val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = selectedIndex)
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(pagerState.currentPage) {
            selectedIndex = pagerState.currentPage
        }

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
                modifier = Modifier// 🔥 处理旋转、缩放和透明度
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.height(45.dp).fillMaxWidth().padding(horizontal = 20.dp)
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


                            Box(modifier = Modifier.weight(1f)){
                                TabHeader(
                                    tabs = tabs,
                                    selectedIndex = selectedIndex,
                                    onTabSelected = { index ->
                                        coroutineScope.launch {
                                            selectedIndex=index
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                )
                            }

                            IconButton(onClick = {
                               navigator.push(DownloadScreen())
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.download_line),
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ){ page->
                            Box(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp)
                            ){
                                when(tabs[page].key){
                                    "local" -> LocalScreen(vm,navigator)
                                    else -> OnlineScreen(vm,navigator)
                                }
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
 fun CategoryRow(
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
            navigator.push(ArtistScreen())
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
 fun PlayListRow(navigator: Navigator) {
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
                    navigator.push(FavoriteScreen())
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


