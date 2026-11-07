package wzy.hkta.rhythbox.fatchMusic

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import wzy.hkta.rhythbox.fatchMusic.FetchMusic.client
import wzy.hkta.rhythbox.fatchMusic.FetchMusic.getOrJson
import wzy.hkta.rhythbox.fatchMusic.FetchMusic.json
import kotlin.collections.hashMapOf

object Music {
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


    suspend fun getFavoriteSong(): List<Song> {
        val data = getOrJson<List<Song>>("music/song/favorite")!!
        return data.toNetSongs("netFavoriteSongList")
    }

    suspend fun getSongQuality(songId: Long): List<SongAudioItem> {
        val data = getOrJson<List<SongAudioItem>>("music/song/quality") {
            url {
                parameters.append("songId", songId.toString())
            }
        }!!
        return data
    }


    suspend fun getFavoriteAlbum(): List<Album> {
        val data = getOrJson<List<Album>>("music/album/favorite")!!
        return data
    }

    suspend fun getFavoriteArtist(): List<Artist> {
        val data = getOrJson<List<Artist>>("music/artist/favorite")!!
        return data
    }

    suspend fun getFavoritePlaylist(): List<Playlist> {
        val data = getOrJson<List<Playlist>>("music/playlist/favorite")!!
        return data
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


    suspend fun getPlayListSong(playlistId: Long): List<Song> {
        val data = getOrJson<List<Song>>("music/playlist/song") {
            url {
                parameters.append(
                    "playlistId",
                    playlistId.toString()
                )
            }
        }!!
        return data.toNetSongs("$playlistId-playlist-song")
    }


    suspend fun togglePlaylistFavorite(playlistId: Long): Boolean {
        val result = client.post("music/playlist/toggleFavorite") {
            contentType(ContentType.Application.Json)
            setBody(hashMapOf("playlistId" to playlistId))
        }
        val data = json.decodeFromString<Response<Boolean>>(result.bodyAsText())
        return data.data!!
    }

    suspend fun toggleAlbumFavorite(albumId: Long): Boolean {
        val result = client.post("music/album/toggleFavorite") {
            contentType(ContentType.Application.Json)
            setBody(hashMapOf("albumId" to albumId))
        }
        val data = json.decodeFromString<Response<Boolean>>(result.bodyAsText())
        return data.data!!
    }

    suspend fun getArtistAlbumList(artistId: Long): List<Album> {
        val data = getOrJson<List<Album>>("music/artist/albumList") {
            url {
                parameters.append("artistId", artistId.toString())
            }
        }!!
        return data.toNetAlbum()
    }

    suspend fun toggleArtistFavorite(artistId: Long): Boolean {
        val result = client.post("music/artist/toggleFavorite") {
            contentType(ContentType.Application.Json)
            setBody(hashMapOf("artistId" to artistId))
        }
        val data = json.decodeFromString<Response<Boolean>>(result.bodyAsText())
        return data.data!!
    }

    suspend fun toggleFavorite(songId: Long): Boolean {
        val result = client.post("music/song/toggleFavorite") {
            contentType(ContentType.Application.Json)
            setBody(hashMapOf("songId" to songId))
        }
        println("result.bodyAsText() ${result.bodyAsText()}")
        val data = json.decodeFromString<Response<Boolean>>(result.bodyAsText())
        return data.data!!
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
        return data.copy(cover = "$MUSIC_BASE_URL/${data.cover}")
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


    suspend fun searchComplex(keyword: String): Complex {
        val data = getOrJson<Complex>("music/song/search") {
            url { parameters.append("keyword", keyword) }
        }!!
        return data
    }

    suspend fun incrementPlaylistPlayTotal(playlistId: Long): Boolean {
        client.post("music/playlist/incrementPlayTotal") {
            contentType(ContentType.Application.Json)
            setBody(
                hashMapOf(
                    "playlistId" to playlistId
                )
            )
        }
        return true
    }
}