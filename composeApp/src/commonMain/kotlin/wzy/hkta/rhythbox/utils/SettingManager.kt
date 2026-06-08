package wzy.hkta.rhythbox.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingManager {
    private val _showFloatLyric = MutableStateFlow(StorageManager.getItem("floatLyric") ?: false)
    val showFloatLyric = _showFloatLyric.asStateFlow()

    private val _pauseOnHeadsetDisconnect =
        MutableStateFlow(StorageManager.getItem("pauseOnHeadsetDisconnect") ?: true)
    val pauseOnHeadsetDisconnect = _pauseOnHeadsetDisconnect.asStateFlow()

    private val _isSystemInDarkTheme =
        MutableStateFlow(StorageManager.getItem("systemInDarkTheme") ?: true)
    val isSystemInDarkTheme = _isSystemInDarkTheme.asStateFlow()


    fun changeShowFloatLyric(show: Boolean) {
        StorageManager.setItem("floatLyric", show)
        _showFloatLyric.value = show
    }

    fun changePauseOnHeadsetDisconnect(state: Boolean) {
        StorageManager.setItem("pauseOnHeadsetDisconnect", state)
        _pauseOnHeadsetDisconnect.value = state
    }

    fun changeSystemInDarkTheme(state: Boolean) {
        StorageManager.setItem("systemInDarkTheme", state)
        _isSystemInDarkTheme.value = state
    }

}

