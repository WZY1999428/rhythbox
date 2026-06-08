package wzy.hkta.rhythbox.pages.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import wzy.hkta.rhythbox.cssToColor

@Composable
internal fun Category(
    text: String,
    modifier: Modifier? = Modifier,
    icon: DrawableResource,
    iconSize: Dp = 18.dp,
    fontSize: TextUnit = 15.sp,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.then(modifier ?: Modifier),
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(10.dp)
            )
                .fillMaxSize().clickable {
                    onClick?.invoke()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(icon),
                modifier = Modifier.size(iconSize),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text,
                fontSize = fontSize,
                color = "#BBB9B4".cssToColor()
            )
        }
    }
}
