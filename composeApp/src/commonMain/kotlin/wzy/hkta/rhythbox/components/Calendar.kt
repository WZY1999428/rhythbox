package wzy.hkta.rhythbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.kozakov.orrery.compose.CalendarNavHeader
import me.kozakov.orrery.compose.DefaultDay
import me.kozakov.orrery.compose.HorizontalCalendar
import me.kozakov.orrery.compose.currentDate
import me.kozakov.orrery.compose.rememberCalendarSelectionState
import me.kozakov.orrery.compose.rememberCalendarState
import me.kozakov.orrery.core.Selection
import me.kozakov.orrery.core.SelectionMode

/**
 * 日历选择模式
 */
enum class CalendarSelectMode {
    Single, // 单选日期
    Range   // 日期范围
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarBottomSheet(
    show: Boolean,
    title: String = "选择日期",
    mode: CalendarSelectMode = CalendarSelectMode.Range,
    maxDays: Int = 30,
    onDismissRequest: () -> Unit,
    onConfirm: (Selection) -> Unit
) {
    val today = remember { currentDate() }

    val calendarState = rememberCalendarState()
    val selection = rememberCalendarSelectionState(
        mode = when (mode) {
            CalendarSelectMode.Single -> SelectionMode.Single()
            CalendarSelectMode.Range -> SelectionMode.Range(maxDays = maxDays)
        }
    )

    // 每次打开时清空上一次的选择
    LaunchedEffect(show) {
        if (show) {
            selection.clear()
        }
    }

    if (!show) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        ),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // 标题
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    )
            )

            // 日历导航
            CalendarNavHeader(
                state = calendarState
            )

            // 日历主体
            HorizontalCalendar(
                state = calendarState,
                showWeekNumbers = false,
                dayContent = { day ->
                    DefaultDay(
                        day = day,
                        selectionState = selection,
                        today = today,
                        onClick = {
                        },
                    )
                }
            )

            Spacer(Modifier.padding(top = 12.dp))

            Text(
                text = when {
                    mode == CalendarSelectMode.Single -> {
                        "已选择：${selection.selection.single}"
                    }

                    else -> {
                        val range = selection.selection.range
                        "已选择：${range?.start} 至 ${range?.endInclusive}"
                    }
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = 4.dp,
                    vertical = 8.dp
                )
            )

            // 操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }

                Button(
                    onClick = {
                        onConfirm(selection.selection)
                        onDismissRequest()
                    },
                    enabled = selection.selection.range != null || selection.selection.single != null,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape
                ) {
                    Text("确定")
                }
            }
        }
    }
}