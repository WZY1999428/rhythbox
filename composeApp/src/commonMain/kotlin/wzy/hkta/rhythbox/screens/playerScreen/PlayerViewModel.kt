package wzy.hkta.rhythbox.screens.playerScreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.CollectEntity
import wzy.hkta.rhythbox.database.PlaylistSongEntity
import wzy.hkta.rhythbox.database.SongEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.SnackbarManager
import wzy.hkta.rhythbox.utils.SongInfo
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic


class PlayerViewModel : ScreenModel {
    val collectDao = Rhythbox.dataBase.getCollectDao()
    val playlistDao = Rhythbox.dataBase.getPlaylistDao()
    fun toggleCollect(item: SongType, status: Boolean) {
        screenModelScope.launch {
            when (item) {
                is SongType.Local -> {
                    if (status) collectDao.insert(CollectEntity(songId = item.song.id))
                    else collectDao.delete(item.song.id)
                    Rhythbox.player.updateMediaItem(item.copy(song = item.song.copy(isLike = !item.song.isLike)))
                }

                is SongType.Network -> {
                   val status = FetchMusic.toggleFavorite(item.song.id)
                    Rhythbox.player.updateMediaItem(item.copy(song = item.song.copy(isLike = status)))
                }
            }
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

