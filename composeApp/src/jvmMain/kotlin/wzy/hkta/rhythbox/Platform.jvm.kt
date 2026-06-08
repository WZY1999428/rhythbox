package wzy.hkta.rhythbox

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import wzy.hkta.rhythbox.database.AlbumEntity
import wzy.hkta.rhythbox.database.DirEntity
import wzy.hkta.rhythbox.database.SingerEntity
import wzy.hkta.rhythbox.database.SongEntity

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual class ScanMusic {
    actual val dir: StateFlow<List<DirItem>> = MutableStateFlow(emptyList())
    actual val songList: StateFlow<List<SongItem>> = MutableStateFlow(emptyList())
    actual val singer: StateFlow<List<SingerItem>> = MutableStateFlow(emptyList())
    actual val album: StateFlow<List<AlbumItem>> = MutableStateFlow(emptyList())

    actual suspend fun scan() {
    }

    actual suspend fun save() {

    }

    actual val currentPath: MutableState<String>
        get() = TODO("Not yet implemented")
}