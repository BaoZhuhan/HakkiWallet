package com.example.account.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.account.viewmodel.MainViewModel

@Composable
fun AiDialog(
    mainViewModel: MainViewModel,
    onDismissRequest: () -> Unit
) {
    var model by remember { mutableStateOf("nex-agi/deepseek-v3.1-nex-n1:free") }
    var prompt by remember { mutableStateOf("") }

    val aiResponse by remember { mainViewModel.aiResponse }
    val aiLoading by remember { mainViewModel.aiLoading }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "AI 对话") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Model")
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Prompt / Input") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (aiLoading) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                    }
                }

                aiResponse?.let { resp ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "AI 返回:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                        Text(text = resp, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    mainViewModel.clearAiResponse()
                    onDismissRequest()
                }) { Text("关闭") }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = {
                    // 仅调用并显示返回文本，使用已保存的 api key
                    mainViewModel.fetchAiResponseStoredKey(model = model, input = prompt)
                }) { Text("调用并显示") }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = {
                    // 直接调用并保存到 DB，使用已保存的 api key
                    mainViewModel.callAiModelAndIngestStoredKey(model = model, input = prompt)
                    onDismissRequest()
                }) { Text("直接调用并保存") }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = {
                    // 如果已经有返回且是 JSON，可以尝试解析保存
                    aiResponse?.let { mainViewModel.ingestAiJson(it) }
                    onDismissRequest()
                }) { Text("保存到数据库") }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}
