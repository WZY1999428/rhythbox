package wzy.hkta.rhythbox.expect

import kotlinx.coroutines.flow.StateFlow
import wzy.hkta.rhythbox.fatchMusic.LyricData
import wzy.hkta.rhythbox.utils.SongType

actual class RhythboxPlayer {
    actual val isPlaying: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
    actual val currentSong: StateFlow<SongType?>
        get() = TODO("Not yet implemented")
    actual val lyricStatus: StateFlow<LyricStatus>
        get() = TODO("Not yet implemented")
    actual val lyricList: StateFlow<List<LyricData>>
        get() = TODO("Not yet implemented")
    actual val currentPosition: StateFlow<Long>
        get() = TODO("Not yet implemented")
    actual val duration: StateFlow<Long>
        get() = TODO("Not yet implemented")
    actual val playerLoading: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
    actual val mediaItems: StateFlow<MutableList<SongType>>
        get() = TODO("Not yet implemented")
    actual val playMode: StateFlow<PlayMode>
        get() = TODO("Not yet implemented")
    actual val playIndex: StateFlow<Int>
        get() = TODO("Not yet implemented")
    actual val playKey: StateFlow<String?>
        get() = TODO("Not yet implemented")
    actual val playMediaId: StateFlow<String?>
        get() = TODO("Not yet implemented")
    actual val progress: StateFlow<Float>
        get() = TODO("Not yet implemented")

    actual fun prepare(mediaItemIndex: Int) {
    }

    actual fun play() {
    }

    actual fun shufflePlay() {
    }

    actual fun togglePlay() {
    }

    actual fun addSong(song: SongType) {
    }

    actual fun delSong(song: SongType) {
    }

    actual fun next() {
    }

    actual fun prev() {
    }

    actual fun pause() {
    }

    actual fun seekTo(position: Long) {
    }

    actual fun release() {
    }

    actual fun restorePlaylist() {
    }

    actual fun getSongLyric() {
    }

    actual fun sortMediaItems(songItems: List<SongType>) {
    }

    actual fun resetMediaItems(
        key: String,
        songItems: List<SongType>,
        refresh: Boolean?,
        cb: (() -> Unit)?
    ) {
    }

    actual fun updateMediaItem(song: SongType) {
    }

    actual fun setRhythboxRingtone() {
    }

    actual fun onChangePlayMode() {
    }
}