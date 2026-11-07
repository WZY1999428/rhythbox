package wzy.hkta.rhythbox.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import wzy.hkta.rhythbox.fatchMusic.SongArtist
import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

@Entity(
    indices = [
        Index(value = ["path", "name", "size", "createTime"], unique = true)
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val name: String,
    val size: Long = 0,
    val duration: Long = 0,
    val dirId: String,
    val cover: String?,
    val artist: String? = null,
    val album: String? = null,
    val playCount: Int = 0,
    var isLike: Boolean = false,
    val createTime: Long = System.currentTimeMillis(),
    var mediaId: String? = null
)

@Entity(indices = [Index(value = ["songId"], unique = true)])
data class LyricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: String,
    val lyric: String,
    val transLyric: String? = null,
    val offset: Int = 0
)

@Entity(
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val artist: String,
    val count: Int = 0,
    val cover: String? = null
)


@Entity(indices = [Index(value = ["songId"], unique = true)])
data class CollectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val createTime: Long = System.currentTimeMillis(),
)

@Entity(
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class SingerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val count: Int = 0,
    val cover: String? = null,
)

@Entity(
    indices = [
        Index(value = ["name", "fullPath"], unique = true)
    ]
)
data class DirEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val count: Int = 0,
    val fullPath: String,
)

@Entity(
    indices = [Index(value = ["name"], unique = true)]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val describe: String?,
    val createTime: Long = System.currentTimeMillis(),
    val count: Int = 0,
)

@Entity(
    indices = [Index(value = ["playlistId", "songId"], unique = true)]
)
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val songId: Long,
)

data class SongInfo(
    val id: Long,
    val name: String,
    val count: Int = 0,
    val cover: String? = null
)


@Entity(
    indices = [Index(value = ["sortKey"], unique = true)]
)
data class PlaylistSortEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sortKey: String,
    @ColumnInfo(defaultValue = "'name'")
    val sortName: String = "name",
    @ColumnInfo(defaultValue = "'asc'")
    val sortType: String = "asc",
)


@Entity(
    indices = [Index(value = ["songId"], unique = true)]
)
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val createTime: Long = System.currentTimeMillis(),
)

@Entity(
    indices = [Index(value = ["url", "filePath"], unique = true)]
)
data class SongCache(
    @PrimaryKey
    val url: String,
    val filePath: String
)


@Entity(
    indices = [Index(value = ["url", "filePath"], unique = true)]
)
data class DownloadSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val url: String? = null,
    val filePath: String? = null,
    val title: String,
    val duration: Long,
    var isLike: Boolean = false,
    val mvUrl: String? = null,
    val cover: String,
    val mediaId: String? = null,
    val originalName: String,
    val filename: String,
    val artists: List<SongArtist> = emptyList()
)


class SongArtistConverter {

    @TypeConverter
    fun fromArtists(value: List<SongArtist>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toArtists(value: String): List<SongArtist> {
        return Json.decodeFromString(value)
    }
}