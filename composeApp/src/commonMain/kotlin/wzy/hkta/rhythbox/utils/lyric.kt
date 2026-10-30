package wzy.hkta.rhythbox.utils

import com.google.common.graph.Network
import wzy.hkta.rhythbox.database.PlaylistSongEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.fatchMusic.Song


data class Lyric(
    val timeMillis: Int,
    val text: String,
)

data class LyricInfo(
    val lyric: List<Lyric>,
    val tlyric: List<Lyric>? = null,
    var offset: Int = 0,
)

fun parseLyric(lyric: String?): List<Lyric>? {
    if (lyric.isNullOrEmpty()) return null

    val list = mutableListOf<Lyric>()
    // 1. 先按换行符切成每一行
    val lyricItems = lyric.split("\n")

    for (lyricItem in lyricItems) {
        val trimmedItem = lyricItem.trim()
        // 🚨 兜底防闪退：如果这一行不包含时间戳标记，或者是空行，直接跳过
        if (!trimmedItem.startsWith("[") || !trimmedItem.contains("]")) continue

        try {
            val parts = trimmedItem.split("]", limit = 2)
            if (parts.size >= 2) {
                val timeStr = parts[0] // 这里拿到的是 "[00:12.050"（去掉了右括号）
                val lyricText = parts[1].trim() // 这里拿到的是真正的歌词文本

                // 过滤掉那些只有时间戳但没歌词的空行
                if (lyricText.isNotBlank()) {
                    // 🎯 第二刀：交给昨天写好的转换函数，把时间戳彻底转成 Int 毫秒
                    val timeMillis = parseTimeToMillis(timeStr)

                    // 组装成你的 Lyric 对象
                    list.add(Lyric(timeMillis = timeMillis, text = lyricText))
                }
            }
        } catch (e: Exception) {
            // 某一行解析失败也不要影响整首歌词的加载
            e.printStackTrace()
        }
    }

    // 🚀 最后按时间戳从小到大排个序，防止网易云返回乱序数据导致滚动抽风
    return list
        .sortedBy { it.timeMillis }
        .groupBy { it.timeMillis }
        .map { (_, lyrics) ->
            lyrics.first().copy(
                text = lyrics.joinToString("\n") { it.text }
            )
        }
}

private fun parseTimeToMillis(timeStr: String): Int {
    return try {
        // 1. 去掉左右的方括号，变成 "00:12.050"
        val cleanTime = timeStr.replace("[", "").replace("]", "").trim()

        // 2. 先用冒号切开分钟和秒 -> ["00", "12.050"]
        val minSecParts = cleanTime.split(":")
        val minutes = minSecParts[0].toInt()

        // 3. 再用点号切开秒和毫秒 -> ["12", "050"]
        val secMsParts = minSecParts[1].split(".")
        val seconds = secMsParts[0].toInt()

        // 4. 注意：有些老歌词的毫秒可能是两位数（比如 .05），我们要统一补齐或解析
        val msStr = secMsParts[1]
        val milliseconds = when (msStr.length) {
            1 -> msStr.toInt() * 100 // .5 -> 500ms
            2 -> msStr.toInt() * 10  // .05 -> 50ms
            else -> msStr.toInt()    // .050 -> 50ms
        }

        // 5. 算出总毫秒数：(分钟 * 60 + 秒) * 1000 + 毫秒
        ((minutes * 60 + seconds) * 1000) + milliseconds
    } catch (e: Exception) {
        e.printStackTrace()
        0 // 如果解析失败（脏数据），兜底返回 0 毫秒
    }
}