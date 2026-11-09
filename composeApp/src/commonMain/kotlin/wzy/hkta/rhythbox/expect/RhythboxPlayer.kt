package wzy.hkta.rhythbox.expect

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.fatchMusic.LyricData

enum class PlayMode {
    LIST,       // 顺序播放
    LIST_LOOP,  // 列表循环
    SINGLE_LOOP,// 单曲循环
    SHUFFLE     // 随机播放
}

// commonMain
expect class RhythboxPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentSong: StateFlow<SongType?>
    val lyricStatus: StateFlow<LyricStatus>
    val lyricList: StateFlow<List<LyricData>>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val playerLoading: StateFlow<Boolean>
    val mediaItems: StateFlow<MutableList<SongType>>
    val playMode: StateFlow<PlayMode>
    val playIndex: StateFlow<Int>
    val playKey: StateFlow<String?>
    val playMediaId: StateFlow<String?>
    val progress: StateFlow<Float>
    fun prepare(mediaItemIndex: Int)
    fun play()
    fun shufflePlay()
    fun togglePlay()
    fun addSong(song: SongType)
    fun delSong(song: SongType)

    fun next()
    fun prev()
    fun pause()
    fun seekTo(position: Long)
    fun release()
    fun restorePlaylist()
    fun getSongLyric()

    fun sortMediaItems(songItems: List<SongType>)
    fun resetMediaItems(
        key: String,
        songItems: List<SongType>,
        refresh: Boolean?,
        cb: (() -> Unit)? = null
    )

    fun updateMediaItem(song: SongType)
    fun setRhythboxRingtone()
    fun onChangePlayMode()
}


@Serializable
data class PlayerInfo(
    val playMode: PlayMode,
    val duration: Long,
    val playIndex: Int,
    val playMediaId: String?,
    val playSongType: String
)

data class SongItem(
    val path: String,
    val name: String,
    val size: Long = 0,
    val duration: Long = 0,
    val dirId: String,
    val cover: String?,
    val artist: String? = null,
    val album: String? = null,
)

data class DirItem(
    val name: String,
    val songList: MutableList<SongItem>,
    val fullPath: String,
)


data class SongInfo(
    val name: String,
    val count: Int = 0,
    val cover: String? = null,
)



