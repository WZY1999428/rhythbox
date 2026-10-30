package wzy.hkta.rhythbox.utils.fatchMusic

import kotlinx.serialization.Serializable


@Serializable
data class Response<T>(
    val code: Int,
    val message: String,
    val data: T?
)

@Serializable
data class ListData<T>(
    val list: List<T>,
    val total: Long,
)

@Serializable
data class HomeData(
    val hotSongList: List<Song>,
    var bannerList: List<Banner>,
)


@Serializable
data class Banner(
    val id: Long,
    val title: String,
    var image: String,
    val type: String,
    val value: String,
    val desc: String
)


@Serializable
data class PlaylistInfo(
    val id: Long,
    val title: String,
    val cover: String? = null,
    val description: String,
    val favoriteTotal: Long,
    val playTotal: Long,
    val clickTotal: Long,
) {
}

@Serializable
data class SongArtist(
    val artistId: String,
    val artistName: String,
)

@Serializable
data class Song(
    val id: Long,
    val url: String? = null,
    val cache: String? = null,
    val title: String,
    val duration: Long,
    val isLike: Boolean = false,
    val mvUrl: String? = null,
    val language: List<Language> = emptyList(),
    val cover: String,
    val uuid: String? = null,
    val artists: List<SongArtist> = emptyList()
)

@Serializable
data class Language(
    val id: Long,
    val lang: String
)

@Serializable
data class Artist(
    val id: Long,
    val name: String,
    val realName: String,
    val pinyin: String? = null,
    val gender: String,
    val description: String,
    val sort: Int,
    val avatar: String? = null,
    val cover: String? = null
)

@Serializable
data class LyricData(
    val id: Long,
    val title: String,
    val songId: Long,
    val lang: String,
    val content: String,
)

@Serializable
data class Complex(
    val playlist: List<Playlist>,
    val album: List<Album>,
    val song: List<Song>,
    val artist: List<Artist>,
)


@Serializable
data class Playlist(
    val id: Long,
    val title: String,
    val cover: String? = null,
    val isHot: Boolean,
    val sort: Int,
    val description: String,
    val favoriteTotal: Long,
    val playTotal: Long,
    val clickTotal: Long
)


@Serializable
data class Album(
    val id: Long,
    val title: String,
    val releaseDate: String,
    val company: String,
    val type: String,
    val genre: String,
    val description: String,
    val artistId: String? = null,
    val artistName: String? = null,
    val cover: String? = null
)
