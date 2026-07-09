package wzy.hkta.rhythbox.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import wzy.hkta.rhythbox.expect.AppDatabaseConstructor

@Database(
    entities = [SingerEntity::class, SongEntity::class, AlbumEntity::class, DirEntity::class, PlaylistSortEntity::class,
        CollectEntity::class, LyricEntity::class, PlaylistEntity::class, PlaylistSongEntity::class],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 4, to = 5)
    ]
)


@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getSingerDao(): SingerDao
    abstract fun getSongDao(): SongDao
    abstract fun getAlbumDao(): AlbumDao
    abstract fun getDirDao(): DirDao
    abstract fun getCollectDao(): CollectDao
    abstract fun getPlaylistDao(): PlaylistDao
    abstract fun getPlayListSortDao(): PlaylistSortDao

    @Transaction
    suspend fun deleteSongAndCleanUpTotally(songId: Long) {
        // 直接调用各个独立 DAO 的方法，它们会自动运行在同一个大事务中！
        val songDao = getSongDao()
        val song = songDao.findOne(songId)
        if (song != null) {
            getPlaylistDao().delSong(songId)
            getCollectDao().delete(songId)
            val album = songDao.getByAlbumName(song.album ?: "其他")
            if (album.count == 0) getAlbumDao().delete(album.id)
            val singer = songDao.getBySingerName(song.artist ?: "未知歌手")
            if (singer.count == 0) getSingerDao().delete(singer.id)
            val dir = songDao.getDirByPath(song.path)
            if (dir.count == 0) getDirDao().delete(dir.id)
        }
    }

    fun getPlayListSort(key: String): String {
        val dao = getPlayListSortDao()
        val savedType = dao.get(key)?.sortType
        return when (savedType) {
            SortTypes.BY_NAME -> "name ASC"
            SortTypes.BY_NAME_DESC -> "name DESC"
            SortTypes.BY_TIME -> "createTime ASC"
            SortTypes.BY_TIME_DESC -> "createTime DESC"
            SortTypes.BY_ARTIST -> "artist ASC"
            SortTypes.BY_ARTIST_DESC -> "artist DESC"
            else -> "name ASC"
        }
    }

    @Transaction
    suspend fun updateSongAndAlbumAndSinger(song: SongEntity, oldItem: SongEntity) {
        val songDao = getSongDao()
        songDao.update(song)
        val album = songDao.getByAlbumName(oldItem.album ?: "其他")
        val albumDao = getAlbumDao()
        if (album.count == 0) {
            val newAlbum = songDao.getByAlbumName(song.album!!)
            if (newAlbum.count > 0) {
                albumDao.delete(album.id)
            } else {
                albumDao.updateByName(oldItem.album!!, song.album, song.artist!!)
            }
        } else {
            albumDao.upsert(AlbumEntity(name = song.album!!, artist = song.artist!!))
        }
        val singer = songDao.getBySingerName(oldItem.artist ?: "未知歌手")
        val singerDao = getSingerDao()
        if (singer.count == 0) {
            val newSinger = songDao.getBySingerName(song.artist!!)
            if (newSinger.count > 0) {
                singerDao.delete(singer.id)
            } else {
                singerDao.updateByName(oldItem.artist!!, song.artist)
            }
        } else {
            singerDao.upsert(SingerEntity(name = song.artist!!))
        }
    }
}

