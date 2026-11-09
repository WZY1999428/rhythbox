package wzy.hkta.rhythbox.fatchMusic

import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import wzy.hkta.rhythbox.utils.StorageManager
import wzy.hkta.rhythbox.fatchMusic.FetchMusic.client
import wzy.hkta.rhythbox.fatchMusic.FetchMusic.getOrJson
import wzy.hkta.rhythbox.fatchMusic.FetchMusic.json

object User {

    suspend fun getUserInfo(): UserInfo? {
        return getOrJson<UserInfo>("user/userInfo")
    }

    suspend fun updateUserInfo(params: UserInfo): Boolean {
        val result = client.put("user/update") {
            contentType(ContentType.Application.Json)
            setBody(params)
        }
        val data = json.decodeFromString<Response<String>>(result.bodyAsText())
        if (data.code != 0) {
            throw Exception(data.message)
        }
        return true
    }

    suspend fun login(params: LoginRequest): Boolean {
        val result = client.post("user/login") {
            contentType(ContentType.Application.Json)
            setBody(params)
        }
        val data = json.decodeFromString<Response<String>>(result.bodyAsText())
        if (data.code != 0) {
            throw Exception(data.message)
        }
        StorageManager.setItem("token", data.data!!)
        return true
    }

    suspend fun savePlaylist(params: PlaylistFields<Song>): Boolean {
        val result = client.post("user-playlist/save") {
            contentType(ContentType.Application.Json)
            setBody(params)
        }
        val data = json.decodeFromString<Response<String>>(result.bodyAsText())
        if (data.code != 0) {
            throw Exception(data.message)
        }
        return true
    }

    suspend fun deletePlaylist(playlistId: Long): String? {
        val data = getOrJson<String?>("user-playlist/del") {
            url {
                parameters.append("playlistId", playlistId.toString())
            }
        }
        return data
    }


    suspend fun getUserPlaylist(): List<PlaylistFields<Song>>? {
        val data = getOrJson<List<PlaylistFields<Song>>>("user-playlist/list")
        return data
    }


    suspend fun addUserPlaylistSong(songId: Long, playlistId: Long): String? {
        val data = getOrJson<String?>("user-playlist/addSong") {
            url {
                parameters.append("songId", songId.toString())
                parameters.append("playlistId", playlistId.toString())
            }
        }
        return data
    }


    suspend fun deletePlaylistSong(songId: Long, playlistId: Long): String? {
        val data = getOrJson<String?>("user-playlist/delSong") {
            url {
                parameters.append("songId", songId.toString())
                parameters.append("playlistId", playlistId.toString())
            }
        }
        return data
    }


    suspend fun getPlayListInfo(playlistId: Long): PlaylistFields<Song>? {
        val data = getOrJson<PlaylistFields<Song>>("user-playlist/$playlistId") {
        }
        return data
    }

    suspend fun getPlayListSong(playlistId: Long): List<Song> {
        val data = getOrJson<List<Song>>("user-playlist/song") {
            url {
                parameters.append(
                    "playlistId",
                    playlistId.toString()
                )
            }
        }!!
        return data.toNetSongs("$playlistId-user-playlist-song")
    }

}