package wzy.hkta.rhythbox.expect

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.google.protobuf.LazyStringArrayList.emptyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.services.PlaybackService
import wzy.hkta.rhythbox.utils.NetEaseCloudMusic
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SnackbarManager
import wzy.hkta.rhythbox.utils.StorageManager
import wzy.hkta.rhythbox.utils.generateMediaId
import kotlin.collections.emptyList
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
actual class RhythboxPlayer(private val context: Context, val database: AppDatabase) {
    private var exoPlayerSession: MediaController? = null
    private var job: Job? = null
    private var isInitRestorePlaylist = false
    private val _mediaItems = MutableStateFlow<List<SongEntity>>(mutableListOf())
    private var _mediaItems2 = listOf<MediaItem>()

    private val _playMode = MutableStateFlow(PlayMode.LIST_LOOP)
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    private val _lyricStatus = MutableStateFlow<LyricStatus>(LyricStatus.Init)
    private val _currentPosition = MutableStateFlow<Long>(0)
    private val _isPlaying = MutableStateFlow(false)
    private val _duration = MutableStateFlow<Long>(0)
    private val _progress = MutableStateFlow(0f)
    private val _playIndex = MutableStateFlow(-1)
    private var _playKey = MutableStateFlow<String?>(null)
    private val _playUuid = MutableStateFlow<String?>(null)

    actual val isPlaying = _isPlaying.asStateFlow()
    actual val currentPosition = _currentPosition.asStateFlow()
    actual val duration = _duration.asStateFlow()
    actual val mediaItems = _mediaItems.asStateFlow()
    actual val playIndex = _playIndex.asStateFlow()
    actual val progress = _progress.asStateFlow()
    actual val playMode = _playMode.asStateFlow()
    actual val playKey = _playKey.asStateFlow()
    actual val playUuid = _playUuid.asStateFlow()

    actual val currentSong = _currentSong.asStateFlow()
    actual val lyricStatus = _lyricStatus.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (isPlaying.value) {
                    _duration.value = exoPlayerSession?.duration ?: 0
                    _currentPosition.value = exoPlayerSession?.currentPosition ?: 0
                    val p = _currentPosition.value.toFloat() / _duration.value.toFloat()
                    _progress.value = if (p.isNaN()) 0f else p
                    StorageManager.setItem("currentPosition", _currentPosition.value)
                    delay(250)
                } else {
                    delay(500)
                }
            }
        }
        initMediaController()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initMediaController() {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            exoPlayerSession = controllerFuture.get()
            exoPlayerSession?.repeatMode = REPEAT_MODE_OFF
            exoPlayerSession?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int
                ) {
                    when (reason) {
                        Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                            mediaItem?.let {
                                val index = _mediaItems2.indexOf(it)
                                _playIndex.value = index
                                _currentSong.value = mediaItems.value[index]
                                savePlayerInfo()
                            }
                        }

                        else -> {}
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY && !isInitRestorePlaylist) {
                        seekTo(currentPosition.value)
                        isInitRestorePlaylist = true
                    }

                    if (playbackState == Player.STATE_ENDED) {
                        CoroutineScope(Dispatchers.IO).launch {
                            currentSong.value?.id?.let { songId ->
                                Rhythbox.dataBase.getSongDao().incrementPlayCount(songId)
                            }
                        }
                        when (playMode.value) {
                            PlayMode.SINGLE_LOOP -> {
                                exoPlayerSession?.seekTo(0)
                                exoPlayerSession?.prepare()
                                exoPlayerSession?.play()
                            }

                            PlayMode.LIST -> {
                                // 如果不是最后一首，正常切下一首
                                if (playIndex.value < mediaItems.value.size - 1) {
                                    next()
                                } else {
                                    // 已经是最后一首了，列表播放结束，停止播放
                                    pause()
                                    seekTo(0)
                                }
                            }

                            else -> next()
                        }
                    }
                }
            })
            restorePlaylist()
        }, MoreExecutors.directExecutor())
    }

    actual fun resetMediaItems(key: String, items: List<SongEntity>, refresh: Boolean?) {
        if (_playKey.value == key && items.size == mediaItems.value.size && refresh != true) return
        _playKey.value = key
        exoPlayerSession?.clearMediaItems()
        _mediaItems2 = items.map { buildMediaItems(it) }
        exoPlayerSession?.addMediaItems(_mediaItems2)
        _mediaItems.value = items
        StorageManager.setItem("mediaItems", items.map { it.id })
        StorageManager.setItem<String>("playKey", key)
    }

    actual fun updateMediaItem(item: SongEntity) {
        _mediaItems.value = mediaItems.value.map {
            if (it.id == item.id) item else it
        }
        if (currentSong.value?.id == item.id) {
            _currentSong.value = item
        }
        StorageManager.setItem("mediaItems", _mediaItems.value.map { it.id })
    }

    actual fun restorePlaylist() {
        val ids = StorageManager.getItem<List<Long>>("mediaItems")
        val key = StorageManager.getItem<String>("playKey")
        val playerInfo = StorageManager.getItem<PlayerInfo>("PlayerInfo")
        val currentPosition = StorageManager.getItem<Long>("currentPosition") ?: 0L
        CoroutineScope(Dispatchers.IO).launch {
            if (!ids.isNullOrEmpty() && key != null) {
                val data = database.getSongDao().getByIds(ids)
                // 2. 内存里的数据组装，这里用 map 重新生成 UUID 列表
                val newData = data.map { it.copy(uuid = generateMediaId(key, it)) }
                // 3. 🚨 关键：捞完数据了，立刻切回主线程去调播放器和更新 UI 状态！
                withContext(Dispatchers.Main) {
                    // 现在的环境是 Main 线程了，ExoPlayer 绝对不会再抗议
                    resetMediaItems(key, newData, true)
                    if (playerInfo != null) {
                        _duration.value = playerInfo.duration
                        _playIndex.value = playerInfo.playIndex
                        _playUuid.value = playerInfo.playUuid
                        _playMode.value = playerInfo.playMode
                        prepare(_playIndex.value)
                        _currentPosition.value = currentPosition
                        getSongLyric()
                    }
                }
            }
        }
    }

    actual fun addSong(songItem: SongEntity) {
        if (!mediaItems.value.any { it.id == songItem.id }) {
            _mediaItems.value = mediaItems.value + songItem
        }
        prepare(_mediaItems.value.size - 1)
        play()
    }

    actual fun delSong(songEntity: SongEntity) {
        val isCurrent = currentSong.value?.id == songEntity.id
        val newList = _mediaItems.value.filter { it.id != songEntity.id }
        _mediaItems.value = newList
        StorageManager.setItem("mediaItems", mediaItems.value.map { it.id })
        if (isCurrent && isPlaying.value) {
            next()
        }
    }

    actual fun getSongLyric() {
        currentSong.value?.let {
            _lyricStatus.value = LyricStatus.Loading
            job?.cancel()
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    delay(300)
                    val info = NetEaseCloudMusic.getLyric(it)
                    _lyricStatus.value = info
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        _lyricStatus.value = LyricStatus.LoadErr("加载失败 ${e.message}")
                    }
                }
            }
        }
    }


    actual fun onChangePlayMode() {
        _playMode.value = when (playMode.value) {
            PlayMode.LIST_LOOP -> {
                SnackbarManager.show("单曲循环")
                PlayMode.SINGLE_LOOP
            }

            PlayMode.SINGLE_LOOP -> {
                SnackbarManager.show("列表播放")
                PlayMode.LIST
            }

            PlayMode.LIST -> {
                SnackbarManager.show("随机播放")
                PlayMode.SHUFFLE
            }

            PlayMode.SHUFFLE -> {
                SnackbarManager.show("列表循环")
                PlayMode.LIST_LOOP
            }
        }
        savePlayerInfo()
    }

    actual fun prepare(mediaItemIndex: Int) {
        _playIndex.value = mediaItemIndex
        mediaItems.value.getOrNull(mediaItemIndex)?.let {
            _currentSong.value = it
            _playUuid.value = it.uuid
            exoPlayerSession?.seekTo(mediaItemIndex, 0)
            exoPlayerSession?.prepare()
            savePlayerInfo()
        }
    }

    private fun buildMediaItems(it: SongEntity): MediaItem {
        return MediaItem.Builder()
            .setMediaId(it.path)
            .setUri(it.path)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(it.artist)
                    .setTitle(it.name)
                    .setArtworkUri((it.cover ?: "").toUri())
                    .build()
            )
            .build()
    }

    fun savePlayerInfo() {
        StorageManager.setItem(
            "PlayerInfo", PlayerInfo(
                playMode = _playMode.value,
                duration = _duration.value,
                playIndex = _playIndex.value,
                playUuid = _playUuid.value
            )
        )
    }

    actual fun seekTo(position: Long) {
        exoPlayerSession?.seekTo(position)
        val progress = position.toFloat() / _duration.value.toFloat()
        if (progress.isNaN()) _progress.value = 0f
        else _progress.value = progress
    }

    actual fun play() {
        if (mediaItems.value.isEmpty()) return
        exoPlayerSession?.play()
    }

    actual fun togglePlay() {
        if (mediaItems.value.isEmpty()) return
        when (isPlaying.value) {
            true -> exoPlayerSession?.pause()
            false -> exoPlayerSession?.play()
        }
    }

    fun randomPlayIndex(): Int {
        return Random.nextInt(mediaItems.value.size)
    }

    actual fun next() {
        if (mediaItems.value.isEmpty()) return
        val index = if (playMode.value == PlayMode.SHUFFLE) {
            randomPlayIndex()
        } else {
            (_playIndex.value + 1) % mediaItems.value.size
        }
        prepare(index)
        if (!isPlaying.value) play()
    }

    actual fun prev() {
        if (mediaItems.value.isEmpty()) return
        val index = if (playMode.value == PlayMode.SHUFFLE) {
            randomPlayIndex()
        } else {
            (_playIndex.value - 1 + mediaItems.value.size) % mediaItems.value.size
        }
        prepare(index)
        if (!isPlaying.value) play()
    }

    actual fun setRhythboxRingtone() {
        if (!Settings.System.canWrite(context)) {
            Toast.makeText(
                context,
                "乐动盒需要系统设置修改权限，请在随后打开的页面中开启",
                Toast.LENGTH_LONG
            ).show()
            // 引导用户去系统权限开关页
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }
        try {
            // ⚡️ 核心高光时刻：调用 RingtoneManager 直接把 Uri 焊进系统铃声插槽
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE, // TYPE_RINGTONE 代表来电铃声（如果是通知音可以用 TYPE_NOTIFICATION）
                currentSong.value!!.path.toUri()
            )
            Toast.makeText(context, "🔔 成功设为系统来电铃声！", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "铃声设置失败: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    actual fun pause() {
    }

    actual fun release() {
    }
}