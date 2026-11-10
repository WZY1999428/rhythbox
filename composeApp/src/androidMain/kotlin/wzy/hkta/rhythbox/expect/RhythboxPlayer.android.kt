package wzy.hkta.rhythbox.expect

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.database.LyricEntity
import wzy.hkta.rhythbox.database.SongCache
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.LyricData
import wzy.hkta.rhythbox.services.PlaybackService
import wzy.hkta.rhythbox.utils.LyricInfo
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.StorageManager
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.utils.netEaseCloudMusic.NetEaseCloudMusic
import wzy.hkta.rhythbox.utils.parseLyric
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

actual class RhythboxPlayer(private val context: Context, val database: AppDatabase) {
    private var exoPlayerSession: MediaController? = null
    private var job: Job? = null
    private var isInitRestorePlaylist = false
    private val songDao = database.getSongDao()
    private val _mediaItems = MutableStateFlow(mutableListOf<SongType>())
    private var _mediaItems2 = listOf<MediaItem>()
    private var errorToast: Toast? = null
    private val _playMode = MutableStateFlow(PlayMode.LIST_LOOP)
    private val _playerLoading = MutableStateFlow(false)
    private val _currentSong = MutableStateFlow<SongType?>(null)
    private val _lyricStatus = MutableStateFlow<LyricStatus>(LyricStatus.Init)
    private val _lyricList = MutableStateFlow<List<LyricData>>(emptyList())
    private val _currentPosition = MutableStateFlow<Long>(0)
    private val _isPlaying = MutableStateFlow(false)
    private val _duration = MutableStateFlow<Long>(0)
    private val _progress = MutableStateFlow(0f)
    private val _playIndex = MutableStateFlow(-1)
    private var _playKey = MutableStateFlow<String?>(null)
    private val _playMediaId = MutableStateFlow<String?>(null)
    actual val lyricList = _lyricList.asStateFlow()
    actual val playerLoading = _playerLoading.asStateFlow()
    actual val isPlaying = _isPlaying.asStateFlow()
    actual val currentPosition = _currentPosition.asStateFlow()
    actual val duration = _duration.asStateFlow()
    actual val mediaItems = _mediaItems.asStateFlow()
    actual val playIndex = _playIndex.asStateFlow()
    actual val progress = _progress.asStateFlow()
    actual val playMode = _playMode.asStateFlow()
    actual val playKey = _playKey.asStateFlow()
    actual val playMediaId = _playMediaId.asStateFlow()


    actual val currentSong = _currentSong.asStateFlow()
    actual val lyricStatus = _lyricStatus.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (isInitRestorePlaylist) {
                    _duration.value = exoPlayerSession?.duration ?: 0
                    _currentPosition.value = exoPlayerSession?.currentPosition ?: 0
                    val p = _currentPosition.value.toFloat() / _duration.value.toFloat()
                    _progress.value = if (p.isNaN()) 0f else p
                    if (isPlaying.value) {
                        StorageManager.setItem("currentPosition", _currentPosition.value)
                    }
                }
                delay(250.milliseconds)
            }
        }
        initMediaController()
    }

    fun initMediaController() {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            exoPlayerSession = controllerFuture.get()
            exoPlayerSession?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    super.onIsLoadingChanged(isLoading)
                    _playerLoading.value = isLoading
                }

                override fun onPlayerError(error: PlaybackException) {

                    super.onPlayerError(error)
                    errorToast?.cancel()
                    errorToast =
                        Toast.makeText(context, "播放失败: ${error.message}", Toast.LENGTH_LONG)
                    errorToast?.show()
                    if (currentSong.value is SongType.Network) {
                        val song = (currentSong.value as SongType.Network).song
                        CoroutineScope(Dispatchers.IO).launch {
                            song.cache?.let { songDao.deleteSongCache(it) }
                        }
                    }
                    next()
                }

                override fun onMediaItemTransition(
                    mediaItem: MediaItem?, reason: Int
                ) {
                    when (reason) {
                        Player.MEDIA_ITEM_TRANSITION_REASON_SEEK, Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                            // 直接从播放器获取当前准确的媒体项索引，避免用 indexOf 查错
                            val index = exoPlayerSession?.currentMediaItemIndex ?: return
                            if (index >= 0) {
                                _playIndex.value = index
                                setNextInfo(index)
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
                }
            })
            restorePlaylist()
        }, MoreExecutors.directExecutor())
    }

    private fun setNextInfo(index: Int) {

        mediaItems.value.getOrNull(index)?.let { song ->
            _currentSong.value = song
            _playMediaId.value = song.mediaId
            if (song is SongType.Network) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val url = song.url
                        if (songDao.getSongCacheByUrl(url) == null) {
                            downloadMusicToCache(context, url, song.mediaId)?.let { file ->
                                songDao.insertSongCache(SongCache(url, file.path))
                                withContext(Dispatchers.Main) {
                                    val player = exoPlayerSession ?: return@withContext
                                    val currentIndex = player.currentMediaItemIndex
                                    if (currentIndex == -1) return@withContext
                                    val oldMediaItem = player.getMediaItemAt(currentIndex)
                                    if (oldMediaItem.mediaId == url || oldMediaItem.localConfiguration?.uri.toString() == url) {
                                        val newMediaItem =
                                            oldMediaItem.buildUpon().setMediaId(file.path)
                                                .setUri(file.path).build()
                                        val currentPosition = player.currentPosition
                                        // 3. 完美无缝替换并恢复进度
                                        player.replaceMediaItem(currentIndex, newMediaItem)
                                        player.seekTo(currentIndex, currentPosition)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("error $e")
                    }
                }
            }
        }
    }

    actual fun resetMediaItems(
        key: String, songItems: List<SongType>, refresh: Boolean?, cb: (() -> Unit)?
    ) {
        if (_playKey.value == key && songItems.size == mediaItems.value.size && refresh != true) {
            cb?.invoke()
            return
        }
        exoPlayerSession?.setMediaItems(_mediaItems2, true)
        CoroutineScope(Dispatchers.IO).launch {
            val cacheList = songDao.getSongCache()
            _mediaItems2 = songItems.map { buildMediaItems(it, cacheList) }
            withContext(Dispatchers.Main) {
                exoPlayerSession?.setMediaItems(_mediaItems2)
                cb?.invoke()
            }
        }
        _playKey.value = key
        _mediaItems.value = songItems.toMutableList()
        saveMediaItems()
    }

    actual fun sortMediaItems(songItems: List<SongType>) {
        val player = exoPlayerSession ?: return
        if (player.mediaItemCount <= 0 || songItems.size != player.mediaItemCount) return
        // 目标排序 ID
        val sortedIds = songItems.map { it.mediaId }
        val currentMediaId = player.currentMediaItem?.mediaId
        // 确保排序前后是同一批歌曲
        val mediaItemMap = mutableMapOf<String, MediaItem>()
        for (i in 0 until player.mediaItemCount) {
            val item = player.getMediaItemAt(i)
            mediaItemMap[item.mediaId] = item
        }
        val reorderedMediaItems = sortedIds.mapNotNull { mediaItemMap[it] }
        //播放时会停顿
        if (reorderedMediaItems.isNotEmpty()) {
            // 找到排序后，原本正在播放的那首歌现在跑哪去了！
            val newCurrentIndex = if (currentMediaId != null) {
                reorderedMediaItems.indexOfFirst { it.mediaId == currentMediaId }.coerceAtLeast(0)
            } else {
                player.currentMediaItemIndex
            }
            val currentPosition = player.currentPosition
            player.setMediaItems(reorderedMediaItems, newCurrentIndex, currentPosition)
            _playIndex.value = newCurrentIndex
            player.prepare()
        }

        // 同步状态
        _mediaItems.value = songItems.toMutableList()
        saveMediaItems()
    }

    actual fun updateMediaItem(song: SongType) {
        val index = _mediaItems.value.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            _mediaItems.value[index] = song
        }
        _currentSong.value = song
        saveMediaItems()
    }

    private fun saveMediaItems() {
        StorageManager.setItem<List<SongType>>("mediaItems", mediaItems.value)
    }

    actual fun restorePlaylist() {
        val key = StorageManager.getItem<String>("playKey")
        val playerInfo = StorageManager.getItem<PlayerInfo>("PlayerInfo")
        CoroutineScope(Dispatchers.IO).launch {
            if (playerInfo != null) {
                val newData = StorageManager.getItem<List<SongType>>("mediaItems")
                if (newData != null && key != null) {
                    withContext(Dispatchers.Main) {
                        resetMediaItems(key, newData, true)
                        initPlayInfo(playerInfo)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    initPlayInfo(PlayerInfo())
                }
            }
        }
    }

    private fun initPlayInfo(playerInfo: PlayerInfo) {
        val currentPosition = StorageManager.getItem<Long>("currentPosition") ?: 0L
        _duration.value = playerInfo.duration
        _playIndex.value = playerInfo.playIndex
        _playMediaId.value = playerInfo.playMediaId
        _currentPosition.value = currentPosition
        _playMode.value = playerInfo.playMode
        setPlayerRepeatMode()
        _mediaItems.value.getOrNull(playIndex.value)?.let { song ->
            _currentSong.value = song
            exoPlayerSession?.prepare()
            exoPlayerSession?.seekTo(currentPosition)
            getSongLyric()
        }
    }

    actual fun addSong(song: SongType) {
        if (_mediaItems.value.none { it.mediaId == song.mediaId }) {
            _mediaItems.value += song
            CoroutineScope(Dispatchers.IO).launch {
                val cacheList = songDao.getSongCache()
                val mediaItem = buildMediaItems(song, cacheList)
                withContext(Dispatchers.Main) {
                    exoPlayerSession?.addMediaItem(mediaItem)
                    prepare(_mediaItems.value.size - 1)
                    play()
                }
            }
        } else {
            val index = _mediaItems.value.indexOfLast { it.mediaId == song.mediaId }
            prepare(index)
            play()
        }
    }


    actual fun delSong(song: SongType) {
        val mediaId = when (song) {
            is SongType.Local -> song.song.mediaId
            is SongType.Network -> song.song.mediaId
        }
        val index = _mediaItems.value.indexOfLast { it.mediaId == mediaId }
        if (index == -1) return
        val isCurrent = when (val cur = currentSong.value) {
            is SongType.Local -> cur.song.mediaId == mediaId
            is SongType.Network -> cur.song.mediaId == mediaId
            null -> false
        }
        exoPlayerSession?.removeMediaItem(index)
        _mediaItems.value.removeAll { it.mediaId != mediaId }
        saveMediaItems()
        if (isCurrent && isPlaying.value) {
            next()
        }
    }

    actual fun getSongLyric() {
        currentSong.value?.let {
            _lyricStatus.value = LyricStatus.Loading
            _lyricList.value = emptyList()
            job?.cancel()
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    delay(300.milliseconds)
                    when (it) {
                        is SongType.Local -> {
                            val info = NetEaseCloudMusic.getLyric(it.song)
                            _lyricStatus.value = info
                        }

                        is SongType.Network -> {
                            val lyricData = songDao.getLyric("online-${it.song.id}")
                            var lyricInfo: LyricInfo? = null
                            if (lyricData != null) {
                                lyricInfo = LyricInfo(
                                    parseLyric(lyricData.lyric)!!, null, lyricData.offset
                                )
                            }
                            val lyric = FetchMusic.music.getSongLyric(it.song.id)
                            _lyricList.value = lyric
                            if (lyric.isNotEmpty() && lyricData == null) {
                                val content = lyric[0].content
                                lyricInfo = LyricInfo(parseLyric(content)!!, null, 0)
                                val lyricEntity =
                                    LyricEntity(songId = "online-${it.song.id}", lyric = content);
                                songDao.insertLyric(lyricEntity)
                            } else {
                                _lyricStatus.value = LyricStatus.NotFound
                            }
                            if (lyricInfo != null) {
                                _lyricStatus.value = LyricStatus.Finished(lyricInfo)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        _lyricStatus.value = LyricStatus.LoadErr("加载失败 ${e.message}")
                    }
                }
            }
        }
    }


    private fun setPlayerRepeatMode() {
        val player = exoPlayerSession ?: return
        player.shuffleModeEnabled = false
        when (playMode.value) {
            PlayMode.LIST_LOOP -> {
                player.repeatMode = REPEAT_MODE_ALL
            }

            PlayMode.SINGLE_LOOP -> {
                player.repeatMode = REPEAT_MODE_ONE
            }

            PlayMode.LIST -> {
                player.repeatMode = REPEAT_MODE_OFF
            }

            PlayMode.SHUFFLE -> {
                player.shuffleModeEnabled = true
                player.repeatMode = REPEAT_MODE_ALL
            }
        }
    }

    actual fun onChangePlayMode() {
        _playMode.value = when (playMode.value) {
            PlayMode.LIST_LOOP -> {
                ToastManager.show("单曲循环")
                PlayMode.SINGLE_LOOP
            }

            PlayMode.SINGLE_LOOP -> {
                ToastManager.show("列表播放")
                PlayMode.LIST
            }

            PlayMode.LIST -> {
                ToastManager.show("随机播放")
                PlayMode.SHUFFLE
            }

            PlayMode.SHUFFLE -> {
                ToastManager.show("列表循环")
                PlayMode.LIST_LOOP
            }
        }
        setPlayerRepeatMode()
        savePlayerInfo()
    }

    actual fun prepare(mediaItemIndex: Int) {
        if (mediaItemIndex < 0) return
        _playIndex.value = mediaItemIndex
        mediaItems.value.getOrNull(mediaItemIndex)?.let {
            _currentSong.value = it
            _playMediaId.value = it.mediaId
        }
        exoPlayerSession?.seekTo(mediaItemIndex, 0)
        exoPlayerSession?.prepare()
        savePlayerInfo()
    }

    private fun buildMediaItems(song: SongType, cacheList: List<SongCache>): MediaItem {
        val cache = cacheList.find { it.url == song.url }
        val url = when (cache) {
            null -> song.url
            else -> cache.filePath
        }
        return MediaItem.Builder().setMediaId(song.mediaId).setUri(url).setMediaMetadata(
            MediaMetadata.Builder().setArtist(song.artist).setTitle(song.name)
                .setArtworkUri((song.cover ?: "").toUri()).build()
        ).build()
    }

    fun savePlayerInfo() {
        playKey.value?.let { key -> StorageManager.setItem("playKey", key) }
        StorageManager.setItem(
            "PlayerInfo", PlayerInfo(
                playMode = _playMode.value,
                duration = _duration.value,
                playIndex = _playIndex.value,
                playMediaId = _playMediaId.value,
                playSongType = when (currentSong.value) {
                    is SongType.Local, null -> "Local"
                    is SongType.Network -> "Network"
                }
            )
        )
    }

    actual fun seekTo(position: Long) {
        exoPlayerSession?.seekTo(position)
        val progress = position.toFloat() / _duration.value.toFloat()
        if (progress.isNaN()) _progress.value = 0f
        else _progress.value = progress
    }

    private fun mediaItemsIsEmpty(): Boolean {
        return mediaItems.value.isEmpty()
    }

    actual fun play() {
        if (mediaItemsIsEmpty()) return
        exoPlayerSession?.play()
    }


    actual fun shufflePlay() {
        val player = exoPlayerSession ?: return
        if (mediaItemsIsEmpty()) return
        _playMode.value = PlayMode.SHUFFLE
        player.shuffleModeEnabled = true
        player.repeatMode = REPEAT_MODE_ALL
        next()
    }

    actual fun togglePlay() {
        if (mediaItemsIsEmpty()) return
        when (isPlaying.value) {
            true -> exoPlayerSession?.pause()
            false -> exoPlayerSession?.play()
        }
    }

    fun randomPlayIndex(): Int {
        return Random.nextInt(mediaItems.value.size)
    }

    actual fun next() {
        if (mediaItemsIsEmpty()) return
        val index = if (playMode.value == PlayMode.SHUFFLE) {
            randomPlayIndex()
        } else {
            (_playIndex.value + 1) % mediaItems.value.size
        }
        prepare(index)
        if (!isPlaying.value) play()
    }

    actual fun prev() {
        if (mediaItemsIsEmpty()) return
        val index = if (playMode.value == PlayMode.SHUFFLE) {
            randomPlayIndex()
        } else {
            (_playIndex.value - 1) % mediaItems.value.size
        }
        prepare(index)
        if (!isPlaying.value) play()
    }

    actual fun setRhythboxRingtone() {
        if (!Settings.System.canWrite(context)) {
            Toast.makeText(
                context, "乐动盒需要系统设置修改权限，请在随后打开的页面中开启", Toast.LENGTH_LONG
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
            val url = when (val cur = currentSong.value) {
                is SongType.Local -> cur.song.path
                is SongType.Network -> cur.song.cache
                null -> return
            }
            if (url == null) return
            RingtoneManager.setActualDefaultRingtoneUri(
                context, RingtoneManager.TYPE_RINGTONE, url.toUri()
            )
            Toast.makeText(context, "🔔 成功设为系统来电铃声！", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "铃声设置失败: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    actual fun pause() {
        exoPlayerSession?.pause()
    }

    actual fun release() {
    }

}