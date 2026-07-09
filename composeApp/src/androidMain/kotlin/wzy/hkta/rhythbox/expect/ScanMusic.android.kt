package wzy.hkta.rhythbox.expect

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import wzy.hkta.rhythbox.database.AlbumEntity
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.database.DirEntity
import wzy.hkta.rhythbox.database.LyricEntity
import wzy.hkta.rhythbox.database.SingerEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.md5
import java.io.File
import java.io.FileOutputStream
import kotlin.String

private val ROOT_PATH = File("/storage/emulated/0")
private val blacklistKeywords = listOf("录音", "call", "record", "audio_", "voice_")

actual class ScanMusic(private val context: Context, database: AppDatabase) {

    private val _dir = MutableStateFlow<MutableMap<String, DirItem>>(mutableMapOf())
    private val _progress = MutableStateFlow(0f)
    private val _currentPath = MutableStateFlow("")
    private val _scanStatus = MutableStateFlow(ScanStatus.WaitIng)
    actual val currentPath = _currentPath.asStateFlow()
    actual val dir = _dir.asStateFlow()
    actual val progress = _progress.asStateFlow()
    actual val scanStatus = _scanStatus.asStateFlow()

    private val dirDao = database.getDirDao()
    private val songDao = database.getSongDao()
    private val albumDao = database.getAlbumDao()
    private val singerDao = database.getSingerDao()


    actual suspend fun scan() {
        if (_scanStatus.value != ScanStatus.WaitIng) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    "package:${context.packageName}".toUri()
                )
                context.startActivity(intent)
                return
            }
        }
        _scanStatus.value = ScanStatus.ScanIng
        withContext(Dispatchers.IO) {
            val list = ROOT_PATH.walk()
                .filter { it.isFile }
            val songs =
                list.filter { it.extension in listOf("mp3", "flac", "ogg", "m4a", "aac", "wav") }
            val lyrics = list.filter { it.extension in listOf("lrc") }
//            .map {
//                LyricEntity(songId = it., lyric = it.readText(Charsets.UTF_8))
//            }.toList()


            var len = songs.count().toFloat()
            for ((index, file) in songs.withIndex()) {
                if (scanStatus.value == ScanStatus.Cancel) {
                    break
                }
                if (!scanMusic(file, lyrics)) {
                    len--
                    _progress.value = (index.toFloat() / len)
                    continue
                }

                _progress.value = (index.toFloat() / len)
            }
            if (scanStatus.value != ScanStatus.Cancel) {
                _progress.value = 1f
                _scanStatus.value = ScanStatus.Finished
            }

        }
    }

    private fun scanMusic(file: File, lyrics: Sequence<File>): Boolean {
        try {
            val uri = Uri.fromFile(file)
            val r = MediaMetadataRetriever()
            r.setDataSource(context, uri)
            val duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = duration?.toLongOrNull() ?: 0L
            val title = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val fileName = file.nameWithoutExtension
            _currentPath.value = file.absolutePath
            if (!filterLegitimateMusic(
                    durationMs,
                    fileName,
                    title,
                    file.absolutePath.lowercase()
                )
            ) return false

            val artist = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val albumName = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val parent = File(file.parent!!)
            val songList =
                (_dir.value[parent.absolutePath]?.songList ?: mutableListOf()).apply {
                    val cover = getSongCover(file.absolutePath)
                    add(
                        SongItem(
                            name = title ?: fileName,
                            artist = artist ?: "未知歌手",
                            duration = durationMs,
                            dirId = parent.absolutePath,
                            cover = cover,
                            album = albumName,
                            path = uri.toString(),
                            size = file.length(),
                        )
                    )
                }



            _dir.value = _dir.value.toMutableMap().apply {
                this[parent.absolutePath] = DirItem(
                    name = parent.name,
                    fullPath = parent.absolutePath,
                    songList = songList
                )
            }
            r.release()
        } catch (e: Exception) {
            println(e)
            return false
        }
        return true
    }

    private fun getSongCover(path: String): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            // 1. 设置要解析的音频文件路径
            val tempDir = File(context.cacheDir, "RhythboxTemp")
            if (!tempDir.exists()) {
                tempDir.mkdirs() // 文件夹不存在就人肉建一个
            }

            val file = File(tempDir, path.md5())
            if (file.exists() && file.isFile) {
                return file.absolutePath
            }
            retriever.setDataSource(path)
            val pictureBytes: ByteArray? = retriever.embeddedPicture
            if (pictureBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size)
                bitmap?.asImageBitmap()
                val tempFile = File(tempDir, path.md5())
                // 💡 3. 开辟文件流，把内存里的像素狠狠拍进硬盘里
                FileOutputStream(tempFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                }
                // 💡 4. 吃完饭记得擦嘴，立刻回收 Native C++ 层的 Bitmap 内存，防止扫描 OOM
                bitmap.recycle()
                return tempFile.absolutePath
            } else {
                null // 歌曲压根没内嵌封面
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null // 路径不对或者文件损坏时安全降级
        } finally {
            retriever.release()
        }
    }

    actual fun reset() {
        _dir.value.clear()
        _scanStatus.value = ScanStatus.WaitIng
        _progress.value = 0f
        _currentPath.value = ""
    }

    actual fun cancel() {
        _scanStatus.value = ScanStatus.Cancel
    }

    actual suspend fun save(paths: List<String>) {
        if (scanStatus.value == ScanStatus.SaveIng) return
        _scanStatus.value = ScanStatus.SaveIng
        val oldSongSet = mutableSetOf<String>()
        songDao.find().first().forEach { oldSongSet.add(it.path) }
        singerDao.clear()
        albumDao.clear()
        dirDao.clear()
        val singerEntityList = mutableListOf<SingerEntity>()
        val albumEntityList = mutableListOf<AlbumEntity>()
        val dirEntityList = mutableListOf<DirEntity>()
        val songEntityList = mutableListOf<SongEntity>()

        for (path in paths) {
            val item = _dir.value[path]
            if (item != null) {
                for (song in item.songList) {
                    val artist = song.artist ?: "未知歌手"
                    val album = song.album ?: "其他"
                    if (song.path in oldSongSet) {
                        oldSongSet.remove(song.path)
                    } else {
                        songEntityList.add(
                            SongEntity(
                                name = song.name,
                                artist = artist,
                                duration = song.duration,
                                dirId = song.dirId,
                                cover = song.cover,
                                album = song.album ?: "其他",
                                path = song.path,
                                size = song.size,
                            )
                        )
                    }
                    singerEntityList.add(SingerEntity(name = artist))
                    albumEntityList.add(AlbumEntity(name = album, artist = artist))
                }
                dirEntityList.add(DirEntity(name = item.name, fullPath = item.fullPath))
            }
        }
        songDao.insertAll(songEntityList)
        if (!oldSongSet.isEmpty()) songDao.deleteByPaths(oldSongSet.toList())
        singerDao.insertAll(singerEntityList)
        albumDao.insertAll(albumEntityList)
        dirDao.insertAll(dirEntityList)
        _scanStatus.value = ScanStatus.SaveFinished
    }

    actual fun openBrowser(url: String) {
        try {
            // 1. 创建一个查看（VIEW）动作的 Intent
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // 2. 拉起默认浏览器
            context.startActivity(intent)
        } catch (e: Exception) {
            // 防御性编程：万一用户的奇葩手机里连一个浏览器都没有，捕获异常防止崩溃
            e.printStackTrace()
        }
    }
}

private fun filterLegitimateMusic(
    durationMs: Long,
    fileName: String,
    title: String?,
    absolutePath: String
): Boolean {
    if (durationMs < 60 * 1000) {
        println("【过滤】时长过短 (${durationMs / 1000}秒)，跳过: $fileName")
        return false
    }
    val isBlacklisted = blacklistKeywords.any { fileName.contains(it) }
    // 如果没有内置 Title，并且文件名命中黑名单，直接丢弃
    if (title.isNullOrBlank() && isBlacklisted) {
        println("【过滤】命中录音黑名单关键词，跳过: $fileName")
        return false
    }
    if (absolutePath.contains("/record") || absolutePath.contains("/soundrecorder")) {
        println("【过滤】位于系统录音专属目录，跳过: $fileName")
        return false
    }
    return true
}


actual fun editSongInfo(
    path: String,
    name: String,
    album: String,
    artist: String,
) {
    val audioFile = AudioFileIO.read(File(path.toUri().path!!))
    val tag = audioFile.tagOrCreateAndSetDefault
    tag.setField(FieldKey.TITLE, name)
    tag.setField(FieldKey.ARTIST, artist)
    tag.setField(FieldKey.ALBUM, album)
    println(tag)
    audioFile.commit()
}