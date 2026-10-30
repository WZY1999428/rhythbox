package wzy.hkta.rhythbox.screens.playerScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.*
import wzy.hkta.rhythbox.components.CircularBarSpinner
import wzy.hkta.rhythbox.expect.PlayMode
import wzy.hkta.rhythbox.components.PlayerMenuBottomSheet
import wzy.hkta.rhythbox.components.PlayerSeekBar
import wzy.hkta.rhythbox.components.SelectPlaylist
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.utils.fatchMusic.LyricData
import wzy.hkta.rhythbox.utils.formatDuration

@Composable
internal fun PlayerBottom(
    modifier: Modifier = Modifier,
    progress: Float,
    currentTime: Long,
    playerLoading: Boolean = false,
    isPlay: Boolean,
    duration: Long,
    playMode: PlayMode,
    showToolbar: Boolean = false,
    togglePlay: () -> Unit,
    isLike: Boolean = false,
    lyricsCount: Int? = null,
    prev: () -> Unit,
    next: () -> Unit,
    openLyric: (() -> Unit)? = null,
    seekTo: (p: Long) -> Unit,
    onLike: (() -> Unit)? = null,
    onRing: (() -> Unit)? = null,
    onChangePlayMode: () -> Unit,
    onSelectFolder: ((id: Long) -> Unit)? = null,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectPlaylist by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().then(modifier)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showToolbar) {
                Row(
                    modifier = Modifier.width(240.dp).height(40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarBtn(
                        if (isLike) Res.drawable.like_pink else Res.drawable.like_grey,
                        tint = if (isLike) "#e74c3c".cssToColor() else Color.White
                    ) { onLike?.invoke() }

                    if (lyricsCount != null && lyricsCount >= 1) {
                        ToolbarBtn(
                            Res.drawable.lyrics,
                        ) { openLyric?.invoke() }
                    }
                    ToolbarBtn(Res.drawable.ring) { onRing?.invoke() }
                    ToolbarBtn(Res.drawable.folder_plus) { showSelectPlaylist = true }
                    ToolbarBtn(Res.drawable.share) {}
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp)
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayTimer(currentTime)
                Box(
                    modifier = Modifier.weight(1f).padding(horizontal = 5.dp),
                ) {
                    PlayerSeekBar(
                        height = 3.dp,
                        value = progress,
                        valueRange = 0f..1f,
                        duration = duration,
                        onValueChangeFinished = {
                            seekTo(it)
                        },
                    )
                }
                PlayTimer(duration)
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onChangePlayMode() }) {
                    Icon(
                        painter = when (playMode) {
                            PlayMode.LIST_LOOP -> painterResource(Res.drawable.list_loop)
                            PlayMode.SINGLE_LOOP -> painterResource(Res.drawable.single_loop)
                            PlayMode.LIST -> painterResource(Res.drawable.list)
                            PlayMode.SHUFFLE -> painterResource(Res.drawable.shuffle)
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { prev() }) {
                        Icon(
                            painterResource(Res.drawable.prev),
                            modifier = Modifier.size(15.dp),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier.padding(horizontal = 25.dp).size(50.dp)
                            .clip(RoundedCornerShape(888.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            .clickable { togglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        when (playerLoading) {
                            false -> Icon(
                                painterResource(if (isPlay) Res.drawable.pause2 else Res.drawable.play),
                                modifier = Modifier.size(20.dp),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )

                            true -> CircularBarSpinner()
                        }
                    }
                    IconButton(onClick = { next() }) {
                        Icon(
                            painterResource(Res.drawable.next),
                            modifier = Modifier.size(15.dp),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = { showBottomSheet = true }) {
                    Icon(
                        painterResource(Res.drawable.pmenu),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    SelectPlaylist(
        show = showSelectPlaylist,
        onCancel = { showSelectPlaylist = false },
        onConfirm = {
            onSelectFolder?.invoke(it)
            showSelectPlaylist = false
        }
    )

    PlayerMenuBottomSheet(
        show = showBottomSheet,
        onDismissRequest = {
            showBottomSheet = false
        }
    )
}


@Composable
internal fun PlayTimer(time: Long) {
    Text(
        formatDuration(time),
        modifier = Modifier.width(40.dp),
        textAlign = TextAlign.Center,
        color = Color.White,
        fontSize = 14.sp
    )
}

@Composable
internal fun ToolbarBtn(
    icon: DrawableResource,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            painterResource(icon),
            modifier = Modifier.size(20.dp),
            contentDescription = null,
            tint = tint
        )
    }
}