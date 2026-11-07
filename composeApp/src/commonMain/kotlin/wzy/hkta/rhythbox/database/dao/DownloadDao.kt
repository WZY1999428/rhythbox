package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import wzy.hkta.rhythbox.database.DownloadSongEntity

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(item: List<DownloadSongEntity>)

    @Upsert
    suspend fun upsert(item: DownloadSongEntity)

    @Query("DELETE FROM DownloadSongEntity WHERE id = :id")
    suspend fun delete(id: Long)


    @Query("SELECT EXISTS(SELECT 1 FROM SongEntity INNER JOIN DownloadSongEntity ON SongEntity.id = DownloadSongEntity.id WHERE DownloadSongEntity.id = :id)")
    suspend fun exists(id: Long): Boolean


    @Query("SELECT * FROM DownloadSongEntity")
    suspend fun findAll(): List<DownloadSongEntity>

    @Query("DELETE FROM DownloadSongEntity")
    suspend fun clear()
}
