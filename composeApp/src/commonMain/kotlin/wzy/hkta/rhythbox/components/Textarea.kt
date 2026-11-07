package wzy.hkta.rhythbox.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Textarea(
    value: String,
    enabled: Boolean = true,
    maxLength: Int = 1000, // 最大长度限制
    minHeight: Dp = 100.dp, // 默认最小高度，模拟 textarea 大小
    maxHeight: Dp = 200.dp, // 最大高度限制（可选）
    placeholder: String = "请输入内容...",
    showLimit: Boolean = true, // 是否显示字数限制
    textAlign: TextAlign = TextAlign.Start,
    onValueChange: (String) -> Unit, // 语义更贴切，原 onKeywordChange
    onFocusedChange: ((Boolean) -> Unit)? = null,
) {
    // 多行文本通常以左上/右上/顶部居中对齐为主
    val boxAlignment = when (textAlign) {
        TextAlign.Start, TextAlign.Left -> Alignment.TopStart
        TextAlign.Center -> Alignment.TopCenter
        TextAlign.End, TextAlign.Right -> Alignment.TopEnd
        else -> Alignment.TopStart
    }

    val scrollState = rememberScrollState()

    BasicTextField(
        value = value,
        enabled = enabled,
        onValueChange = {
            onValueChange(it.take(maxLength))
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
            .onFocusChanged { focusState ->
                onFocusedChange?.invoke(focusState.isFocused)
            },
        textStyle = TextStyle(
            color = Color(0xFF2C3E50),
            fontSize = 14.sp,
            textAlign = textAlign
        ),
        singleLine = false, // 允许换行，变成真正的多行文本
        decorationBox = { innerTextField ->
            // 外部用 Box 代替 Row，让多行文本可以上下左右自由伸展并支持内边距
            Box(
                modifier = Modifier
                    .fillMaxSize(), // 模拟 textarea 的内部边距
                contentAlignment = boxAlignment
            ) {
                // 占位符提示文本
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = textAlign
                    )
                }

                if (showLimit) {
                    Text(
                        text = "${value.length}/${maxLength}",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(
                                color = Color.White.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // 真实输入区域，赋予垂直滚动能力
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    contentAlignment = boxAlignment
                ) {
                    innerTextField()
                }
            }
        }
    )
}