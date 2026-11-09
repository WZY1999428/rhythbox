package wzy.hkta.rhythbox.screens.download

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linroid.ketch.api.DownloadState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.close_grey
import rhythbox.composeapp.generated.resources.pause2
import rhythbox.composeapp.generated.resources.play3
import wzy.hkta.rhythbox.components.MAlertDialog
import wzy.hkta.rhythbox.utils.DownloadEngine

@Composable
fun DownloadContent(vm: DownloadViewModel) {

    val downloads by vm.downloadList.collectAsState()
    val states = downloads.map { task ->
        val state by task.state.collectAsState()
        state
    }

    var showDelete by remember { mutableStateOf(false) }
    var selectId by remember { mutableStateOf<String?>(null) }

    var showClear by remember { mutableStateOf(false) }
    val isAllPaused = states.isNotEmpty() && states.all { it is DownloadState.Paused }
    val scope = rememberCoroutineScope()
    LaunchedEffect(downloads.size) {
        vm.getDownloadEntity()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 列表头部计数信息
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "正在下载 ${downloads.size}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (downloads.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            scope.launch {
                                if (isAllPaused) {
                                    DownloadEngine.resumeAll()
                                } else {
                                    DownloadEngine.resumeAll()
                                }
                            }
                        }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(if (isAllPaused) Res.drawable.play3 else Res.drawable.pause2),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isAllPaused) "全部继续" else "全部暂停",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 清空按钮
                    Row(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            showClear = true
                        }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close_grey),
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "清空",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


        }

        // 列表主体
        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无下载任务",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(downloads, key = { it.taskId }) { item ->
                    SimpleDownloadItem(vm = vm, item = item, onTogglePause = {
                        scope.launch {
                            when (item.state) {
                                DownloadState.Downloading -> DownloadEngine.pause(item.taskId)
                                DownloadState.Paused -> DownloadEngine.resume(item.taskId)
                                DownloadState.Failed -> DownloadEngine.resume(item.taskId)
                                else -> {}
                            }
                        }
                    }, onDelete = {
                        selectId = item.taskId
                        showDelete = true
                    })
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }


        MAlertDialog(
            show = showDelete,
            title = "提示",
            onCancel = { showDelete = false },
            onConfirm = {
                scope.launch {
                    val task = DownloadEngine.getTask(selectId!!)
                    task?.let {
                        val file = it.outputPath
                        DownloadEngine.remove(selectId!!)
                        vm.downloadDao.deleteOnFile(file!!)
                    }

                    showDelete = false
                }
            }) { Text("确定要删除当前任务吗") }


        MAlertDialog(
            show = showClear,
            title = "提示",
            onCancel = { showClear = false },
            onConfirm = {
                scope.launch {
                    val outputPaths = DownloadEngine.tasks.value.mapNotNull { it.outputPath }
                    vm.downloadDao.deleteOnInFile(outputPaths)
                    vm.clear()
                    showClear = false
                }
            }) {
            Text("确定要清空下载任务吗")
        }


    }

}