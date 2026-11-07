package wzy.hkta.rhythbox.screens.album

import androidx.compose.runtime.mutableStateListOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.fatchMusic.Album
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.fatchMusic.FetchMusic

class OnlineAlbumViewSongModel(
    val id: Long,
    val key: String
) : ScreenModel {

    private val _albumInfo = MutableStateFlow<Album?>(null)
    private val _songList = MutableStateFlow<MutableList<Song>>(mutableStateListOf())
    private val _loading = MutableStateFlow(true)

    val albumInfo = _albumInfo.asStateFlow()
    val songList = _songList.asStateFlow()
    val loading = _loading.asStateFlow()

    private val player = Rhythbox.player

    init {
        load()
    }

    /**
     * 获取专辑信息和歌曲
     */
    fun load() {
        screenModelScope.launch {
            try {
                _loading.value = true
                val albumDeferred = async { FetchMusic.music.getAlbum(id) }
                val songDeferred = async { FetchMusic.music.getAlbumSong(id) }
                _albumInfo.value = albumDeferred.await()
                _songList.value = songDeferred.await() as MutableList<Song>

            } catch (err: Exception) {
                err.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }


    fun togglePlaylistFavorite() {
        try {
            screenModelScope.launch {
                val status = FetchMusic.music.toggleAlbumFavorite(id)
                _albumInfo.value = _albumInfo.value?.copy(isLike = status)
            }
        } catch (err: Exception) {
            println("err $err")
        }
    }

    /**
     * 播放专辑歌曲
     */
    fun play(index: Int = 0) {

        if (songList.value.isEmpty()) return
        if (index !in songList.value.indices) return

        player.resetMediaItems(
            key,
            SongListType.NetworkList(songList.value),
            false,
            index
        )

        player.prepare(index)
        player.play()
    }

}