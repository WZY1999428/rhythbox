package wzy.hkta.rhythbox.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class PlaylistWithSongs(
    @Embedded
    val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",         // PlaylistEntity 的主键
        entityColumn = "id",         // SongEntity 的主键
        associateBy = Junction(
            PlaylistSongEntity::class, // 中间表
            parentColumn = "playlistId", // 中间表中指向 PlaylistEntity 的字段
            entityColumn = "songId"      // 中间表中指向 SongEntity 的字段
        )
    )
    val songList: List<SongEntity>
)