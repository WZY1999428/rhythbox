package wzy.hkta.rhythbox.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.folder_pink
import rhythbox.composeapp.generated.resources.right
import rhythbox.composeapp.generated.resources.scanning
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.RhythboxSongEmptyState
import wzy.hkta.rhythbox.cssToColor
import wzy.hkta.rhythbox.screens.song.SongScreen
import wzy.hkta.rhythbox.utils.Rhythbox

class FolderScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberScreenModel { FolderViewModel() }
        val dirs = vm.dirs.collectAsState()
        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(title = "文件夹") {
                IconButton(
                    onClick = { navigator.push(ScanMusicScreen()) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.scanning),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (dirs.value.isEmpty()) {
                RhythboxSongEmptyState(
                    describe = "还没有扫描文件夹快去扫描吧",
                    buttonText = "立即扫描",
                    onButtonClick = {
                        navigator.push(ScanMusicScreen())
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(dirs.value.size) {
                        val dir = dirs.value[it]
                        Row(
                            modifier = Modifier.clip(
                                RoundedCornerShape(3.dp)
                            ).clickable {
                                navigator.push(
                                    SongScreen(
                                        "FolderScreen${dir.fullPath}",
                                        dir.name,
                                        dir.fullPath
                                    )
                                )
                            }.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(Res.drawable.folder_pink),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
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
                                        dir.name,
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        dir.fullPath,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        color = "#666666".cssToColor(),
                                        overflow = TextOverflow.StartEllipsis
                                    )
                                }
                                Icon(
                                    painterResource(Res.drawable.right),
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }


        }
    }
}


private class FolderViewModel : ScreenModel {
    val dirDao = Rhythbox.dataBase.getDirDao()
    val dirs = dirDao.find().stateIn(
        started = SharingStarted.WhileSubscribed(5000),
        scope = screenModelScope,
        initialValue = emptyList()
    )

}