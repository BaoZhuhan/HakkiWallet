package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CustomNumberInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        TextField(
            value = value,
            onValueChange = { 
                // 只允许输入数字
                if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                    onValueChange(it)
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.surface,
                focusedIndicatorColor = MaterialTheme.colors.primary,
                unfocusedIndicatorColor = MaterialTheme.colors.onBackground.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colors.primary
            ),
            textStyle = TextStyle(
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.End
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        )
    }
}