package wzy.hkta.rhythbox.utils

import com.linroid.ketch.api.Destination
import com.linroid.ketch.api.DownloadRequest
import com.linroid.ketch.api.DownloadState
import com.linroid.ketch.api.DownloadTask
import com.linroid.ketch.core.Ketch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import wzy.hkta.rhythbox.database.DownloadSongEntity
import wzy.hkta.rhythbox.database.dao.DownloadDao
import wzy.hkta.rhythbox.fatchMusic.MUSIC_BASE_URL
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.fatchMusic.SongAudioItem
import kotlin.String

enum class AddTaskState {
    Success,
    Exists,
}

object DownloadEngine {

    private var _downloadDao: DownloadDao? = null
    private var _ketch: Ketch? = null
    private var savePath = ""

    val ketch get() = _ketch ?: error("DownloadEngine 尚未初始化")
    val downloadDao get() = _downloadDao ?: error("_downloadDao 尚未初始化")

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


    fun initEngine(engine: Ketch, path: String) {
        _ketch = engine
        savePath = path
        _downloadDao = Rhythbox.dataBase.getDownloadDao()
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
     * @param wzy.hkta.rhythbox.utils.fatchMusic.Song 下载地址
     */
    suspend fun addTask(
        song: Song,
        songAudioItem: SongAudioItem,
        overwrite: Boolean = false
    ): AddTaskState {
//        val exists = downloadDao.exists(song.id)
//        if (exists && !overwrite) {
//            return AddTaskState.Exists
//        }
        val filePath = "$savePath/${songAudioItem.originalName}"
        val url ="$MUSIC_BASE_URL/${songAudioItem.url}"
        println("filePathfilePath $filePath")
        downloadDao.upsert(
            DownloadSongEntity(
                songId = song.id,
                url = url,
                filePath = filePath,
                title = song.title,
                duration = song.duration,
                isLike = song.isLike,
                mvUrl = song.mvUrl,
                cover = song.cover,
                mediaId = song.mediaId,
                originalName = songAudioItem.originalName,
                filename = songAudioItem.filename,
                artists = song.artists,
            )
        )

        ketch.download(
            DownloadRequest(
                url = url,
                destination = Destination(filePath)
            )
        )
        return AddTaskState.Success
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
