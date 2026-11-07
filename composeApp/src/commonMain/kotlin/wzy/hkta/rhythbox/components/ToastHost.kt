package wzy.hkta.rhythbox.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import wzy.hkta.rhythbox.utils.ToastManager

@Composable
fun Toast(
    hostState: SnackbarHostState, modifier: Modifier = Modifier
) {
    val loading by ToastManager.loading.collectAsState()
    val isShow by ToastManager.isShow.collectAsState()
    if (isShow) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            ),
            onDismissRequest = {},
        ) {
            SnackbarHost(
                hostState = hostState, modifier = Modifier.zIndex(9999999f).then(modifier)
            ) { data ->

                Box(
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp)
                    ).widthIn(min = 100.dp, max = 240.dp).padding(
                        horizontal = 14.dp, vertical = 12.dp
                    ), contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )

                            Spacer(Modifier.width(10.dp))
                        }

                        Text(
                            text = data.visuals.message,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

}
