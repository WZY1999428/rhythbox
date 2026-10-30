package wzy.hkta.rhythbox.utils

import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.fatchMusic.Song


sealed interface SongListType {
    data class LocalList(val songList: List<SongEntity>) : SongListType
    data class NetworkList(val songList: List<Song>) : SongListType
}

sealed interface SongType {
    data class Local(val song: SongEntity) : SongType
    data class Network(val song: Song) : SongType
}


data class SongInfo(
    val id : Long,
    val cover : String?,
    val name : String,
    val artist : String?,
    val isLike : Boolean = false,
    val uuid: String
)



 fun getSongInfo(song: SongType?):SongInfo?{
    return when (song) {
        is SongType.Local ->{
            SongInfo(
                id = song.song.id,
                cover = song.song.cover,
                name = song.song.name,
                artist = song.song.artist,
                isLike = song.song.isLike,
                uuid = song.song.uuid!!
            )
        }

        is SongType.Network -> {
            SongInfo(
                id = song.song.id,
                cover = song.song.cover,
                name = song.song.title,
                uuid = song.song.uuid!!,
                artist = song.song.artists.joinToString("/") { it.artistName }
            )
        }

        null -> null
    }
}