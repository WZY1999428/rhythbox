package wzy.hkta.rhythbox.services

import android.annotation.SuppressLint
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
import androidx.media3.common.Player.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures.immediateFuture
import com.google.common.util.concurrent.ListenableFuture
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import wzy.hkta.rhythbox.R
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SettingManager

const val CUSTOM_ACTION_PREV = "wzy.hkta.rhythbox.ACTION_PREV"
const val CUSTOM_ACTION_NEXT = "wzy.hkta.rhythbox.ACTION_NEXT"

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var noisyReceiver: BroadcastReceiver? = null

    // 💡 当服务启动时触发
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val player = ExoPlayer.Builder(this).apply {
            setAudioAttributes(audioAttributes, true)
        }.build()
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(CustomMediaSessionCallback())
            .setId("rhythbox")
            .build()

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


@UnstableApi
private class CustomMediaSessionCallback : MediaSession.Callback {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMediaButtonEvent(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        intent: Intent
    ): Boolean {
        val keyEvent =
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
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

    @SuppressLint("WrongConstant")
    @OptIn(UnstableApi::class)
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ConnectionResult {

        // 1. 定义你的自定义按钮（由于你只有一首歌，系统默认的上下首用不了，必须用自定义的）
        val customPrevButton = CommandButton.Builder(R.drawable.prev2)
            .setDisplayName("上一首")
            .setCustomIconResId(R.drawable.prev2) // 可以使用media3自带的图标
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_PREV, Bundle.EMPTY))
            .setSlots(COMMAND_SEEK_TO_PREVIOUS)
            .build()

        val customNextButton = CommandButton.Builder(R.drawable.next2)
            .setDisplayName("下一首")
            .setCustomIconResId(R.drawable.next2)
            .setSlots(COMMAND_SEEK_TO_NEXT)
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_NEXT, Bundle.EMPTY))
            .build()

        // 2. 允许这些自定义命令
        val availableSessionCommands = ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
            .add(SessionCommand(CUSTOM_ACTION_PREV, Bundle.EMPTY))
            .add(SessionCommand(CUSTOM_ACTION_NEXT, Bundle.EMPTY))
            .build()

        val availablePlayerCommands = ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
            .remove(COMMAND_SEEK_TO_NEXT)
            .remove(COMMAND_SEEK_TO_PREVIOUS)
            .build()

        // 3. 将自定义按钮布局塞进连接结果中
        return AcceptedResultBuilder(session)
            .setAvailableSessionCommands(availableSessionCommands)
            .setAvailablePlayerCommands(availablePlayerCommands)
            .setCustomLayout(
                ImmutableList.of(
                    customPrevButton,
                    customNextButton,
                )
            )
            .build()
    }

    // 2. 在这里接收通知栏的点击事件，然后调用你自己的列表管理器
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        println(customCommand.customAction)
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
}

