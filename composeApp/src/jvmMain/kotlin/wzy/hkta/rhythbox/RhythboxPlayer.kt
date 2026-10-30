package wzy.hkta.rhythbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.expect.LyricStatus
import wzy.hkta.rhythbox.expect.PlayMode

actual class RhythboxPlayer {
    private val _isPlaying = MutableStateFlow(false)
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    private val _lyricStatus = MutableStateFlow<LyricStatus>(LyricStatus.Init)
    private val _currentPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _mediaItems = MutableStateFlow<List<SongEntity>>(emptyList())
    private val _playMode = MutableStateFlow(PlayMode.LIST)
    private val _playIndex = MutableStateFlow(-1)
    private val _playKey = MutableStateFlow<String?>(null)
    private val _playUuid = MutableStateFlow<String?>(null)
    private val _progress = MutableStateFlow(0f)

    actual val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    actual val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()
    actual val lyricStatus: StateFlow<LyricStatus> = _lyricStatus.asStateFlow()
    actual val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    actual val duration: StateFlow<Long> = _duration.asStateFlow()
    actual val mediaItems: StateFlow<List<SongEntity>> = _mediaItems.asStateFlow()
    actual val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()
    actual val playIndex: StateFlow<Int> = _playIndex.asStateFlow()
    actual val playKey: StateFlow<String?> = _playKey.asStateFlow()
    actual val playUuid: StateFlow<String?> = _playUuid.asStateFlow()
    actual val progress: StateFlow<Float> = _progress.asStateFlow()

    actual fun prepare(mediaItemIndex: Int) {}
    actual fun play() {}
    actual fun togglePlay() {}
    actual fun addSong(songItem: SongEntity) {}
    actual fun delSong(songEntity: SongEntity) {}
    actual fun next() {}
    actual fun prev() {}
    actual fun pause() {}
    actual fun seekTo(position: Long) {}
    actual fun release() {}
    actual fun restorePlaylist() {}
    actual fun getSongLyric() {}
    actual fun resetMediaItems(key: String, items: List<SongEntity>, refresh: Boolean?) {}
    actual fun updateMediaItem(item: SongEntity) {}
    actual fun setRhythboxRingtone() {}
    actual fun onChangePlayMode() {}
}
