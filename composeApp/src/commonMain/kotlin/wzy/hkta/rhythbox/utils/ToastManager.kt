package wzy.hkta.rhythbox.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ToastManager {

    private var hostState: SnackbarHostState? = null

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private var snackbarJob: Job? = null

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _isShow = MutableStateFlow(false)
    val isShow = _isShow.asStateFlow()

    /**
     * 绑定 SnackbarHostState
     */
    fun bind(state: SnackbarHostState) {
        hostState = state
    }

    /**
     * 普通提示
     */
    fun show(message: String) {
        snackbarJob?.cancel()
        _loading.value = false
        _isShow.value = true
        snackbarJob = scope.launch {
            val host = hostState ?: return@launch
            host.currentSnackbarData?.dismiss()
            host.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            _isShow.value = false
        }
    }

    /**
     * 显示加载状态
     */
    fun showLoading(message: String = "加载中...") {
        snackbarJob?.cancel()
        _loading.value = true
        _isShow.value = true
        snackbarJob = scope.launch {
            val host = hostState ?: return@launch
            host.currentSnackbarData?.dismiss()
            host.showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
            _isShow.value = false
        }
    }

    /**
     * 隐藏加载状态
     */
    fun hideLoading() {
        _loading.value = false
        _isShow.value = false
        snackbarJob?.cancel()
        snackbarJob = null
        scope.launch {
            hostState?.currentSnackbarData?.dismiss()
        }
    }

    /**
     * 释放资源（通常不需要手动调用）
     */
    fun dispose() {
        hideLoading()
        hostState = null
        _isShow.value = false
    }
}
