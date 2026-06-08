package wzy.hkta.rhythbox.expect


import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import wzy.hkta.rhythbox.services.FloatLyricService
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import wzy.hkta.rhythbox.utils.SettingManager

actual class FloatLyric(private val activity: ComponentActivity) {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    hideLyric(false)
                }
                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    val shouldShow = SettingManager.showFloatLyric.value
                    if (shouldShow && Settings.canDrawOverlays(activity)) {
                        activity.startService(getIntent())
                    }
                }
            }
        )
    }

    private val overlayPermissionLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // 从设置页返回后的回调
            if (Settings.canDrawOverlays(activity)) {
                onPermissionGranted?.invoke()
            }
        }

    // 提供一个权限成功后的回调供外部监听
    var onPermissionGranted: (() -> Unit)? = null


    fun getIntent(): Intent {
        return Intent(activity, FloatLyricService::class.java)
    }

    actual fun showLyric(): Boolean {
        if (Settings.canDrawOverlays(activity)) {
            SettingManager.changeShowFloatLyric(true)
            return true
        } else {
            Toast.makeText(activity, "需要显示在其他应用上层才能显示歌词", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = "package:${activity.packageName}".toUri()
            }
            overlayPermissionLauncher.launch(intent)
            SettingManager.changeShowFloatLyric(true)
            return false
        }
    }

    actual fun hideLyric(close: Boolean) {
        if (close) {
            SettingManager.changeShowFloatLyric(false)
        }
        activity.stopService(getIntent())
    }

}