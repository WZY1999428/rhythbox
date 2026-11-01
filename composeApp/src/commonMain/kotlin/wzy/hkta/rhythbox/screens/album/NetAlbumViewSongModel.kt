package wzy.hkta.rhythbox.screens.album

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongListType
import wzy.hkta.rhythbox.utils.fatchMusic.Album
import wzy.hkta.rhythbox.utils.fatchMusic.Song
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic

class NetAlbumViewSongModel(
    val id: Long,
    val key: String
) : ScreenModel {

    private val _album = MutableStateFlow<Album?>(null)
    private val _songList = MutableStateFlow<List<Song>>(emptyList())
    private val _loading = MutableStateFlow(true)

    val album = _album.asStateFlow()
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
                val albumDeferred = async { FetchMusic.getAlbum(id) }
                val songDeferred = async { FetchMusic.getAlbumSong(id) }
                _album.value = albumDeferred.await()
                _songList.value = songDeferred.await()

            } catch (err: Exception) {
                err.printStackTrace()
            } finally {
                _loading.value = false
            }
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