package wzy.hkta.rhythbox.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface SongDao {
    @Insert
    suspend fun insert(item: SongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(item: List<SongEntity>)

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

    @Query("SELECT *,EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s ORDER BY :sort DESC LIMIT 100")
    fun getRecentlyAddedSongs(sort: String): Flow<List<SongEntity>>

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


    @Query("SELECT s.*, EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s ORDER BY :sort")
    fun find(sort: String? = "createTime ASC"): Flow<List<SongEntity>>

    @Query("SELECT * FROM SongEntity WHERE id = :id")
    fun getById(id: Long): SongEntity?

    @Query("SELECT * FROM SongEntity WHERE id IN (:ids)")
    fun getByIds(ids: List<Long>): List<SongEntity>

    @Query("SELECT *,EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s WHERE album=:album ORDER BY :sort")
    fun getAlbum(album: String, sort: String = "name AES"): Flow<List<SongEntity>>

    @Query("SELECT *, EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s WHERE artist=:artist ORDER BY :sort")
    fun getArtist(artist: String, sort: String = "name AES"): Flow<List<SongEntity>>

    @Query("SELECT *,EXISTS( SELECT 1 FROM CollectEntity c WHERE c.songId = s.id ) AS isLike FROM SongEntity s WHERE dirId=:dir ORDER BY :sort")
    fun getDir(dir: String, sort: String = "name AES"): Flow<List<SongEntity>>

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
    ORDER BY :sort
"""
    )
    fun getPlayListSong(id: Long, sort: String = "name ASC"): Flow<List<SongEntity>>

    @Query(
        """
    SELECT song.*,EXISTS(SELECT 1 FROM CollectEntity c WHERE c.songId = song.id ) AS isLike FROM CollectEntity s
    LEFT JOIN SongEntity song ON s.songId = song.id
    GROUP BY s.id ORDER BY :sort
    """
    )
    fun findCollect(sort: String): Flow<List<SongEntity>>

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
    fun getLyric(id: Long): LyricEntity?

    @Query("UPDATE LyricEntity SET `offset` = :offset WHERE songId=:songId")
    fun updateLyricOffset(songId: Long, offset: Int)

    @Query("DELETE FROM LyricEntity")
    fun clearLyric()


}

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





