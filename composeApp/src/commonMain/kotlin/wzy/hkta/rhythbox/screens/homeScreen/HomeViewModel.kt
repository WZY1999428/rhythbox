package wzy.hkta.rhythbox.screens.homeScreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.UserManager
import wzy.hkta.rhythbox.fatchMusic.Banner
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.PlaylistFields
import wzy.hkta.rhythbox.fatchMusic.Song

class HomeViewModel : ScreenModel {
    private val playlistDao = Rhythbox.dataBase.getPlaylistDao()
    private val _banner = MutableStateFlow<List<Banner>>(emptyList())
    private val _isRefreshing = MutableStateFlow(false)
    private val _songList = MutableStateFlow<List<Song>>(emptyList())

    val banner = _banner.asStateFlow()
    val songList = _songList.asStateFlow()

    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        fetchHomeData()
        getUserPlaylist()
    }

    val playlist = playlistDao.getPlaylistWithSongs().stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _onlinePlayList = MutableStateFlow<List<PlaylistFields>>(emptyList())

    val onlinePlayList = _onlinePlayList.asStateFlow()

    fun fetchHomeData() {
        screenModelScope.launch {
            _isRefreshing.value = true
            getUserPlaylist()
            try {
                val res = FetchMusic.music.fetchHome()
                _banner.value = res.bannerList
                _songList.value = res.hotSongList
            } catch (e: Exception) {
                println(e)
            }
            _isRefreshing.value = false
        }
    }


    fun getUserPlaylist() {

        UserManager.getToken()?.let {
            screenModelScope.launch {
                try {
                    FetchMusic.user.getUserPlaylist()?.let { li ->
                        _onlinePlayList.value = li
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }

    }
}