package wzy.hkta.rhythbox.pages.homeScreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.google.protobuf.LazyStringArrayList.emptyList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.PlaylistEntity
import wzy.hkta.rhythbox.database.PlaylistSongEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SnackbarManager
import kotlin.collections.emptyList

class HomeViewModel : ScreenModel {
    private val playlistDao = Rhythbox.dataBase.getPlaylistDao()

    val playlist = playlistDao.getPlaylistWithSongs().stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = kotlin.collections.emptyList()
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
}