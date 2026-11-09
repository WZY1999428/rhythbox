package wzy.hkta.rhythbox.utils

import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.fatchMusic.Song
fun sortSong(songList: List<SongType>, name: String, order: String): List<SongType> {
    return when (name) {
        "date" -> if (order == "asc")
            songList.sortedBy { it.createAt }
        else
            songList.sortedByDescending { it.createAt }

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