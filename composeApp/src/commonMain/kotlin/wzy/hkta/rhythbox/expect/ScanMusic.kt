package wzy.hkta.rhythbox.expect

import kotlinx.coroutines.flow.StateFlow
import wzy.hkta.rhythbox.utils.LyricInfo


expect class ScanMusic {
    val dir: StateFlow<MutableMap<String, DirItem>>
    val progress: StateFlow<Float>
    val scanStatus: StateFlow<ScanStatus>
    val currentPath: StateFlow<String>
    suspend fun scan()
    fun cancel()
    fun reset()
    suspend fun save(paths: List<String>)

    fun openBrowser(url: String) 
}


expect fun editSongInfo(
    path: String,
    name: String,
    album: String,
    artist: String
)


enum class ScanStatus {
    Cancel,
    ScanIng,
    WaitIng,
    Finished,
    SaveIng,
    SaveFinished
}

sealed class LyricStatus {
    data object Init : LyricStatus()
    data object Loading : LyricStatus()
    data class Finished(val data: LyricInfo) : LyricStatus()
    data class LoadErr(val msg: String) : LyricStatus()
    data object NotFound : LyricStatus()
}
