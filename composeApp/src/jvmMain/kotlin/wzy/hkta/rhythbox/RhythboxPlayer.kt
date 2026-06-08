package wzy.hkta.rhythbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// androidMain

actual class RhythboxPlayer() {

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    private val _currentPosition = MutableStateFlow<Long>(0)
    private val _duration = MutableStateFlow<Long>(0)

    actual val isPlaying = _isPlaying.asStateFlow()
    actual val currentPosition = _currentPosition.asStateFlow()
    actual val duration = _duration.asStateFlow()


    actual fun prepare(mediaItemIndex: Int) {
    }

    actual fun play() {
    }

    actual fun pause() {
    }

    actual fun seekTo(position: Long) {
    }

    actual fun release() {
    }


    actual val progress: StateFlow<Float>
        get() = TODO("Not yet implemented")


}