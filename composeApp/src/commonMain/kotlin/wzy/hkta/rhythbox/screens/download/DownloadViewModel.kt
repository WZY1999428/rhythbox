package wzy.hkta.rhythbox.screens.download

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.DownloadSongEntity
import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.database.toSong
import wzy.hkta.rhythbox.fatchMusic.Song
import wzy.hkta.rhythbox.utils.DownloadEngine
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.generateNetMediaId
import wzy.hkta.rhythbox.utils.sortSong
import kotlin.time.Duration.Companion.milliseconds

class DownloadViewModel(val key: String) : ScreenModel {
    var downloadDao = Rhythbox.dataBase.getDownloadDao()
    private val _sortEntity = MutableStateFlow(PlaylistSortEntity(sortKey = key))
    val sortEntity = _sortEntity.asStateFlow()
    private val player = Rhythbox.player

    val downloadList = DownloadEngine.tasks

    private var downloadEntity = emptyList<DownloadSongEntity>()


    init {
        getDownloadEntity()
    }

    val songList = combine(
        downloadDao.findSuccess(),
        Rhythbox.dataBase.getPlayListSortDao().get(key)
    ) { rawList, sort ->
        val sortConfig = sort ?: PlaylistSortEntity(sortKey = key)
        _sortEntity.value = sortConfig
        val newList =
            rawList.map { dSong ->
                var newSong = dSong.toSong()
                newSong = newSong.copy(mediaId = generateNetMediaId(key, newSong))
                SongType.Network(newSong)
            }
        val sortList = sortSong(newList, sortConfig.sortName, sortConfig.sortType)
        if (player.playKey.value == key) {
            player.sortMediaItems(sortList)
            resetMediaItems()
        }
        sortList
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun getDownloadEntity() {
        screenModelScope.launch {
            downloadEntity = downloadDao.findALL()
        }
    }

    fun getTaskSong(filePath: String?): Song? {
        if (filePath.isNullOrEmpty()) return null
        val song = downloadEntity.find { it.downloadPath == filePath }?.song
        return song
    }


    fun clear() {
        screenModelScope.launch {
            DownloadEngine.clear()
        }
    }


    fun play(index: Int = 0) {
        player.resetMediaItems(key, songList.value, false) {
            player.prepare(index)
            player.play()
        }
    }

    fun resetMediaItems() {
        screenModelScope.launch {
            delay(50.milliseconds)
            player.resetMediaItems(key, songList.value, false)
        }
    }
}