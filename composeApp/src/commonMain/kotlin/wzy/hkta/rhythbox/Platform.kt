package wzy.hkta.rhythbox

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform


@Composable
expect fun GetCover(cb: (String?) -> Unit)

