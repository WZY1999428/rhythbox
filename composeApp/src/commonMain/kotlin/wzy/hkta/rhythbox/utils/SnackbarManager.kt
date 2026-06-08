package wzy.hkta.rhythbox.utils

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object SnackbarManager {
    private var hostState: SnackbarHostState? = null
    private var snackbarJob: Job? = null
    fun bind(state: SnackbarHostState) {
        hostState = state
    }

    fun show(message: String) {
        snackbarJob?.cancel()
        snackbarJob = CoroutineScope(Dispatchers.Main).launch {
            hostState?.currentSnackbarData?.dismiss()
            hostState?.showSnackbar(message)
        }
    }

}