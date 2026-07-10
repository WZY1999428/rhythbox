package wzy.hkta.rhythbox.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import wzy.hkta.rhythbox.database.SongEntity
import java.security.MessageDigest
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import wzy.hkta.rhythbox.database.SortTypes
import kotlin.time.Instant

fun formatDate(time: Long): String {
    try {
        val instant = Instant.fromEpochMilliseconds(time)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.year}-${dt.month.number.toString().padStart(2, '0')}-${
            dt.day.toString().padStart(2, '0')
        }"
    } catch (e: Exception) {
        return ""
    }
}

fun formatDuration(duration: Long): String {
    val duration = duration / 1000;
    val hh = duration / 3600
    val mm = (duration % 3600) / 60
    val ss = duration % 60
    return if (hh > 0) {
        "%02d:%02d:%02d".format(hh, mm, ss)
    } else {
        "%02d:%02d".format(mm, ss)
    }
}

fun String.md5(): String {
    // 1. 拿到 MD5 实例
    val md = MessageDigest.getInstance("MD5")
    // 2. 将字符串转成字节数组并计算哈希，拿到 16 字节的 ByteArray
    val digest = md.digest(this.toByteArray(Charsets.UTF_8))

    // 3. 格式化为标准的 32 位 16 进制小写字符串
    return digest.joinToString("") { String.format("%02x", it) }
}

fun generateMediaId(key: String, song: SongEntity): String {
    return "${key}_${song.name}_${song.size}_${song.path}"
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.marqueeText(): Modifier {
    return this.basicMarquee(
        iterations = Int.MAX_VALUE, // ♾️ 无线循环滚动
        repeatDelayMillis = 500,        // ⏱️ 歌变了或者复位后，停顿2秒再开始走马灯
        initialDelayMillis = 1000, // 🚀 第一次加载完，停顿1秒开始动
        velocity = 40.dp           // 🏃‍♂️ 滚动的速度（每秒滚动多少 dp）
    )
}


fun getSortType(savedType: String?): String {
    return when (savedType) {
        SortTypes.BY_NAME -> "name ASC"
        SortTypes.BY_NAME_DESC -> "name DESC"
        SortTypes.BY_TIME -> "createTime ASC"
        SortTypes.BY_TIME_DESC -> "createTime DESC"
        SortTypes.BY_ARTIST -> "artist ASC"
        SortTypes.BY_ARTIST_DESC -> "artist DESC"
        else -> "name ASC"
    }
}

suspend fun <T> retry(
    times: Int = 3,
    block: suspend () -> T
): T {
    var lastException: Exception? = null

    repeat(times) {
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            delay(1000L * (it + 1))
        }
    }

    throw lastException!!
}