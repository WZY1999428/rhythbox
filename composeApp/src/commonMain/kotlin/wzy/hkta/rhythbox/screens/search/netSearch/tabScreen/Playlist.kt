package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import wzy.hkta.rhythbox.screens.search.netSearch.SearchPlaylistItem

@Composable
fun  PlaylistList(){
    LazyColumn(modifier = Modifier.fillMaxSize()) {
       items(30){
           SearchPlaylistItem(
               title = "适合一个人听的华语歌",
               creator = "湾湾的歌单",
               songCount = 36,
               onClick = { /* 打开歌单 */ })
       }
    }
}