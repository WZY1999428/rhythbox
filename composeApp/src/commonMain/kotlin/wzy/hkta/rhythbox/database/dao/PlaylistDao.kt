package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.database.PlaylistSongEntity
import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.database.PlaylistWithSongs

@Dao
interface PlaylistDao {
    @Query(
        """
    SELECT 
        s.id,s.name,s.describe,s.createTime,(SELECT COUNT(*)  FROM PlaylistSongEntity WHERE :id = playlistId) AS count
        FROM PlaylistEntity s WHERE id=:id
    """
    )
    suspend fun get(id: Long): PlaylistEntity

    @Transaction
    @Query("SELECT * FROM PlaylistEntity")
    fun getPlaylistWithSongs(): Flow<List<PlaylistWithSongs>>

    @Query("SELECT * FROM PlaylistEntity")
    suspend fun getList(): List<PlaylistEntity>


    @Query("SELECT * FROM PlaylistSongEntity WHERE playlistId = :playlistId")
    suspend fun getPlaylistSongList(playlistId: Long): List<PlaylistSongEntity>


    @Insert
    suspend fun insert(item: PlaylistEntity)

    @Query("DELETE FROM PlaylistEntity WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM PlaylistSongEntity WHERE playlistId = :id")
    suspend fun deletePlaylistSongs(id: Long)

    @Transaction
    suspend fun deletePlaylistWithSongs(id: Long) {
        deletePlaylistSongs(id)
        deletePlaylist(id)
    }


    @Insert
    suspend fun addSong(item: PlaylistSongEntity)

    @Query("DELETE FROM PlaylistSongEntity WHERE id = :id")
    suspend fun delSong(id: Long)

    @Query("DELETE FROM CollectEntity")
    suspend fun clear()
}



@Dao
interface PlaylistSortDao {
    @Query("SELECT * FROM PlaylistSortEntity WHERE sortKey = :key ")
    fun get(key: String): Flow<PlaylistSortEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: PlaylistSortEntity)

    @Query("DELETE FROM  PlaylistSortEntity WHERE sortKey = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM CollectEntity")
    suspend fun clear()
}