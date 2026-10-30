package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import wzy.hkta.rhythbox.database.CollectEntity

@Dao
interface CollectDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: CollectEntity)

    @Query("DELETE FROM CollectEntity WHERE songId = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM CollectEntity WHERE songId = :id")
    suspend fun isSongCollected(id: Long): Boolean

    @Query("DELETE FROM CollectEntity")
    suspend fun clear()
}





