package wzy.hkta.rhythbox.screens.homeScreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SnackbarManager
import wzy.hkta.rhythbox.utils.fatchMusic.Banner
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.Song

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
    }

    val playlist = playlistDao.getPlaylistWithSongs().stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun addPlayList(name: String, describe: String?) {
        screenModelScope.launch {
            try {
                playlistDao.insert(
                    PlaylistEntity(
                        name = name,
                        describe = describe
                    )
                )
            } catch (e: Exception) {
                val msg = e.message
                if (msg?.startsWith("Error code: 2067") == true) {
                    SnackbarManager.show("当前歌单已存在此歌曲")
                } else {
                    SnackbarManager.show("创建失败")
                }
            }
        }
    }


    fun fetchHomeData(){
        screenModelScope.launch {
            _isRefreshing.value=true
            try {
                val res = FetchMusic.fetchHome()
                _banner.value=res.bannerList
                _songList.value = res.hotSongList
            }catch (e: Exception){
              println(e)
            }
            _isRefreshing.value=false
        }
    }
}