package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.account.R

@Composable
fun CustomDropDownInput(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = selectedOption,
                onValueChange = { /* 不允许直接输入，只能通过下拉选择 */ },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_icon_arrow_down),
                            contentDescription = "展开选项",
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

        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(
                        text = option,
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}