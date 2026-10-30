package wzy.hkta.rhythbox.screens.search.netSearch.tabScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import wzy.hkta.rhythbox.screens.search.netSearch.SearchArtistItem

@Composable
fun SingerList() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(30){
          SearchArtistItem(
              name = "周杰伦",
              onClick = { /* 打开歌手详情 */ }
          )
      }
    }
}