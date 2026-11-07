package wzy.hkta.rhythbox.screens.playerScreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.CollectEntity
import wzy.hkta.rhythbox.database.PlaylistSongEntity
import wzy.hkta.rhythbox.utils.Rhythbox
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.utils.SongType
import wzy.hkta.rhythbox.fatchMusic.FetchMusic


class PlayerViewModel : ScreenModel {
    val collectDao = Rhythbox.dataBase.getCollectDao()
    val playlistDao = Rhythbox.dataBase.getPlaylistDao()
    fun toggleCollect(item: SongType, isLike: Boolean) {
        screenModelScope.launch {
            when (item) {
                is SongType.Local -> {
                    if (isLike) collectDao.insert(CollectEntity(songId = item.song.id))
                    else collectDao.delete(item.song.id)
                    val song = item.copy(song = item.song.copy(isLike = !item.song.isLike))
                    Rhythbox.player.updateMediaItem(song)
                }

                is SongType.Network -> {
                    val status = FetchMusic.music.toggleFavorite(item.song.id)
                    val song = item.copy(song = item.song.copy(isLike = status))
                    Rhythbox.player.updateMediaItem(song)
                }
            }
        }
    }

    fun addFolderSong(id: Long, songId: Long) {
        screenModelScope.launch {
            try {
                playlistDao.addSong(
                    PlaylistSongEntity(
                        songId = songId,
                        playlistId = id,
                    )
                )
                ToastManager.show("添加成功")
            } catch (e: Exception) {
                val msg = e.message
                if (msg?.startsWith("Error code: 2067") == true) {
                    ToastManager.show("歌单已存在")
                } else {
                    ToastManager.show("创建失败")
                }
            }

        }
    }
}

