package wzy.hkta.rhythbox.screens.search.netSearch

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.utils.fatchMusic.Album
import wzy.hkta.rhythbox.utils.fatchMusic.Artist
import wzy.hkta.rhythbox.utils.fatchMusic.Complex
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.ListData
import wzy.hkta.rhythbox.utils.fatchMusic.Playlist
import wzy.hkta.rhythbox.utils.fatchMusic.Song
import kotlin.collections.emptyList

enum class SearchType {
    SONG,
    ALBUM,
    PLAYLIST,
    ARTIST
}

sealed class LoadState {
    object Loading : LoadState()
    object NoMoreData : LoadState()
    object LoadMore : LoadState()
    object NotFound : LoadState()
    data class LoadError(val error: String) : LoadState()
}

data class SearchResult<T>(
    val list: List<T> = emptyList(),
    var loading: LoadState = LoadState.LoadMore,
    var total: Long = 0,
    var pageIndex: Int = 0,
    var pageSize: Int = 20
)


class NetSearchViewModel : ScreenModel {
    private val _songList = MutableStateFlow<SearchResult<Song>>(SearchResult(emptyList()))
    private val _artistList = MutableStateFlow<SearchResult<Artist>>(SearchResult(emptyList()))
    private val _albumList = MutableStateFlow<SearchResult<Album>>(SearchResult(emptyList()))
    private val _playlist = MutableStateFlow<SearchResult<Playlist>>(SearchResult(emptyList()))
    private val _complex =
        MutableStateFlow(Complex(emptyList(), emptyList(), emptyList(), emptyList()))
    private val _keyword = MutableStateFlow("")
    private val _showSearchDiscovery = MutableStateFlow(true)

    val songList = _songList.asStateFlow()
    val artistList = _artistList.asStateFlow()
    val albumList = _albumList.asStateFlow()
    val playlist = _playlist.asStateFlow()
    val complex = _complex.asStateFlow()
    val keyword = _keyword.asStateFlow()
    val showSearchDiscovery = _showSearchDiscovery.asStateFlow()


    fun  clearKeyword() {
        _keyword.value = ""
    }

    fun startSearch(keyword: String) {
        _keyword.value = keyword
        _songList.value = SearchResult(emptyList())
        _artistList.value = SearchResult(emptyList())
        _albumList.value = SearchResult(emptyList())
        _playlist.value = SearchResult(emptyList())
        _complex.value = Complex(emptyList(), emptyList(), emptyList(), emptyList())
        searchComplex()
    }

    fun toggleSearchDiscovery(show: Boolean) {
        _showSearchDiscovery.value = show
    }

    fun addPageIndex(type: SearchType) {
        val state = when (type) {
            SearchType.SONG -> _songList.value
            SearchType.ALBUM -> _albumList.value
            SearchType.PLAYLIST -> _playlist.value
            SearchType.ARTIST -> _artistList.value
        }

        if (state.loading == LoadState.Loading ||
            state.loading == LoadState.NoMoreData
        ) return

        state.pageIndex++

        search(type, state.pageIndex, state.pageSize)
    }


    fun retry(type: SearchType) {
        val state = when (type) {
            SearchType.SONG -> _songList.value
            SearchType.ALBUM -> _albumList.value
            SearchType.PLAYLIST -> _playlist.value
            SearchType.ARTIST -> _artistList.value
        }
        state.pageIndex--
        addPageIndex(type)
    }


    private suspend inline fun <reified T> loadPage(
        pageIndex: Int,
        pageSize: Int,
        type: String,
        crossinline updateLoading: (LoadState) -> Unit,
        crossinline updateData: (ListData<T>) -> Int
    ) {
        try {
            updateLoading(LoadState.Loading)
            val data = FetchMusic.search<T>(
                _keyword.value,
                type,
                pageIndex,
                pageSize
            )

            val currentSize = updateData(data)

            updateLoading(
                if (data.total == 0.toLong()) {
                    LoadState.NotFound
                } else if (currentSize >= data.total) {
                    LoadState.NoMoreData
                } else {
                    LoadState.LoadMore
                }
            )
        } catch (error: Exception) {
            updateLoading(LoadState.LoadError(error.message ?: ""))
        }
    }

    fun search(
        type: SearchType,
        pageIndex: Int,
        pageSize: Int
    ) {
        screenModelScope.launch {
            when (type) {
                SearchType.SONG -> loadPage(
                    pageIndex,
                    pageSize,
                    type = "signle",
                    { _songList.value = songList.value.copy(loading = it) }
                ) { data ->

                    _songList.value = songList.value.copy(
                        list = songList.value.list + data.list,
                        total = data.total
                    )
                    _songList.value.list.size
                }

                SearchType.ALBUM -> loadPage(
                    pageIndex,
                    pageSize,
                    type = "album",
                    { _albumList.value = albumList.value.copy(loading = it) }
                ) { data ->
                    _albumList.value = albumList.value.copy(
                        list = albumList.value.list + data.list,
                        total = data.total
                    )
                    _albumList.value.list.size
                }

                SearchType.PLAYLIST -> loadPage(
                    pageIndex,
                    pageSize,
                    type = "playlist",
                    { _playlist.value = playlist.value.copy(loading = it) }
                ) { data ->
                    _playlist.value = playlist.value.copy(
                        list = playlist.value.list + data.list,
                        total = data.total
                    )
                    _playlist.value.list.size
                }

                SearchType.ARTIST -> loadPage(
                    pageIndex,
                    pageSize,
                    type = "artist",
                    { _artistList.value = artistList.value.copy(loading = it) }
                ) { data ->
                    _artistList.value = artistList.value.copy(
                        list = artistList.value.list + data.list,
                        total = data.total
                    )
                    _artistList.value.list.size
                }
            }
        }
    }

    fun searchComplex() {
        screenModelScope.launch {
            _complex.value = FetchMusic.searchComplex(_keyword.value)
        }
    }

}