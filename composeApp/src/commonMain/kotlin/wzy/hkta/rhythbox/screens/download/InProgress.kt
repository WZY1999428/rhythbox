package wzy.hkta.rhythbox.screens.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linroid.ketch.api.DownloadState
import com.linroid.ketch.api.DownloadTask

@Composable
fun InProgress(
    tasks: List<Pair<DownloadTask, DownloadState>>
) {
    if (tasks.isEmpty()) {
        EmptyDownload("暂无下载任务")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = tasks,
            key = { it.first.taskId }
        ) { (task, state) ->
            DownloadTaskItem(
                task = task,
                state = state,
                completed = false
            )
        }
    }
}