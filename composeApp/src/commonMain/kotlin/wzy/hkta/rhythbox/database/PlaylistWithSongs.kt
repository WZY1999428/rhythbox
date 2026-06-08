package wzy.hkta.rhythbox.database

import androidx.room.Embedded
import androidx.room.Relation


data class PlaylistWithSongs(
    @Embedded
    val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId",
    )
    val songs: List<PlaylistSongEntity>
)