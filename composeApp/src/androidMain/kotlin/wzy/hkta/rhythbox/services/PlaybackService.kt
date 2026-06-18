package wzy.hkta.rhythbox.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.content.IntentCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures.immediateFuture
import com.google.common.util.concurrent.ListenableFuture
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import wzy.hkta.rhythbox.MainActivity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SettingManager

const val CUSTOM_ACTION_PREV = "wzy.hkta.rhythbox.ACTION_PREV"
const val CUSTOM_ACTION_NEXT = "wzy.hkta.rhythbox.ACTION_NEXT"

class PlaybackService : MediaSessionService() {
    private val isAndroid80 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    private var mediaSession: MediaSession? = null
    private var noisyReceiver: BroadcastReceiver? = null

    // 1. 定义你的自定义按钮（由于你只有一首歌，系统默认的上下首用不了，必须用自定义的）


    // 💡 当服务启动时触发
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, buildPlayer())
            .setCallback(CustomMediaSessionCallback())
            //App 主 Activity（比如 MainActivity）的 PendingIntent，然后通过 MediaSession.Builder 的 setSessionActivity() 方法传给它
            .setSessionActivity(buildPendingIntent())
            .setId("rhythbox")
            .build()


          /*
          * 在 Android 系统中，当耳机（不管是 3.5mm 有线耳机还是蓝牙耳机）被拔出或断开连接时，
          * 系统音频流会突然从“耳机”切换回“手机扬声器”。
          * 为了防止在公共场合发生耳机一拔、扬声器突然大声外放音乐的尴尬社交名场面，Android 官方提供了这个机制。
          * */
        noisyReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    if (SettingManager.pauseOnHeadsetDisconnect.value) {
                        Rhythbox.player.pause()
                    }
                }
            }
        }

        registerReceiver(
            noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
    }
   /*
       在 Media3 中，点击通知栏（或者点击系统播放控制中心）需要能够唤醒并打开 App 的界面。
       这个功能是通过向 MediaSession 提供一个 PendingIntent 来实现的。
       创建一个指向你 App 主 Activity（比如 MainActivity）的 PendingIntent，
       然后通过 MediaSession.Builder 的 setSessionActivity() 方法传给它。
   * */
    private fun buildPendingIntent(): PendingIntent {
        // 🛠️ 1. 创建点击通知栏时要打开的 Intent
        val intent = Intent(this, MainActivity::class.java).apply {
            // 如果你的 Activity 已经在前台，避免重复创建多个实例，复用当前实例
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        // 🛠️ 2. 将 Intent 包装成 PendingIntent
        // 注意：Android 12 (API 31) 以上必须显式指定 PendingIntent.FLAG_IMMUTABLE 或 FLAG_MUTABLE
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(this, 0, intent, flags)
    }


    @OptIn(UnstableApi::class)
    private fun buildPlayer(): ForwardingPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val player = ExoPlayer.Builder(this).apply {
            setAudioAttributes(audioAttributes, true)
        }.build()

        //🛠️ 用这个包装层，把系统默认按钮的点击事件，强行导流到你自己的播放器
        val forwardingPlayer = object : ForwardingPlayer(player) {
            // 拦截通知栏“上一首”标准动作
            override fun seekToPrevious() {
                // 放弃 ExoPlayer 内部的 3秒判断，直接执行你的切歌
                if (isAndroid80) Rhythbox.player.prev()
            }

            // 拦截通知栏“下一首”标准动作
            override fun seekToNext() {
                if (isAndroid80) Rhythbox.player.next()
            }

            // 拦截多媒体按键或某些系统控制的标准上一首
            override fun seekToPreviousMediaItem() {
                if (isAndroid80) Rhythbox.player.prev()
            }

            // 拦截多媒体按键或某些系统控制的标准下一首
            override fun seekToNextMediaItem() {
                if (isAndroid80) Rhythbox.player.next()
            }
        }
        return forwardingPlayer

    }


    @UnstableApi
    private inner class CustomMediaSessionCallback : MediaSession.Callback {


        // 2. 在这里接收通知栏的点击事件，然后调用你自己的列表管理器
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CUSTOM_ACTION_PREV -> {
                    Rhythbox.player.prev()
                    return immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                CUSTOM_ACTION_NEXT -> {
                    Rhythbox.player.next()

                    return immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            val keyEvent =
                IntentCompat.getParcelableExtra(
                    intent,
                    Intent.EXTRA_KEY_EVENT,
                    KeyEvent::class.java
                )
            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        println("【耳机物理键】监听到下一首")
                        Rhythbox.player.next()
                        return true
                    }

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        println("【耳机物理键】监听到上一首")
                        Rhythbox.player.prev()
                        return true
                    }
                }
            }
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }

    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // 💡 别忘了在销毁时释放，防止内存泄漏
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        unregisterReceiver(noisyReceiver)
        mediaSession = null
        super.onDestroy()
    }
}

