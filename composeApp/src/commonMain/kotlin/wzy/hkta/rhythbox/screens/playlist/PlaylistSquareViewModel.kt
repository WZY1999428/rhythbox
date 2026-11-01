package wzy.hkta.rhythbox.screens.playlist

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.utils.fatchMusic.Banner
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.Playlist
import wzy.hkta.rhythbox.utils.fatchMusic.PlaylistBanner
import wzy.hkta.rhythbox.utils.fatchMusic.PlaylistTab

class PlaylistSquareViewModel : ScreenModel {

    private val _hotPlaylist = MutableStateFlow<List<Playlist>>(emptyList())
    private val _banner = MutableStateFlow<List<PlaylistBanner>>(emptyList())
    private val _recommendPlaylist = MutableStateFlow<List<Playlist>>(emptyList())
    private val _playlistType = MutableStateFlow("recommend")
    private val _playlistTabs = MutableStateFlow<List<PlaylistTab>>(emptyList())

    val hotPlaylist = _hotPlaylist.asStateFlow()
    val banner = _banner.asStateFlow()
    val recommendPlaylist = _recommendPlaylist.asStateFlow()
    val playlistType = _playlistType.asStateFlow()
    val playlistTabs = _playlistTabs.asStateFlow()

    init {
        getPlaylistRecommend()
        getPlaylistSquare()
    }

    fun setPlaylistType(type: String) {
        _playlistType.value = type
        getPlaylistRecommend()
    }

    fun getPlaylistSquare() {
        screenModelScope.launch {
            try {
                val dataFuture = async { FetchMusic.getPlaylistSquare() }
                val tabsFuture = async { FetchMusic.getPlaylistTabs() }
                val data = dataFuture.await()
                _playlistTabs.value = tabsFuture.await()
                _banner.value = data.banner
                _hotPlaylist.value = data.hotPlaylist
            } catch (e: Exception) {
                println("eee $e")
            }
        }
    }

    fun getPlaylistRecommend() {
        screenModelScope.launch {
            try {
                val data = FetchMusic.getPlaylistRecommend(_playlistType.value)
                println(data)
                _recommendPlaylist.value = data.list
            } catch (e: Exception) {
                println("eee $e")
            }
        }
    }
}