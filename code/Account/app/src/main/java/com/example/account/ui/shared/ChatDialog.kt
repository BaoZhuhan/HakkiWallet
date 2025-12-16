package com.example.account.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("欢迎使用对话，示例回复将回显您的消息。")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI 聊天") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Message list with fixed height so dialog stays reasonable
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    items(messages) { msg ->
                        Text(msg, modifier = Modifier.padding(6.dp))
                        Divider()
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (input.isNotBlank()) {
                            // Local echo behavior; replace with real AI send/receive later
                            messages = messages + ("我: $input")
                            messages = messages + ("AI: 收到 - $input")
                            input = ""
                        }
                    }) {
                        Text("发送")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

