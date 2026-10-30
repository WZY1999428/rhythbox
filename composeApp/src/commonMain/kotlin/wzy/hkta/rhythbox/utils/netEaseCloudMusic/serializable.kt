package wzy.hkta.rhythbox.utils.netEaseCloudMusic
import kotlinx.serialization.Serializable

@Serializable
internal data class Song(
    val id: Long,
    val name: String,
    val duration: Long,
    val fee: Int? = null,
    val album: Album? = null,
    val artists: List<Artist>? = null
)

@Serializable
internal data class Artist(
    val id: Long,
    val name: String
)

@Serializable
internal data class Album(
    val id: Long,
    val name: String,
    val publishTime: Long? = null
)

@Serializable
internal data class NetEaseLyricResponse(
    val code: Int,
    val lrc: LrcContent? = null,
    val tlyric: LrcContent? = null
)

@Serializable
internal data class LrcContent(
    val version: Int = 0,
    val lyric: String = "" // 可能是正常 LRC，也可能是空字符串 ""
)


@Serializable
internal data class AISong(
    val id: Long,
    val duration: Long,
    val name: String,
    val album: String?,
    val artists: List<String>? = null
)

@Serializable
 data class AIResult(
    val id: Long?,
)

@Serializable
internal data class SearchResult(
    val result: Result
)

@Serializable
internal data class Result(
    val songs: List<Song>
)
