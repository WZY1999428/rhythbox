package wzy.hkta.rhythbox.utils.netEaseCloudMusic

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import wzy.hkta.rhythbox.BuildConfig.NET_EASE_CLOUD_MUSIC
import wzy.hkta.rhythbox.expect.LyricStatus
import wzy.hkta.rhythbox.database.LyricEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.LyricInfo
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.parseLyric
import wzy.hkta.rhythbox.utils.retry

object NetEaseCloudMusic {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient {
        install(Logging)
        install(HttpCookies)
        defaultRequest {
            url(NET_EASE_CLOUD_MUSIC)
            header(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36"
            )
        }
    }

    suspend fun getLyric(song: SongEntity): LyricStatus {
        if (isPureMusic(song.name)) return LyricStatus.NotFound
        val data = Rhythbox.dataBase.getSongDao().getLyric("local-${song.id}")
        if (data != null) return LyricStatus.Finished(
            LyricInfo(
                parseLyric(data.lyric)!!,
                parseLyric(data.transLyric),
                data.offset
            )
        )
        val artist = song.artist?.takeUnless { it == "未知歌手" }
        val res = client.get("search/get/web?s=${song.name} ${artist ?: ""}&type=1&limit=10")
        val text = res.bodyAsText()
        val result = json.decodeFromString<SearchResult>(text)
        val songs = result.result.songs
        if (songs.isNullOrEmpty()) return LyricStatus.NotFound
        val id = generateContentWithOpenAi(
            "${song.name} ${if (artist != null) "  歌手：$artist" else ""} 时长：${song.duration}",
            songs
        )
        if (id <= 0) return LyricStatus.NotFound
        val lyricList = loadLyric(id)
        if (!lyricList.isNullOrEmpty()) {
            val lyric = lyricList[0]!!
            val tlyric = lyricList.getOrNull(1)
            Rhythbox.dataBase.getSongDao().insertLyric(
                LyricEntity(
                    songId = "local-${song.id}",
                    lyric = lyric,
                    transLyric = tlyric
                )
            )
            return LyricStatus.Finished(LyricInfo(parseLyric(lyric)!!, parseLyric(tlyric)))
        }
        return LyricStatus.NotFound
    }

    private suspend fun generateContentWithOpenAi(currentSong: String, songList: List<Song>): Long {
        val completion = retry(3) {
            Rhythbox.openAi.chatCompletion(
                ChatCompletionRequest(
                    model = ModelId("deepseek-v4-flash"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = buildChatPrompt(currentSong, songList)
                        )
                    )
                )
            )
        }
        val text = completion.choices.first().message.content
            ?.replace(Regex("^```\\w*\\s*|\\s*```$"), "")
            ?.trim()
        if (text != null) {
            val res = json.decodeFromString<AIResult>(text)
            return res.id ?: -1
        }
        return -1
    }

    private fun buildChatPrompt(currentSong: String, songList: List<Song>): String {
        val songListStr = json.encodeToString(songList.map {
            AISong(
                id = it.id,
                duration = it.duration,
                name = it.name,
                album = it.album?.name,
                artists = it.artists?.map { ar -> ar.name }
            )
        })

        val str = """
                    # Role
               你是一个音乐曲库匹配引擎。
                        你的任务是：
                        根据【当前播放歌曲】和【候选歌曲列表】，
                        从候选列表中选出最匹配的一首歌曲。
                        你必须严格按照以下规则执行，不允许自行发挥。
                        【匹配优先级】
                        
                        第一层（最高优先级）
                        1. 歌名完全一致
                        如果存在歌名完全一致的候选歌曲：
                        必须只在这些歌曲中继续筛选。
                        禁止跳过这些歌曲去选择其它歌曲。
                        第二层
                        2. 歌手完全一致
                        第三层
                        3. 时长最接近
                        优先选择时长差值最小的歌曲。
                        第四层
                        4. 专辑名称相关
                        第五层
                        5. 风格、流派、情感相近
                        --------------------------------------------------
                        【降权规则】
                        如果歌曲名、专辑名、歌手名中出现以下关键词：
                        cover
                        翻唱
                        instrumental
                        piano
                        guitar
                        remix
                        live
                        八音盒
                        则视为衍生版本。
                        当存在非衍生版本时：
                        衍生版本必须降级。
                        --------------------------------------------------
                        【同歌手规则】
                        仅当不存在歌名完全一致的歌曲时，
                        才允许优先考虑同歌手歌曲。
                        --------------------------------------------------
                        【放弃规则】
                        如果候选歌曲与当前歌曲明显无关，
                        或者无法合理判断最优结果：
                        返回：
                        {"id": null}
                        禁止猜测。
                        禁止随机选择。
                        禁止为了返回结果而强行推荐。
                        --------------------------------------------------
                        【输入数据】
                        当前播放歌曲： $currentSong
                        候选歌曲列表：
                        $songListStr
                        --------------------------------------------------
                        【输出要求】
                        只能输出合法 JSON。
                        正确格式：
                        {"id":123456}
                        或
                        {"id":null}
                        禁止输出解释。
                        禁止输出备注。
                        禁止输出 Markdown。
                        禁止输出多余字符。
                    """
        return str
    }

    private suspend fun loadLyric(id: Long): List<String?>? {
        val text = client.get("song/lyric?id=$id&lv=1&kv=1&tv=-1").bodyAsText()
        val result = json.decodeFromString<NetEaseLyricResponse>(text)
        if (result.code != 200) return null
        if (result.lrc?.lyric.isNullOrBlank()) return null
        return listOf(result.lrc.lyric, result.tlyric?.lyric)
    }
}


private fun isPureMusic(songName: String): Boolean {
    val text = songName.lowercase()
    // 1. 检查网易云歌词里是否包含纯音乐的文本占位符
    if (text.contains("纯音乐") || text.contains("无歌词") || text.contains("instrumental")) {
        return true
    }
    // 2. 兜底策略：如果歌词只有一两行，且歌名里带有 "without"、"off vocal"、"instrumental"、"伴奏"
    return text.contains("without") || text.contains("off vocal") || text.contains("instrumental") || text.contains(
        "伴奏"
    )
    // 歌名都说了是伴奏，而且歌词确实没什么内容，直接断定为纯音乐
}
