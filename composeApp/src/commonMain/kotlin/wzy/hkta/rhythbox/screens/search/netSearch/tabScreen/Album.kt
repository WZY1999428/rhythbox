package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import wzy.hkta.rhythbox.screens.search.netSearch.SearchAlbumItem

@Composable
fun  AlbumList(){
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(30){
            SearchAlbumItem(
                title = "叶惠美",
                artist = "周杰伦",
                year = "2003",
                onClick = { /* 打开专辑 */ })
        }
    }
}