package wzy.hkta.rhythbox.utils

import kotlinx.serialization.Serializable
import wzy.hkta.rhythbox.database.SongEntity
import kotlinx.serialization.SerialName
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.fatchMusic.SongArtist


@Serializable
sealed interface SongType {

    val id: Long
    val cover: String?
    val name: String
    val artist: String
    val isLike: Boolean
    val mediaId: String
    val duration: Long
    val url: String
    val sourceType: String
    val artists: List<SongArtist>?
    val createAt: String
    val filePath: String?

    @Serializable // 2. 子类也必须加这个！
    @SerialName("Local") // 3. 建议加上明确的序列化别名，防止混淆
    data class Local(
        val song: SongEntity
    ) : SongType {
        override val id = song.id
        override val sourceType = "Local"
        override val cover = song.cover
        override val name = song.name
        override val artist = song.artist ?: "未知歌手"
        override val isLike = song.isLike
        override val mediaId = song.mediaId?:""
        override val url = song.path
        override val duration = song.duration
        override val artists = null
        override val filePath = null
        override val createAt = formatDate(song.createTime)
    }

    @Serializable // 2. 子类也必须加这个！
    @SerialName("Network") // 3. 建议加上明确的序列化别名，防止混淆
    data class Network(
        val song: Song
    ) : SongType {
        override val id = song.id
        override val sourceType = "Network"
        override val cover = song.cover
        override val name = song.title
        override val isLike = song.isLike
        override val mediaId = song.mediaId?:""
        override val url = song.url?:""
        override val duration = song.duration
        override val artist = song.artists.joinToString("/") { it.artistName }
        override val artists = song.artists
        override val createAt = formatDate(song.publishDate)
        override val filePath = song.cache
    }
}

// 只需要这一个函数，支持所有属性的任意组合修改，还完美支持命名参数！
fun SongType.copy(
    cover: String? = this.cover,
    name: String = this.name,
    artist: String? = this.artist,
    isLike: Boolean = this.isLike,
    mediaId: String = this.mediaId,
    url: String = this.url,
): SongType {
    return when (this) {
        is SongType.Local -> {
            this.copy(
                song = this.song.copy(
                    isLike = isLike,
                    name = name,
                    path = url,
                    cover = cover ?: this.song.cover,
                    artist = artist ?: this.song.artist,
                    mediaId = mediaId,
                )
            )
        }

        is SongType.Network -> {
            this.copy(
                song = this.song.copy(
                    isLike = isLike,
                    title = name,
                    url = url,
                    cover = cover ?: this.song.cover,
                    mediaId = mediaId,
                )
            )
        }
    }
}


