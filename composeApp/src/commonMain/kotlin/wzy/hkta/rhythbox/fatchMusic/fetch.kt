package wzy.hkta.rhythbox.fatchMusic

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import wzy.hkta.rhythbox.utils.UserManager
import wzy.hkta.rhythbox.utils.generateNetMediaId
import wzy.hkta.rhythbox.utils.UserManager.getToken

const val MUSIC_BASE_URL = "http://192.168.0.195:8848"

object FetchMusic {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    val client = HttpClient {
        install(Logging)
        install(HttpCookies)
        install(ContentNegotiation) { json(json) }
        install(TokenPlugin)
        defaultRequest {
            url("$MUSIC_BASE_URL/api/")
            header(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36"
            )
        }
    }


    val user = User
    val music = Music

    suspend inline fun <reified T> getOrJson(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T? {
        val result = client.get(url) { block() }
        val res = json.decodeFromString<Response<T>>(result.bodyAsText())
        if (res.code == 0) {
            return res.data
        } else {
            if (res.code == 401) {
                val msg = if (getToken() != null) "登录已过期，请重新登录" else "请先登录"
                UserManager.removeToken()
                throw Exception(msg)
            }
            throw Exception(res.message)
        }
    }

    suspend fun updateAvatar(bytes: ByteArray, filename: String): UploadAvatar {
        val result = client.post("upload/avatar") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "avatar", bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                            }
                        )
                    }
                )
            )
        }
        val data = json.decodeFromString<Response<UploadAvatar>>(result.bodyAsText())
        return data.data!!
    }

    suspend fun updateCover(bytes: ByteArray, filename: String): Upload {
        val result = client.post("upload/cover") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file", bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                            }
                        )
                    }
                )
            )
        }
        val data = json.decodeFromString<Response<Upload>>(result.bodyAsText())
        return data.data!!
    }
}


internal fun List<Song>.toNetSongs(key: String = ""): List<Song> {
    return map { song ->
        song.copy(
            cover = "$MUSIC_BASE_URL/${song.cover}",
            url = "$MUSIC_BASE_URL/api/music/song/url?songId=${song.id}",
            mediaId = generateNetMediaId(key, song),
            duration = song.duration * 1000
        )
    }
}

internal fun List<Playlist>.toNetPlaylist(): List<Playlist> {
    return map { song ->
        song.copy(
            cover = song.cover?.let { "$MUSIC_BASE_URL/$it" },
        )
    }
}

internal fun List<Album>.toNetAlbum(): List<Album> {
    return map { song ->
        song.copy(
            cover = song.cover?.let { "$MUSIC_BASE_URL/$it" }
        )
    }
}

