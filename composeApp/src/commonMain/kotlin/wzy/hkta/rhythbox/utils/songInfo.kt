package wzy.hkta.rhythbox.utils

import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.fatchMusic.Song


sealed interface SongListType {
    data class LocalList(var songList: MutableList<SongEntity>) : SongListType
    data class NetworkList(var songList: MutableList<Song>) : SongListType
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
    val mediaId: String
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
                mediaId = song.song.mediaId!!
            )
        }

        is SongType.Network -> {
            SongInfo(
                id = song.song.id,
                cover = song.song.cover,
                name = song.song.title,
                isLike = song.song.isLike,
                mediaId = song.song.mediaId!!,
                artist = song.song.artists.joinToString("/") { it.artistName }
            )
        }

        null -> null
    }
}