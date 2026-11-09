package wzy.hkta.rhythbox.screens.homeScreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.UserManager
import wzy.hkta.rhythbox.fatchMusic.Banner
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.PlaylistFields
import wzy.hkta.rhythbox.fatchMusic.toNetSongs
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.ToastManager

class HomeViewModel : ScreenModel {
    private val playlistDao = Rhythbox.dataBase.getPlaylistDao()
    private val _banner = MutableStateFlow<List<Banner>>(emptyList())
    private val _isRefreshing = MutableStateFlow(false)
    private val _songList = MutableStateFlow<List<SongType>>(emptyList())

    val banner = _banner.asStateFlow()
    val player = Rhythbox.player
    val playKey = player.playKey
    val songList = _songList.asStateFlow()

    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        fetchHomeData()
        getUserPlaylist()
    }

    val playlist: StateFlow<List<PlaylistFields<SongType.Local>>> =
        playlistDao.getPlaylistWithSongs().map { list ->
            list.map { item ->
                val playlist = item.playlist
                PlaylistFields(
                    id = playlist.id,
                    title = playlist.name,
                    description = playlist.describe!!,
                    cover = null,
                    songList = item.songList.map {
                        SongType.Local(it)
                    }
                )
            }
        }.stateIn(
            screenModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _onlinePlayList = MutableStateFlow<List<PlaylistFields<SongType>>>(emptyList())

    val onlinePlayList = _onlinePlayList.asStateFlow()

    fun fetchHomeData() {
        screenModelScope.launch {
            _isRefreshing.value = true
            getUserPlaylist()
            try {
                val res = FetchMusic.music.fetchHome()
                _banner.value = res.bannerList
                _songList.value = res.hotSongList.map { SongType.Network(it) }
            } catch (e: Exception) {
                println(e)
            }
            _isRefreshing.value = false
        }
    }


    fun playNewHotSong(index: Int, key: String) {
        if (playKey.value != key) {
            screenModelScope.launch {
                ToastManager.showLoading("加载中...")
                try {
                    val song = FetchMusic.music.getHotSong()
                    val songList = song.toNetSongs().map { SongType.Network(it) }
                    play(index, songList, key)
                } catch (e: Exception) {
                    println("ExceptionException $e")
                } finally {
                    ToastManager.hideLoading()
                }
            }

        } else {
            player.prepare(index)
        }
    }

    fun play(index: Int = 0, songList: List<SongType>, key: String) {
        player.resetMediaItems(key, songList, false) {
            player.prepare(index)
            player.play()
        }
    }


    fun getUserPlaylist() {
        UserManager.getToken()?.let {
            screenModelScope.launch {
                try {
                    FetchMusic.user.getUserPlaylist()?.let { list ->
                        _onlinePlayList.value = list.map { playlist ->
                            PlaylistFields(
                                id = playlist.id,
                                title = playlist.title,
                                description = playlist.description,
                                cover = playlist.cover,
                                songList = playlist.songList.map { SongType.Network(it) })
                        }
                    }
                } catch (e: Exception) {
                    println("ExceptionException $e")
                }
            }
        }

    }
}