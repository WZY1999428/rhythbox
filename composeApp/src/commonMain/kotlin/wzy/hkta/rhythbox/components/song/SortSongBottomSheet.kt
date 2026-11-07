package wzy.hkta.rhythbox.components.song

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.database.PlaylistSortEntity
import wzy.hkta.rhythbox.utils.Rhythbox


data class SortFields(
    val type: String,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSongBottomSheet(
    show: Boolean = false,
    sortKey: String,
    onDismissRequest: () -> Unit,
) {
    // 🌟 从当前选中的完整字符串拆解出 维度 (时间/名称/歌手) 和 顺序 (升序/降序)
    var playlistSort by remember { mutableStateOf(PlaylistSortEntity(sortKey = sortKey)) }
    val playlistSortDao = Rhythbox.dataBase.getPlayListSortDao()
    val scope = rememberCoroutineScope()
    LaunchedEffect(show) {
        if (show) {
            scope.launch {
                playlistSortDao.getInfo(sortKey)?.let { playlistSort = it }
            }
        }
    }

    fun upsert(sort: PlaylistSortEntity) {
        scope.launch {
            playlistSort = sort
            playlistSortDao.upsert(sort)
            if (playlistSort.id == 0.toLong()) {
                playlistSortDao.getInfo(sortKey)?.let { playlistSort = it }
            }
        }
    }

    if (show) {
        ModalBottomSheet(
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(36.dp, 4.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            CircleShape
                        )
                )
            },
            containerColor = Color(0xFFFFFBF9), // 温暖微醺米白底色
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            onDismissRequest = { onDismissRequest() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                // 🏷️ 标题：歌曲排序
                Text(
                    text = "歌曲排序",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A3E3D),
                    modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
                )

                // ---------------- 1. 排序依据/维度 ----------------
                Text(
                    text = "排序依据",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A3E3D).copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                val fields = listOf(
                    SortFields("name", "名称"),
                    SortFields("date", "时间"),
                    SortFields("artist", "歌手")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fields.forEach { field ->
                        val isSelected = field.type == playlistSort.sortName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                )
                                .clickable {
                                    upsert(playlistSort.copy(sortName = field.type))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = field.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF4A3E3D).copy(
                                    alpha = 0.7f
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---------------- 2. 排序方向/类型 ----------------
                Text(
                    text = "排序方向",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A3E3D).copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                val orders = listOf(SortFields("asc", "升序"), SortFields("desc", "降序"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    orders.forEach { (order, symbol) ->
                        val isSelected = order == playlistSort.sortType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                ).clickable {
                                    upsert(playlistSort.copy(sortType = order))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$order $symbol",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF4A3E3D).copy(
                                        alpha = 0.7f
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

