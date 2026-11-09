package wzy.hkta.rhythbox.screens.favorite

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.PlayHistoryEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.fatchMusic.Album
import wzy.hkta.rhythbox.fatchMusic.Artist
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.Playlist
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.generateMediaId
import kotlin.time.Duration.Companion.milliseconds


enum class FavoriteType {
    SONG,
    ALBUM,
    PLAYLIST,
    ARTIST
}

sealed class LoadState {
    data object Loading : LoadState()
    data object Finish : LoadState()
    data object NotFound : LoadState()
    data class LoadError(val msg: String) : LoadState()
}

data class FavoriteResult<T>(
    val list: List<T> = emptyList(),
    var loading: LoadState = LoadState.Finish,
    var total: Long = 0,
)


class FavoriteViewModel : ScreenModel {
    var songDao = Rhythbox.dataBase.getSongDao()
    private val player = Rhythbox.player
    val playKey = player.playKey
    val localKey = "localFavoriteSongList"

    private val _netSongList = MutableStateFlow(FavoriteResult<SongType>())

    private val _artistList = MutableStateFlow(FavoriteResult<Artist>())

    private val _albumList = MutableStateFlow(FavoriteResult<Album>())

    private val _playlist = MutableStateFlow(FavoriteResult<Playlist>())

    val netSongList = _netSongList.asStateFlow()
    val artistList = _artistList.asStateFlow()
    val albumList = _albumList.asStateFlow()
    val playlist = _playlist.asStateFlow()

    val localSongList = songDao.findCollect().map { rawList ->
        rawList.map { song -> SongType.Local(song.copy(mediaId = generateMediaId(localKey, song))) }
    }.onEach { list ->
        if (playKey.value == localKey) {
            resetMediaItems(localKey, list)
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    private suspend fun <T> loadFavorite(
        fetch: suspend () -> List<T>,
        state: MutableStateFlow<FavoriteResult<T>>
    ) {
        // 正在加载或者已经没有更多数据时，不重复请求
        if (state.value.loading == LoadState.Loading ||
            state.value.loading == LoadState.NotFound
        ) return

        try {
            state.value = state.value.copy(
                loading = LoadState.Loading
            )

            val data = fetch()

            state.value = FavoriteResult(
                list = data,
                total = data.size.toLong(),
                loading = if (data.isEmpty()) {
                    LoadState.NotFound
                } else {
                    LoadState.Finish
                }
            )
        } catch (error: Exception) {
            state.value = state.value.copy(
                loading = LoadState.LoadError(error.message ?: "加载失败")
            )
        }
    }

    fun getList(type: FavoriteType) {
        screenModelScope.launch {
            when (type) {
                FavoriteType.SONG -> loadFavorite(
                    fetch = { FetchMusic.music.getFavoriteSong().map { SongType.Network(it) } },
                    state = _netSongList
                )

                FavoriteType.ALBUM -> loadFavorite(
                    fetch = { FetchMusic.music.getFavoriteAlbum() },
                    state = _albumList
                )

                FavoriteType.PLAYLIST -> loadFavorite(
                    fetch = { FetchMusic.music.getFavoritePlaylist() },
                    state = _playlist
                )

                FavoriteType.ARTIST -> loadFavorite(
                    fetch = { FetchMusic.music.getFavoriteArtist() },
                    state = _artistList
                )
            }
        }
    }


    fun resetMediaItems(key: String, songList: List<SongType>) {
        screenModelScope.launch {
            delay(50.milliseconds)
            player.resetMediaItems(key, songList, false)
        }
    }


    fun play(key: String, index: Int = 0) {
        player.let { p ->
            p.resetMediaItems(
                key,
                localSongList.value,
                false
            )
            p.prepare(index)
            p.play()
        }
    }


    fun netPlay(index: Int = 0, id: Long = 0) {
        screenModelScope.launch {
            songDao.deleteOldPlayHistory()
            songDao.insertPlayHistory(PlayHistoryEntity(songId = id))
        }
        player.resetMediaItems("favorite-net-song", netSongList.value.list, false) {
            player.prepare(index)
            player.play()
        }
    }

}