package wzy.hkta.rhythbox.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.back
import wzy.hkta.rhythbox.utils.marqueeText

@Composable
fun NavBar(
    title: String? = null,
    @Suppress("ModifierParameter") modifier: Modifier = Modifier,
    iconColor: Color = Color.Black,
    titleColor: Color = Color.Black,
    background: Color = Color.White,
    action: (@Composable () -> Unit)? = null
) {
    val navigator = LocalNavigator.currentOrThrow;
    Box(
        modifier = Modifier.fillMaxWidth().background(background).drawBehind {
            if (background != Color.Transparent) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.08f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }.windowInsetsPadding(WindowInsets.statusBars).height(50.dp).then(modifier)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = { navigator.pop() }
        ) {
            Icon(
                painter = painterResource(Res.drawable.back),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        if (title != null) {
            Text(
                title,
                modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.Center).marqueeText(),
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = titleColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            action?.invoke()
        }
    }
}