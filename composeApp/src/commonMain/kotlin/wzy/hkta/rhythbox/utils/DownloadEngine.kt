package wzy.hkta.rhythbox.utils

import com.linroid.ketch.api.Destination
import com.linroid.ketch.api.DownloadRequest
import com.linroid.ketch.api.DownloadState
import com.linroid.ketch.api.DownloadTask
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.engine.KtorHttpEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

object DownloadEngine {


    private var _ketch: Ketch? = null

    val ketch get() = _ketch ?: error("DownloadEngine 尚未初始化")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTasks(): Flow<List<Pair<DownloadTask, DownloadState>>> =
        tasks.flatMapLatest { list ->
            if (list.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(list.map { it.state }) { states ->
                    list.zip(states.toList())
                }
            }
        }

    /**
     * 所有下载任务
     */
    val tasks: StateFlow<List<DownloadTask>> get() = ketch.tasks


    fun initEngine(engine: Ketch) {
        _ketch = engine
    }

    /**
     * 启动下载引擎
     */
    suspend fun start() {
        ketch.start()
    }

    /**
     * 添加下载任务
     *
     * @param url 下载地址
     * @param destination 文件保存路径
     */
    suspend fun addTask(
        url: String,
        destination: String
    ): DownloadTask {
        return ketch.download(
            DownloadRequest(
                url = url,
                destination = Destination(destination)
            )
        )
    }

    /**
     * 获取任务
     */
    fun getTask(taskId: String): DownloadTask? {
        return tasks.value.firstOrNull {
            it.taskId == taskId
        }
    }

    /**
     * 监听指定任务状态
     */
    fun observeTask(taskId: String) = tasks
        .map { list -> list.firstOrNull { it.taskId == taskId } }
        .map { it?.state }

    /**
     * 暂停任务
     */
    suspend fun pause(taskId: String) {
        getTask(taskId)?.pause()
    }

    /**
     * 恢复任务
     */
    suspend fun resume(taskId: String) {
        getTask(taskId)?.resume()
    }

    /**
     * 取消任务
     */
    suspend fun cancel(taskId: String) {
        getTask(taskId)?.cancel()
    }

    /**
     * 删除任务
     *
     * deleteFiles 是否同时删除已下载或未完成的文件
     */
    suspend fun remove(
        taskId: String,
        deleteFiles: Boolean = false
    ) {
        getTask(taskId)?.remove(deleteFiles)
    }

    /**
     * 关闭引擎
     */
    fun close() {
        ketch.close()
    }
}
