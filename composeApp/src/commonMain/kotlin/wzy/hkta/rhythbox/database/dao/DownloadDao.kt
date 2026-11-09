package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wzy.hkta.rhythbox.database.DownloadSongEntity

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(item: List<DownloadSongEntity>)

    @Upsert
    suspend fun upsert(item: DownloadSongEntity)

    @Query("DELETE FROM DownloadSongEntity WHERE id = :id")
    suspend fun delete(id: Long)


    @Query("DELETE FROM DownloadSongEntity WHERE downloadPath = :filePath")
    suspend fun deleteOnFile(filePath: String)

    @Query("DELETE FROM DownloadSongEntity WHERE downloadPath IN (:filePaths)")
    suspend fun deleteOnInFile(filePaths: List<String>)

    @Query("SELECT EXISTS(SELECT 1 FROM  DownloadSongEntity  WHERE DownloadSongEntity.downloadPath = :filePath)")
    suspend fun exists(filePath: String): Boolean


    @Query("UPDATE DownloadSongEntity SET downloadStatus = :status WHERE downloadPath = :filePath")
    suspend fun updateStatus(filePath: String,status: Int)

    @Query("SELECT * FROM DownloadSongEntity WHERE downloadStatus = 1")
     fun findSuccess(): Flow<List<DownloadSongEntity>>

    @Query("SELECT * FROM DownloadSongEntity WHERE downloadStatus != 1")
     fun findOther(): Flow<List<DownloadSongEntity>>

    @Query("SELECT * FROM DownloadSongEntity")
    suspend fun findALL(): List<DownloadSongEntity>

    @Query("SELECT COUNT(id) FROM DownloadSongEntity")
    fun count() : Int
    @Query("DELETE FROM DownloadSongEntity")
    suspend fun clear()
}
