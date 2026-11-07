package wzy.hkta.rhythbox.expect

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import java.io.File

/**
 * 下载音乐到临时缓存目录
 * @param context Android 上下文
 * @param url 音乐文件的网络直链
 * @param fileName 保存的自定义文件名（例如：song_123.mp3）
 */
suspend fun downloadMusicToCache(
    context: Context,
    url: String,
    fileName: String
): File? {
    // 切换到 IO 线程执行文件写入操作
    return withContext(Dispatchers.IO) {
        try {
            // 1. 在应用的 cache 目录下创建目标文件
            val cacheDir = context.cacheDir
            val musicFile = File(cacheDir, fileName)

            // 2. 使用 Ktor 发起网络请求获取数据
            val response: HttpResponse = FetchMusic.client.get(url)
            // 3. 将响应体转换为字节数组并写入文件
            val bytes: ByteArray = response.body<ByteArray>()
            musicFile.writeBytes(bytes)

            // 返回下载好的文件对象
            musicFile
        } catch (e: Exception) {
            e.printStackTrace()
            null // 下载失败返回 null
        }
    }
}