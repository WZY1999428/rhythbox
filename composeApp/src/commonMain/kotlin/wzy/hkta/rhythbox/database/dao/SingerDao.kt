package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wzy.hkta.rhythbox.database.SingerEntity

@Dao
interface SingerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SingerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(item: List<SingerEntity>)

    @Query("UPDATE SingerEntity SET 'name' = :newName  WHERE name = :oldName ")
    suspend fun updateByName(oldName: String, newName: String)

    @Upsert
    suspend fun upsert(item: SingerEntity)

    @Query("DELETE FROM SingerEntity WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """
    SELECT 
        s.id,s.name,s.cover,COUNT(song.id) AS count
    FROM SingerEntity s
    LEFT JOIN SongEntity song
        ON s.name = song.artist
    GROUP BY s.id
    ORDER BY s.name COLLATE NOCASE ASC
    """
    )
    fun find(): Flow<List<SingerEntity>>


    @Query("SELECT * FROM SingerEntity WHERE id=:id")
    fun getById(id: Int): Flow<SingerEntity>

    @Query("DELETE FROM SingerEntity")
    suspend fun clear()
}

