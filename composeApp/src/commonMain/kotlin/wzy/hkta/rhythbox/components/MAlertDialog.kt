package wzy.hkta.rhythbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MAlertDialog(
    show: Boolean = false,
    title: String,
    content: String,
    cancelText: String = "取消",
    confirmText: String = "确认",
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null,
) {
    if (show) {
        BasicAlertDialog(
            onDismissRequest = {}
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 10.dp
                    )
                ) {
                    Text(title)
                    Spacer(Modifier.height(10.dp))
                    Text(content)
                    Spacer(Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (onCancel != null) {
                            TextButton(
                                onClick = { onCancel() }
                            ) {
                                Text(cancelText)
                            }
                        }

                        TextButton(
                            onClick = { onConfirm() }
                        ) {
                            Text(confirmText)
                        }
                    }
                }
            }
        }
    }

}