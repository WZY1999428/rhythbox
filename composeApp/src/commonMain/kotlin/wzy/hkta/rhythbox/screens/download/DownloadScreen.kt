package wzy.hkta.rhythbox.screens.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.utils.DownloadEngine
import com.linroid.ketch.api.DownloadState

class DownloadScreen : Screen {

    @Composable
    override fun Content() {
        var selectedTab by remember { mutableIntStateOf(0) }

        val tasks by DownloadEngine.observeTasks()
            .collectAsState(initial = emptyList())

        val inProgressTasks = tasks.filter {
            it.second !is DownloadState.Completed &&
                    it.second !is DownloadState.Canceled
        }

        val completedTasks = tasks.filter {
            it.second is DownloadState.Completed
        }

        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(title = "下载中心")

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text("下载中 (${inProgressTasks.size})")
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text("已完成 (${completedTasks.size})")
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> InProgress(inProgressTasks)
                    1 -> Completed(completedTasks)
                }
            }
        }
    }
}