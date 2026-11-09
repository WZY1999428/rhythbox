package wzy.hkta.rhythbox.components.song

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.close_grey
import rhythbox.composeapp.generated.resources.like_grey
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.utils.Rhythbox
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import rhythbox.composeapp.generated.resources.like_pink
import wzy.hkta.rhythbox.components.AudioVisualizerIcon
import wzy.hkta.rhythbox.components.MAlertDialog
import wzy.hkta.rhythbox.expect.PlayMode
import wzy.hkta.rhythbox.database.CollectEntity
import wzy.hkta.rhythbox.screens.song.SongScreen
import wzy.hkta.rhythbox.utils.SongType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMenuBottomSheet(
    show: Boolean = true,
    onDismissRequest: () -> Unit,
) {
    val player = Rhythbox.player
    val navigator = LocalNavigator.currentOrThrow
    var showAlert by remember { mutableStateOf(false) }
    val mediaItems by player.mediaItems.collectAsState()
    val collectDao = Rhythbox.dataBase.getCollectDao()
    var currentSong by remember { mutableStateOf<SongType?>(null) }
    val playIndex by player.playIndex.collectAsState()
    val playMediaId = player.playMediaId.collectAsState()
    val isPlaying = player.isPlaying.collectAsState()
    val playMode = player.playMode.collectAsState()
    val columnState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(show) {
        if (!show) return@LaunchedEffect
        if (playIndex >= 0) columnState.animateScrollToItem(playIndex)
    }
    if (show) {
        ModalBottomSheet(
            dragHandle = {
                // 🌟 自定义一个精致的蜜桃粉小提手线
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(36.dp, 4.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            CircleShape
                        )
                )
            },
            containerColor = Color(0xFFFFFBF9), // 温暖微醺米白底色
            // 修改圆角
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp
            ),
            onDismissRequest = onDismissRequest,
        ) {
            Column(
                modifier = Modifier.height(600.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 15.dp, top = 10.dp, end = 5.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Text(
                            "播放列表",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 5.dp)
                        )
                        Text(
                            "(${mediaItems.size}首音乐)",
                            fontSize = 14.sp,
                            color = "#716D6D".cssToColor(),
                        )
                    }
                    Row(
                        modifier = Modifier.height(25.dp).padding(end = 10.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { player.onChangePlayMode() },
                    ) {
                        Text(
                            when (playMode.value) {
                                PlayMode.LIST_LOOP -> "列表循环"
                                PlayMode.SINGLE_LOOP -> "单曲循环"
                                PlayMode.LIST -> "列表播放"
                                PlayMode.SHUFFLE -> "随机播放"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (mediaItems.size == 0) {
                    RhythboxSongEmptyState(
                        describe = "黑胶唱机正静静等待，快去挑一首心动的旋律让它转动吧~",
                        buttonText = "去听歌",
                        onButtonClick = {
                            onDismissRequest()
                            navigator.push(SongScreen())
                        }
                    )
                } else {
                    LazyColumn(
                        state = columnState,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(mediaItems.size) {
                            mediaItems.getOrNull(it)?.let { item ->
                                PlayerMenuItem(
                                    isPlay = playMediaId.value == item.mediaId,
                                    isPlaying = isPlaying.value,
                                    no = it + 1,
                                    title = item.name,
                                    subTitle = item.artist ?: "未知歌手",
                                    isLike = item.isLike,
                                    onLike = {
                                        scope.launch {
                                            if (item.isLike) {
                                                collectDao.delete(item.id)
                                            } else {
                                                collectDao.insert(CollectEntity(songId = item.id))
                                            }
//                                            player.updateMediaItem(item.copy(isLike = !item.isLike))
                                        }
                                    },
                                    onRemove = {
                                        currentSong = item
                                        showAlert = true
                                    },
                                    onClick = {
                                        player.prepare(it)
                                        player.play()
                                    })
                            }
                            Spacer(modifier = Modifier.height(5.dp))

                        }
                    }
                }
            }
        }

        MAlertDialog(
            show = showAlert,
            title = "提示",
            onCancel = { showAlert = false },
            onConfirm = {
                if (currentSong != null) {
                    player.delSong(currentSong!!)
                }
                showAlert = false
            }
        ) { Text("确定要把此音乐从播放列表移除吗") }
    }
}


@Composable
private fun PlayerMenuItem(
    isPlay: Boolean,
    isPlaying: Boolean?,
    isLike: Boolean? = false,
    no: Int,
    title: String,
    subTitle: String,
    onLike: (() -> Unit)?,
    onRemove: (() -> Unit)?,
    onClick: (() -> Unit)?
) {
    val drw = Res.drawable
    Row(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(45.dp), contentAlignment = Alignment.Center) {
            when (isPlay) {
                true -> AudioVisualizerIcon(
                    modifier = Modifier.size(12.dp),
                    isPlaying = isPlaying
                )

                false -> Text(
                    no.toString(),
                    color = "#666666".cssToColor()
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).clickable {
                onClick?.invoke()
            }.padding(horizontal = 5.dp)

        ) {
            val color = if (isPlay) MaterialTheme.colorScheme.primary else "#666666".cssToColor()
            Text(
                title,
                color = color,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subTitle,
                color = color,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.width(55.dp).padding(start = 10.dp, end = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(if (isLike == true) drw.like_pink else drw.like_grey),
                tint = Color.Unspecified,
                modifier = Modifier.size(13.dp).clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onLike?.invoke()
                },
                contentDescription = null
            )
            Icon(
                painter = painterResource(drw.close_grey),
                tint = Color.Unspecified,
                modifier = Modifier.size(11.dp).clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onRemove?.invoke()
                },
                contentDescription = null
            )
        }
    }
}