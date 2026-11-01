package wzy.hkta.rhythbox.expect

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.database.SongCache
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.services.PlaybackService
import wzy.hkta.rhythbox.utils.LyricInfo
import wzy.hkta.rhythbox.utils.netEaseCloudMusic.NetEaseCloudMusic
import wzy.hkta.rhythbox.utils.SnackbarManager
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.StorageManager
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.LyricData
import wzy.hkta.rhythbox.utils.fatchMusic.Song
import wzy.hkta.rhythbox.utils.generateMediaId
import wzy.hkta.rhythbox.utils.parseLyric
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

actual class RhythboxPlayer(private val context: Context, val database: AppDatabase) {
    private var exoPlayerSession: MediaController? = null
    private var job: Job? = null
    private val songDao = database.getSongDao()
    private var isInitRestorePlaylist = false
    private val _mediaItems = MutableStateFlow<SongListType>(SongListType.LocalList(emptyList()))
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
    private val _playUuid = MutableStateFlow<String?>(null)
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
    actual val playUuid = _playUuid.asStateFlow()


    actual val currentSong = _currentSong.asStateFlow()
    actual val lyricStatus = _lyricStatus.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                _duration.value = exoPlayerSession?.duration ?: 0
                _currentPosition.value = exoPlayerSession?.currentPosition ?: 0
                val p = _currentPosition.value.toFloat() / _duration.value.toFloat()
                _progress.value = if (p.isNaN()) 0f else p
                if (isPlaying.value) StorageManager.setItem(
                    "currentPosition",
                    _currentPosition.value
                )
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
                    mediaItem: MediaItem?,
                    reason: Int
                ) {
                    when (reason) {
                        Player.MEDIA_ITEM_TRANSITION_REASON_SEEK, Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                            // 直接从播放器获取当前准确的媒体项索引，避免用 indexOf 查错
                            val index = exoPlayerSession?.currentMediaItemIndex ?: return
                            println("indexindexindexindex $index")
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
        when (val med = mediaItems.value) {
            is SongListType.LocalList -> {
                med.songList.getOrNull(index)?.let { song ->
                    _currentSong.value = SongType.Local(song)
                    _playUuid.value = song.uuid
                }
            }

            is SongListType.NetworkList -> {
                med.songList.getOrNull(index)?.let { song ->
                    _currentSong.value = SongType.Network(song)
                    _playUuid.value = song.uuid
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val url = song.url!!
                            val cache = songDao.getSongCacheByUrl(url)
                            if (cache == null) {
                                val file = downloadMusicToCache(context, url, song.uuid!!)
                                if (file != null) {
                                    songDao.insertSongCache(SongCache(url, file.path))

                                    withContext(Dispatchers.Main) {
                                        // 1. 确保播放器依然活着，且当前正在播放网络歌曲
                                        val player = exoPlayerSession ?: return@withContext
                                        val currentIndex = player.currentMediaItemIndex
                                        if (currentIndex == -1) return@withContext

                                        val oldMediaItem = player.getMediaItemAt(currentIndex)

                                        // 2. 双重校验：确保当前播放的 mediaId 或 uri 确实就是这首歌的 url
                                        // 防止用户在下载期间切歌了，结果把新切的歌给替换/重置了
                                        if (oldMediaItem.mediaId == url || oldMediaItem.localConfiguration?.uri.toString() == url) {
                                            val newMediaItem = oldMediaItem.buildUpon()
                                                .setMediaId(file.path)
                                                .setUri(file.path)
                                                .build()
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
    }

    actual fun resetMediaItems(
        key: String,
        songItems: SongListType,
        refresh: Boolean?,
        startIndex: Int?
    ) {
        when (songItems) {
            is SongListType.LocalList -> {
                val songList = songItems.songList
                if (_playKey.value == key && songList.size == (mediaItems.value as? SongListType.LocalList)?.songList?.size && refresh != true) return
                exoPlayerSession?.clearMediaItems()
                _mediaItems2 = songList.map { buildMediaItems(it) }
                exoPlayerSession?.setMediaItems(_mediaItems2, true)
                _mediaItems.value = SongListType.LocalList(songList)
                StorageManager.setItem("mediaItems", songList.map { it.id })
                StorageManager.setItem<String>("playKey", key)
            }

            is SongListType.NetworkList -> {
                val songList = songItems.songList
                if (_playKey.value == key && songList.size == ((mediaItems.value as? SongListType.NetworkList)?.songList?.size) && refresh != true) return
                exoPlayerSession?.clearMediaItems()
                // 2. 存储与序列化操作（在 IO 线程执行，没问题）
                StorageManager.setItem("mediaItems", songList.map { it.id })
                StorageManager.setItem<String>("playKey", key)
                _mediaItems.value = SongListType.NetworkList(songList)
                CoroutineScope(Dispatchers.IO).launch {
                    val cacheList = songDao.getSongCache()
                    val newMediaItems = songList.map { buildNetMediaItems(it, cacheList) }
                    withContext(Dispatchers.Main) {

                        println("startIndexstartIndexstartIndexstartIndex $startIndex")

                        exoPlayerSession?.setMediaItems(newMediaItems, startIndex ?: 0, 0L)
                        prepare(startIndex ?: 0)
                    }
                }
            }
        }

        _playKey.value = key

    }

    actual fun updateMediaItem(song: SongType) {
        _mediaItems.value = when (val current = _mediaItems.value) {
            is SongListType.LocalList -> {
                if (song is SongType.Local) {
                    current.copy(
                        songList = current.songList.map {
                            if (it.id == song.song.id) song.song else it
                        }
                    )
                } else current
            }

            is SongListType.NetworkList -> {
                if (song is SongType.Network) {
                    current.copy(
                        songList = current.songList.map {
                            if (it.id == song.song.id) song.song else it
                        }
                    )
                } else current
            }

        }

        when (val current = _mediaItems.value) {
            is SongListType.LocalList ->
                StorageManager.setItem("mediaItems", current.songList.map { it.id })

            is SongListType.NetworkList ->
                StorageManager.setItem("mediaItems", current.songList.map { it.id })
        }
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
                    // 现在的环境是 Main 线程了，ExoPlayer
                    resetMediaItems(key, SongListType.LocalList(newData), true)
                    if (playerInfo != null) {
                        _duration.value = playerInfo.duration
                        _playIndex.value = playerInfo.playIndex
                        _playUuid.value = playerInfo.playUuid
                        _playMode.value = playerInfo.playMode
                        prepare(_playIndex.value)
                        _currentPosition.value = currentPosition
                        setPlayerRepeatMode()
                        getSongLyric()
                    }
                }
            }
        }
    }

    actual fun addSong(song: SongType) {
        when (val current = _mediaItems.value) {
            is SongListType.LocalList -> {
                if (song is SongType.Local) {
                    val songList = current.songList
                    if (songList.none { it.id == song.song.id }) {
                        _mediaItems.value = current.copy(
                            songList = songList + song.song
                        )
                    }
                }
            }

            is SongListType.NetworkList -> {
                if (song is SongType.Network) {
                    val songList = current.songList
                    if (songList.none { it.id == song.song.id }) {
                        _mediaItems.value = current.copy(
                            songList = songList + song.song
                        )
                    }
                }
            }
        }

        prepare(mediaItemsLength() - 1)
        play()
    }

    fun mediaItemsLength(): Int {
        return when (val current = _mediaItems.value) {
            is SongListType.LocalList -> current.songList.size
            is SongListType.NetworkList -> current.songList.size
        }
    }

    actual fun delSong(song: SongType) {
        val songId = when (song) {
            is SongType.Local -> song.song.id
            is SongType.Network -> song.song.id
        }

        val current = _mediaItems.value

        val index = when (current) {
            is SongListType.LocalList ->
                current.songList.indexOfLast { it.id == songId }

            is SongListType.NetworkList ->
                current.songList.indexOfLast { it.id == songId }
        }

        if (index == -1) return

        val isCurrent = when (val cur = currentSong.value) {
            is SongType.Local -> cur.song.id == songId
            is SongType.Network -> cur.song.id == songId
            null -> false
        }

        exoPlayerSession?.removeMediaItem(index)

        _mediaItems.value = when (current) {
            is SongListType.LocalList -> current.copy(
                songList = current.songList.filter { it.id != songId }
            )

            is SongListType.NetworkList -> current.copy(
                songList = current.songList.filter { it.id != songId }
            )
        }

        val ids = when (val updated = _mediaItems.value) {
            is SongListType.LocalList -> updated.songList.map { it.id }
            is SongListType.NetworkList -> updated.songList.map { it.id }
        }

        StorageManager.setItem("mediaItems", ids)

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
                            val lyric = FetchMusic.getSongLyric(it.song.id)
                            _lyricList.value = lyric
                            if (lyric.isNotEmpty()) {
                                _lyricStatus.value = LyricStatus.Finished(
                                    LyricInfo(
                                        parseLyric(lyric[0].content)!!,
                                        null,
                                        0
                                    )
                                )
                            } else {
                                _lyricStatus.value = LyricStatus.NotFound
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
        val player = exoPlayerSession ?: return
        player.shuffleModeEnabled = false
        _playMode.value = when (playMode.value) {
            PlayMode.LIST_LOOP -> {
                SnackbarManager.show("单曲循环")
                player.repeatMode = REPEAT_MODE_ONE
                PlayMode.SINGLE_LOOP
            }

            PlayMode.SINGLE_LOOP -> {
                SnackbarManager.show("列表播放")
                player.repeatMode = REPEAT_MODE_OFF
                PlayMode.LIST
            }

            PlayMode.LIST -> {
                SnackbarManager.show("随机播放")
                player.shuffleModeEnabled = true
                player.repeatMode = REPEAT_MODE_ALL
                PlayMode.SHUFFLE
            }

            PlayMode.SHUFFLE -> {
                SnackbarManager.show("列表循环")
                player.repeatMode = REPEAT_MODE_ALL
                PlayMode.LIST_LOOP
            }
        }
        savePlayerInfo()
    }

    actual fun prepare(mediaItemIndex: Int) {
        if (mediaItemIndex < 0) return
        _playIndex.value = mediaItemIndex
        when (val cur = mediaItems.value) {
            is SongListType.LocalList -> {
                cur.songList.getOrNull(mediaItemIndex)?.let {
                    _currentSong.value = SongType.Local(it)
                    _playUuid.value = it.uuid
                }
            }

            is SongListType.NetworkList -> {
                cur.songList.getOrNull(mediaItemIndex)?.let {
                    _currentSong.value = SongType.Network(it)
                    _playUuid.value = it.uuid
                }
            }

        }
        exoPlayerSession?.seekTo(mediaItemIndex, 0)
        exoPlayerSession?.prepare()
        savePlayerInfo()
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


    private fun buildNetMediaItems(song: Song, cacheList: List<SongCache>): MediaItem {
        val cache = cacheList.find { it.url == song.url }
        val url = when (cache) {
            null -> song.url!!
            else -> cache.filePath
        }

        return MediaItem.Builder()
            .setMediaId(url)
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(song.artists.joinToString("/") { it.artistName })
                    .setTitle(song.title)
                    .setArtworkUri((song.cover).toUri())
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

    private fun mediaItemsIsEmpty(): Boolean {
        return when (val med = mediaItems.value) {
            is SongListType.NetworkList -> med.songList.isEmpty()
            is SongListType.LocalList -> med.songList.isEmpty()
        }
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
        return Random.nextInt(mediaItemsLength())
    }

    actual fun next() {
        if (mediaItemsIsEmpty()) return
        val index = if (playMode.value == PlayMode.SHUFFLE) {
            randomPlayIndex()
        } else {
            (_playIndex.value + 1) % mediaItemsLength()
        }
        prepare(index)
        if (!isPlaying.value) play()
    }

    actual fun prev() {
        if (mediaItemsIsEmpty()) return
        val index = if (playMode.value == PlayMode.SHUFFLE) {
            randomPlayIndex()
        } else {
            (_playIndex.value - 1) % mediaItemsLength()
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
            val url = when (val cur = currentSong.value) {
                is SongType.Local -> cur.song.path
                is SongType.Network -> cur.song.cache
                null -> return
            }
            if (url == null) return
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                url.toUri()
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