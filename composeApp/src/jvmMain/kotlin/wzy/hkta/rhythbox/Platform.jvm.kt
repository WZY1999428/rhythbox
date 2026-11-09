package wzy.hkta.rhythbox

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import wzy.hkta.rhythbox.expect.DirItem
import wzy.hkta.rhythbox.expect.ScanStatus

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual class ScanMusic {
    private val _dir = MutableStateFlow<MutableMap<String, DirItem>>(mutableMapOf())
    private val _progress = MutableStateFlow(0f)
    private val _scanStatus = MutableStateFlow(ScanStatus.WaitIng)
    private val _currentPath = MutableStateFlow("")

    actual val dir: StateFlow<MutableMap<String, DirItem>> = _dir.asStateFlow()
    actual val progress: StateFlow<Float> = _progress.asStateFlow()
    actual val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()
    actual val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    actual suspend fun scan() {}
    actual fun cancel() {}
    actual fun reset() {}
    actual suspend fun save(paths: List<String>) {}
    actual fun openBrowser(url: String) {}
}

@Composable
actual fun GetCover(cb: (String?) -> Unit) {
}