package wzy.hkta.rhythbox.utils.fatchMusic

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import wzy.hkta.rhythbox.utils.generateNetMediaId
import wzy.hkta.rhythbox.database.AppDatabase
import wzy.hkta.rhythbox.database.SongCache
import wzy.hkta.rhythbox.database.SongEntity

const val MUSIC_BASE_URL = "http://192.168.0.195:8848"

object FetchMusic {
    val json = Json {
        ignoreUnknownKeys = true
    }
    val client = HttpClient {
        install(Logging)
        install(HttpCookies)
        defaultRequest {
            url("$MUSIC_BASE_URL/api/")
            header(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36"
            )
        }
    }


    suspend inline fun <reified T> getOrJson(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T? {
        val result = client.get(url) { block() }
        val res = json.decodeFromString<Response<T>>(result.bodyAsText())
        if (res.code == 0) {
            return res.data
        } else {
            throw Exception(res.message)
        }
    }


    suspend fun fetchHome(): HomeData {
        return getOrJson<HomeData>("music/home")!!
    }

    suspend fun fetchRecommendationsDaily(): List<Song> {
        val data = getOrJson<List<Song>>("music/recommendations/daily")!!
        return data.toNetSongs("recommendations-daily")
    }

    suspend fun getLeaderboard(): List<Song> {
        val data = getOrJson<List<Song>>("music/recommendations/leaderboard")!!
        return data.toNetSongs("recommendations-leaderboard")
    }

    suspend fun getPlaylistFeatured(cate: String): List<Song> {
        val data = getOrJson<List<Song>>("music/recommendations/leaderboard") {
            url {
                parameters.append(
                    "category",
                    cate
                )
            }
        }!!
        return data.toNetSongs("song-ByIds")
    }

    suspend fun getSongByIds(ids: List<Long>): List<Song> {
        val data = getOrJson<List<Song>>("music/song/ids") {
            url {
                ids.forEach { id ->
                    parameters.append(
                        "ids[]",
                        id.toString()
                    )
                }
            }
        }!!
        return data.toNetSongs("song-ByIds")
    }

    suspend fun getPlayList(): ListData<PlaylistInfo> {
        val data = getOrJson<ListData<PlaylistInfo>>("music/playlist/list")!!
        return data.let { data ->
            data.copy(
                list = data.list.map {
                    it.copy(
                        cover = it.cover?.let { cover -> "$MUSIC_BASE_URL/$cover" }
                    )
                }
            )
        }
    }

    suspend fun getPlayListSong(playlistId: Long): List<Song> {
        val data = getOrJson<List<Song>>("music/playlist/song") {
            url {
                parameters.append(
                    "playlistId",
                    playlistId.toString()
                )
            }
        }!!
        return data.toNetSongs("playlist-song")
    }

    suspend fun getPlayListInfo(playlistId: Long): PlaylistInfo {
        val data = getOrJson<PlaylistInfo>("music/playlist/info") {
            url {
                parameters.append(
                    "playlistId",
                    playlistId.toString()
                )
            }
        }!!
        return data.copy(cover = if (data.cover == null) null else "$MUSIC_BASE_URL/${data.cover}")
    }

    suspend fun incrementPlayTotal(songId: Long): PlaylistInfo {
        val data = getOrJson<PlaylistInfo>("music/song/incrementPlayTotal") {
            url {
                parameters.append(
                    "songId",
                    songId.toString()
                )
            }
        }!!
        return data.copy(cover = if (data.cover == null) null else "$MUSIC_BASE_URL/${data.cover}")
    }


    suspend fun toggleFavorite(songId: Long): Boolean {
        return getOrJson<Boolean>("music/song/toggleFavorite") {
            url {
                parameters.append(
                    "songId",
                    songId.toString()
                )
            }
        }!!
    }

    suspend fun getSongLyric(songId: Long): List<LyricData> {
        val data = getOrJson<List<LyricData>>("music/song/lyric") {
            url {
                parameters.append(
                    "songId",
                    songId.toString()
                )
            }
        }!!
        return data
    }


    suspend fun getArtist(id: Long): Artist {
        val data = getOrJson<Artist>("music/artist/info") {
            url {
                parameters.append("artistId", id.toString())
            }
        }!!
        return data.copy(
            cover = "$MUSIC_BASE_URL/${data.cover}",
            avatar = "$MUSIC_BASE_URL/${data.avatar}",
        )
    }

    suspend fun getArtistSong(id: Long): List<Song> {
        val data = getOrJson<List<Song>>("music/artist/song") {
            url {
                parameters.append("artistId", id.toString())
            }
        }!!
        return data.toNetSongs("artist-song-$id")
    }


    suspend fun getAlbum(id: Long): Album {
        val data = getOrJson<Album>("music/album/info") {
            url {
                parameters.append("albumId", id.toString())
            }
        }!!
        return data.copy(
            cover = "$MUSIC_BASE_URL/${data.cover}"
        )
    }

    suspend fun getAlbumSong(id: Long): List<Song> {
        val data = getOrJson<List<Song>>("music/album/song") {
            url {
                parameters.append("albumId", id.toString())
            }
        }!!
        return data.toNetSongs("artist-album-$id")
    }

    suspend fun getPlaylistSquare(): Square {
        val data = getOrJson<Square>("music/playlist/square")!!
        return data.copy(
            hotPlaylist = data.hotPlaylist.toNetPlaylist(),
            banner = data.banner.map {
                it.copy(cover = "$MUSIC_BASE_URL/${it.cover}")
            }
        )
    }

    suspend fun getPlaylistRecommend(type: String): ListData<Playlist> {
        val data = getOrJson<ListData<Playlist>>("music/playlist/recom") {
            url { parameters.append("type", type) }
        }!!
        println(data)
        return data.copy(list = data.list.toNetPlaylist())
    }

    suspend fun getPlaylistTabs(): List<PlaylistTab> {
        return getOrJson<List<PlaylistTab>>("music/playlist/tabs")!!
    }

    suspend fun getUserInfo(): UserInfo {
        return getOrJson<UserInfo>("user/userInfo")!!
    }

    suspend fun login(params: LoginRequest) {
        client.post("user/login") {
            contentType(ContentType.Application.Json)
            setBody(params)
        }
    }

    suspend inline fun <reified T> search(
        keyword: String,
        type: String,
        pageIndex: Int = 1,
        pageSize: Int = 20
    ): ListData<T> {
        val data = getOrJson<ListData<T>>("music/song/search") {
            url {
                parameters.append("keyword", keyword)
                parameters.append("type", type)
                parameters.append("pageIndex", pageIndex.toString())
                parameters.append("pageSize", pageSize.toString())
            }
        }!!
        return data
    }

    suspend fun searchComplex(keyword: String): Complex {
        val data = getOrJson<Complex>("music/song/search") {
            url { parameters.append("keyword", keyword) }
        }!!
        return data
    }
}


private fun List<Song>.toNetSongs(key: String = ""): List<Song> {
    return map { song ->
        song.copy(
            cover = "$MUSIC_BASE_URL/${song.cover}",
            url = "$MUSIC_BASE_URL/api/music/song/url?songId=${song.id}",
            uuid = generateNetMediaId(key, song),
            duration = song.duration * 1000
        )
    }
}

private fun List<Playlist>.toNetPlaylist(): List<Playlist> {
    return map { song ->
        song.copy(
            cover = "$MUSIC_BASE_URL/${song.cover}",
        )
    }
}

