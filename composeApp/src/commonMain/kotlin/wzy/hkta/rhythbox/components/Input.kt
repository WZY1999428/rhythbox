package wzy.hkta.rhythbox.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class InputType {
    Text,
    Number,
    Password,
}

@Composable
fun Input(
    value: String,
    enabled: Boolean = true,
    placeholder: String = "请输入内容",
    type: InputType = InputType.Text,
    textAlign: TextAlign = TextAlign.Start,
    onValueChange: ((String) -> Unit)? = null,
    onFocusedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {

    val boxAlignment = when (textAlign) {
        TextAlign.Start -> Alignment.CenterStart
        TextAlign.Center -> Alignment.Center
        TextAlign.End -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }

    BasicTextField(
        value = value,
        enabled = enabled && onClick == null,
        onValueChange = { newValue ->
            val filteredValue = when (type) {
                InputType.Text -> newValue
                InputType.Number -> newValue.filter {
                    it.isDigit()
                }

                InputType.Password -> newValue
            }
            onValueChange?.invoke(filteredValue)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .onFocusChanged { focusState ->
                onFocusedChange?.invoke(focusState.isFocused)
            },
        textStyle = TextStyle(
            color = Color(0xFF2C3E50),
            fontSize = 14.sp,
            textAlign = textAlign
        ),
        singleLine = true,

        // 键盘类型
        keyboardOptions = KeyboardOptions(
            keyboardType = when (type) {
                InputType.Text -> KeyboardType.Text
                InputType.Number -> KeyboardType.Number
                InputType.Password -> KeyboardType.Password
            }
        ),

        // 密码隐藏
        visualTransformation = when (type) {
            InputType.Password -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },

        decorationBox = { innerTextField ->
            val modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .let { modifier ->
                    if (onClick != null) {
                        modifier.clickable { onClick() }
                    } else {
                        modifier
                    }
                }

            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically // 确保整行内容完美垂直居中
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = boxAlignment
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = textAlign
                        )
                    }
                    innerTextField() // 真实的文本输入流
                }
            }
        }
    )
}
