package wzy.hkta.rhythbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.ExpandMore
import rhythbox.composeapp.generated.resources.Refresh
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.notfound
import wzy.hkta.rhythbox.screens.search.netSearch.LoadState

@Composable
fun LoadMore(
    state: LoadState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp), // 没有数据时，上下留白可以适当调大一点，显得更开阔
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            LoadState.Loading -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "正在加载...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LoadState.LoadMore -> {
                TextButton(onClick = onLoadMore) {
                    Text("加载更多")
                    Icon(
                        painter = painterResource(Res.drawable.ExpandMore),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LoadState.NoMoreData -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.width(32.dp))
                    Text(
                        text = "没有更多内容了",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    HorizontalDivider(modifier = Modifier.width(32.dp))
                }
            }

            // 【实现 NotFound 状态】
            LoadState.NotFound -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.notfound), // 替换为你合适的空状态图标（比如没有数据的图标）
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "暂无相关搜索结果",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            is LoadState.LoadError -> {
                TextButton(onClick = onRetry) {
                    Icon(
                        painter = painterResource(Res.drawable.Refresh),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("加载失败，点击重试")
                }
            }
        }
    }
}