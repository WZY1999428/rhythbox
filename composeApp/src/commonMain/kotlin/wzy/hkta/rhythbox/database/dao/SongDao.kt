package wzy.hkta.rhythbox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import wzy.hkta.rhythbox.database.LyricEntity
import wzy.hkta.rhythbox.database.PlayHistoryEntity
import wzy.hkta.rhythbox.database.SongCache
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.database.SongInfo

@Dao
interface SongDao {
    @Insert
    suspend fun insert(item: SongEntity)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongCache(item: SongCache)

    @Query("DELETE FROM SongCache WHERE filePath = :filePath")
    suspend fun deleteSongCache(filePath: String)

    @Query("SELECT * FROM SongCache WHERE url = :url")
    suspend fun getSongCacheByUrl(url: String): SongCache?

    @Query("SELECT * FROM SongCache")
    suspend fun getSongCache(): List<SongCache>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(item: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayHistory(item: PlayHistoryEntity)

    @Query("SELECT * FROM PlayHistoryEntity ORDER BY createTime DESC LIMIT 100")
    suspend fun getPlayHistory(): List<PlayHistoryEntity>

    @Query("DELETE FROM PlayHistoryEntity WHERE id NOT IN (SELECT id FROM PlayHistoryEntity ORDER BY createTime DESC LIMIT 99)")
    suspend fun deleteOldPlayHistory(): Unit

    @Update
    suspend fun update(item: SongEntity)

    @Query("DELETE FROM SongEntity WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("UPDATE SongEntity SET playCount = playCount + 1 WHERE id=:id ")
    suspend fun incrementPlayCount(id: Long)

    @Query("SELECT *,EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s WHERE playCount > 0 ORDER BY playCount DESC LIMIT 100")
    fun getPlayCountList(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM (SELECT * FROM SongEntity WHERE playCount > 0 ORDER BY playCount DESC LIMIT 100)")
    fun getPlayCount(): Flow<Int>

    @Query("SELECT *,EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s  LIMIT 100")
    fun getRecentlyAddedSongs(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM (SELECT id FROM SongEntity ORDER BY createTime DESC LIMIT 100)")
    fun getRecentlyAddedCount(): Flow<Int>

    @Query("SELECT * FROM SongEntity WHERE id = :id")
    fun findOne(id: Long): SongEntity?


    @Query(
        """
    SELECT * FROM SongEntity 
    WHERE name LIKE '%' || :keyword || '%'
    OR artist LIKE '%' || :keyword || '%'
    OR album LIKE '%' || :keyword || '%'
    """
    )
    fun search(keyword: String): List<SongEntity>


    @Query("SELECT s.*, EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s ")
    fun find(): Flow<List<SongEntity>>

    @Query("SELECT * FROM SongEntity WHERE id = :id")
    fun getById(id: Long): SongEntity?

    @Query("SELECT * FROM SongEntity WHERE id IN (:ids)")
    fun getByIds(ids: List<Long>): MutableList<SongEntity>

    @Query("SELECT *,EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s WHERE album=:album ")
    fun getAlbum(album: String, ): Flow<MutableList<SongEntity>>

    @Query("SELECT *, EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s WHERE artist=:artist ")
    fun getArtist(artist: String, ): Flow<MutableList<SongEntity>>

    @Query("SELECT *,EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s WHERE dirId=:dir ")
    fun getDir(dir: String, ): Flow<MutableList<SongEntity>>

    @Query("SELECT COUNT(*) FROM (SELECT * FROM SongEntity WHERE dirId=:dir)")
    fun getDirCount(dir: String): Flow<Int>

    @Query("DELETE  FROM SongEntity WHERE path in (:paths)")
    suspend fun deleteByPaths(paths: List<String>)

    @Query(
        """
     SELECT 
        s.id,s.name,s.cover,(SELECT COUNT(*) FROM SongEntity s2  WHERE s2.artist = s.name ) AS count
        FROM SingerEntity s WHERE s.id = :id """
    )
    fun getBySingerId(id: Long): Flow<SongInfo>

    @Query(
        """
    SELECT 
        s.id,s.name,s.cover,(SELECT COUNT(*) FROM SongEntity s2  WHERE s2.artist = s.name ) AS count
        FROM SingerEntity s WHERE s.name = :name """
    )
    fun getBySingerName(name: String): SongInfo

    @Query(
        """
    SELECT 
        s.id,s.name,s.cover,(SELECT COUNT(*) FROM SongEntity s2  WHERE s2.album = s.name ) AS count
        FROM AlbumEntity s WHERE s.id = :id """
    )
    fun getByAlbumId(id: Long): Flow<SongInfo>

    @Query(
        """
    SELECT 
         s.id,s.name,s.cover,(SELECT COUNT(*) FROM SongEntity s2  WHERE s2.album = s.name ) AS count
        FROM AlbumEntity s WHERE s.name = :name """
    )
    fun getByAlbumName(name: String): SongInfo


    @Query(
        """
    SELECT d.id,d.name,s.cover,
        (SELECT COUNT(*) FROM SongEntity s2 WHERE s2.name = d.name ) AS count
    FROM DirEntity d LEFT JOIN SongEntity s ON s.path = d.fullPath  WHERE d.id = :id"""
    )
    fun getDirId(id: Long): Flow<SongInfo>

    @Query(
        """
    SELECT d.id,d.name,s.cover,
        (SELECT COUNT(*) FROM SongEntity s2 WHERE s2.name = d.name ) AS count
    FROM DirEntity d LEFT JOIN SongEntity s ON s.path = d.fullPath  WHERE d.fullPath = :path """
    )
    fun getDirByPath(path: String): SongInfo


    @Query(
        """
        SELECT p.id,p.name,p.describe, 
        (SELECT COUNT(*) FROM PlaylistSongEntity s2 WHERE s2.playlistId = p.id ) AS count
        FROM PlaylistEntity p  WHERE p.id = :id"""
    )
    fun getPlayListId(id: Long): Flow<SongInfo>


    @Query(
        """
    SELECT s.*, EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike
    FROM PlaylistSongEntity ps
    INNER JOIN SongEntity s
        ON s.id = ps.songId
    WHERE ps.playlistId = :id
    
"""
    )
    fun getPlayListSong(id: Long): Flow<MutableList<SongEntity>>

    @Query(
        """
    SELECT song.*,EXISTS(SELECT 1 FROM CollectEntity c WHERE c.songId = song.id ) AS isLike FROM CollectEntity s
    LEFT JOIN SongEntity song ON s.songId = song.id
    GROUP BY s.id 
    """
    )
    fun findCollect(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM CollectEntity ")
    fun collectCount(): Flow<Int>

    @Query("DELETE FROM SongEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM SongEntity")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllLyric(item: List<LyricEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLyric(item: LyricEntity)

    @Query("SELECT * FROM LyricEntity WHERE songId = :id")
    fun getLyric(id: String): LyricEntity?

    @Query("UPDATE LyricEntity SET `offset` = :offset WHERE songId=:songId")
    fun updateLyricOffset(songId: String, offset: Int)

    @Query("DELETE FROM LyricEntity")
    fun clearLyric()


}
