package wzy.hkta.rhythbox.utils

import com.aallam.openai.api.http.Timeout
import com.google.genai.Client
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.google.genai.types.HttpOptions
import com.google.genai.types.HttpRetryOptions
import wzy.hkta.rhythbox.expect.RhythboxPlayer
import wzy.hkta.rhythbox.expect.ScanMusic
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.expect.FloatLyric
import kotlin.time.Duration.Companion.seconds

const val GEMINI_KEY = "AQ.Ab8RN6KuS0VBFOvpzZRsYO-VrzgpP9EFSlRxiGKF3uua-JZRwg"
const val DEEP_SEEK_KEY = "sk-a1c27c0d425641c59e81806f9398f798"

object Rhythbox {
    private var _player: RhythboxPlayer? = null
    private var _dataBase: AppDatabase? = null
    private var _floatLyric: FloatLyric? = null
    var scanMusic: ScanMusic? = null
    private var _gemini: Client? = null
    private var _openAi: OpenAI? = null

    val dataBase: AppDatabase
        get() = checkNotNull(_dataBase) {
            "AppDatabase 未初始化"
        }
    val floatLyric: FloatLyric
        get() = checkNotNull(_floatLyric) {
            "FloatLyric 未初始化"
        }

    val player: RhythboxPlayer
        get() = checkNotNull(_player) {
            "RhythboxPlayer 未初始化"
        }

    val openAi: OpenAI
        get() = checkNotNull(_openAi) {
            "openai 未初始化"
        }

    fun init(
        rhythboxPlayer: RhythboxPlayer,
        appDatabase: AppDatabase,
        scan: ScanMusic,
        floatLyric: FloatLyric
    ) {
        _player = rhythboxPlayer
        _dataBase = appDatabase
        _floatLyric = floatLyric
        scanMusic = scan
        _gemini = initGeminiAi()
        _openAi = initOpenAi()
    }


    private fun initGeminiAi(): Client {
        val httpOptions =
            HttpOptions.builder().baseUrl("https://hkta.cc:8080/").timeout(1000 * 60).retryOptions(
                HttpRetryOptions.builder()
                    .attempts(3)
                    .httpStatusCodes(408, 429)
            ).build()
        return Client.builder().httpOptions(httpOptions)
            .apiKey(GEMINI_KEY).build()
    }


    private fun initOpenAi(): OpenAI {
        return OpenAI(
            OpenAIConfig(
                token = DEEP_SEEK_KEY,
                timeout = Timeout(socket = 60.seconds),
                host = OpenAIHost(
                    baseUrl = "https://api.deepseek.com"
                ),
            )
        )
    }


}


fun isCurrentPlaying(activeId: String?, targetId: String?): Boolean {
    return (activeId == targetId) && (activeId != null)
}