package wzy.hkta.rhythbox.expect

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import wzy.hkta.rhythbox.database.SongEntity

enum class PlayMode {
    LIST,       // 顺序播放
    LIST_LOOP,  // 列表循环
    SINGLE_LOOP,// 单曲循环
    SHUFFLE     // 随机播放
}

// commonMain
expect class RhythboxPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentSong: StateFlow<SongEntity?>
    val lyricStatus: StateFlow<LyricStatus>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val mediaItems: StateFlow<List<SongEntity>>
    val playMode: StateFlow<PlayMode>
    val playIndex: StateFlow<Int>
    //    val playKey: StateFlow<String?>
    val playUuid: StateFlow<String?>
    val progress: StateFlow<Float>
    fun prepare(mediaItemIndex: Int)
    fun play()
    fun togglePlay()
    fun addSong(songItem: SongEntity)
    fun delSong(songEntity: SongEntity)

    fun next()
    fun prev()
    fun pause()
    fun seekTo(position: Long)
    fun release()
    fun restorePlaylist()
    fun getSongLyric()
    fun resetMediaItems(key: String, items: List<SongEntity>, refresh: Boolean?)
    fun updateMediaItem(item: SongEntity)
    fun setRhythboxRingtone()
    fun onChangePlayMode()
}



@Serializable
data class PlayerInfo(
    val playMode: PlayMode,
    val duration: Long,
    val playIndex: Int,
    val playUuid: String?
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



