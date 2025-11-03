package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.account.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomCalendarInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // 如果有值，解析日期
    if (value.isNotEmpty()) {
        try {
            calendar.time = dateFormat.parse(value)!!
        } catch (e: Exception) {
            // 日期解析失败，使用当前日期
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        TextField(
            value = value,
            onValueChange = { /* 不允许直接输入，只能通过日期选择器 */ },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_icon_calendar),
                        contentDescription = "选择日期",
                        tint = MaterialTheme.colors.primary
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.surface,
                focusedIndicatorColor = MaterialTheme.colors.primary,
                unfocusedIndicatorColor = MaterialTheme.colors.onBackground.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colors.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        )
    }

    // 简单的日期选择器模拟
    if (showDatePicker) {
        // 在实际应用中，这里应该使用Android的DatePickerDialog
        // 由于Compose的限制，这里只是一个简化版本
        val currentDate = dateFormat.format(calendar.time)
        onValueChange(currentDate)
        showDatePicker = false
    }
}