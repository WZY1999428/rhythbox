package wzy.hkta.rhythbox.pages.playerScreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.CollectEntity
import wzy.hkta.rhythbox.database.PlaylistSongEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SnackbarManager


class PlayerViewModel : ScreenModel {
    val collectDao = Rhythbox.dataBase.getCollectDao()
    val playlistDao = Rhythbox.dataBase.getPlaylistDao()
    fun toggleCollect(item: SongEntity, status: Boolean) {
        screenModelScope.launch {
            if (status) collectDao.insert(CollectEntity(songId = item.id)) else collectDao.delete(
                item.id
            )
            Rhythbox.player.updateMediaItem(item.copy(isLike = !item.isLike))
        }
    }

    fun addFolderSong(id: Long, songId: Long, cover: String?) {
        screenModelScope.launch {
            try {
                playlistDao.addSong(
                    PlaylistSongEntity(
                        songId = songId,
                        playlistId = id,
                        congCover = cover
                    )
                )
                SnackbarManager.show("添加成功")
            } catch (e: Exception) {
                val msg = e.message
                if (msg?.startsWith("Error code: 2067") == true) {
                    SnackbarManager.show("歌单已存在")
                } else {
                    SnackbarManager.show("创建失败")
                }
            }

        }
    }
}

