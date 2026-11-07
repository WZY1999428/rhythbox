package wzy.hkta.rhythbox.utils

import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.fatchMusic.Song
@JvmName("sortSongEntity")
fun sortSong(songList: List<SongEntity>, name: String, order: String): List<SongEntity> {
    return when (name) {
        "date" -> if (order == "asc")
            songList.sortedBy { it.createTime }
        else
            songList.sortedByDescending { it.createTime }

        "name" -> if (order == "asc")
            songList.sortedBy { it.name }
        else
            songList.sortedByDescending { it.name }

        "artist" -> if (order == "asc")
            songList.sortedBy { it.artist }
        else
            songList.sortedByDescending { it.artist }

        else -> songList
    }
}

@JvmName("sortNetworkSong")
fun sortSong(songList: List<Song>, name: String, order: String): List<Song> {
    return when (name) {
        "date" -> if (order == "asc")
            songList.sortedBy { it.publishDate }
        else
            songList.sortedByDescending { it.publishDate }

        "name" -> if (order == "asc")
            songList.sortedBy { it.title }
        else
            songList.sortedByDescending { it.title }

        "artist" -> if (order == "asc")
            songList.sortedBy { it.artists.getOrNull(0)?.artistName }
        else
            songList.sortedByDescending { it.artists.getOrNull(0)?.artistName }

        else -> songList
    }
}