package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wzy.hkta.rhythbox.database.AlbumEntity

@Dao
interface AlbumDao {
    @Insert
    suspend fun insert(item: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(item: List<AlbumEntity>)

    @Upsert
    suspend fun upsert(item: AlbumEntity)

    @Query("UPDATE AlbumEntity SET 'name' = :newName , 'artist' = :artist WHERE name = :oldName ")
    suspend fun updateByName(oldName: String, newName: String, artist: String)

    @Query("DELETE FROM AlbumEntity WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM AlbumEntity")
    fun find(): Flow<List<AlbumEntity>>


    @Query("DELETE FROM AlbumEntity")
    suspend fun clear()
}
