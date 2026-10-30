package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wzy.hkta.rhythbox.database.DirEntity

@Dao
interface DirDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(item: List<DirEntity>)

    @Upsert
    suspend fun upsert(item: DirEntity)


    @Query("DELETE FROM DirEntity WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """
    SELECT s.id,s.name,s.fullPath,COUNT(song.id) AS count FROM DirEntity s
    LEFT JOIN SongEntity song ON song.path LIKE s.fullPath || '/%'
        AND instr(substr(song.path,length(s.fullPath) + 2),'/') = 0
    GROUP BY s.id
    ORDER BY s.name COLLATE NOCASE ASC
    """
    )
    fun find(): Flow<List<DirEntity>>

    @Query("DELETE FROM DirEntity")
    suspend fun clear()
}
