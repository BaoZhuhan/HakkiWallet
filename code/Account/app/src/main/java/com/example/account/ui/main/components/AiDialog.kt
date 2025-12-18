package com.example.account.ui.main.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.account.viewmodel.MainViewModel

@Composable
fun AiDialog(
    mainViewModel: MainViewModel,
    onDismissRequest: () -> Unit
) {
    val ctx = LocalContext.current
    var model by remember { mutableStateOf("nex-agi/deepseek-v3.1-nex-n1:free") }
    var prompt by remember { mutableStateOf("") }

    val aiResponse by remember { mainViewModel.aiResponse }
    val aiLoading by remember { mainViewModel.aiLoading }
    // Debug: show whether an API key is configured and allow setting one at runtime
    val maskedKey = remember { mutableStateOf(mainViewModel.getMaskedApiKeyForDebug() ?: "未配置") }
    var debugApiKey by remember { mutableStateOf("") }

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
                Spacer(modifier = Modifier.height(8.dp))
                // Debug UI: show masked key and an input to set key at runtime
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "API Key: ${maskedKey.value}")
                }
                OutlinedTextField(
                    value = debugApiKey,
                    onValueChange = { debugApiKey = it },
                    label = { Text("Debug API Key (paste here for test)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        if (debugApiKey.isNotBlank()) {
                            mainViewModel.setApiKeyForDebug(debugApiKey)
                            maskedKey.value = mainViewModel.getMaskedApiKeyForDebug() ?: "已保存"
                            try { Toast.makeText(ctx, "API key 已保存 (debug)", Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
                        } else {
                            try { Toast.makeText(ctx, "请输入 API key", Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
                        }
                    }) { Text("保存 API Key (debug)") }
                }
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
                    try { Toast.makeText(ctx, "调用AI：仅显示返回", Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
                    Log.d("AiDialog", "fetchAiResponseStoredKey called with model=$model, promptLen=${prompt.length}")
                    mainViewModel.fetchAiResponseStoredKey(model = model, input = prompt)
                }) { Text("调用并显示") }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = {
                    // 直接调用并保存到 DB，使用已保存的 api key
                    try { Toast.makeText(ctx, "调用AI：调用并保存到DB", Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
                    Log.d("AiDialog", "callAiModelAndIngestStoredKey called with model=$model, promptLen=${prompt.length}")
                    mainViewModel.callAiModelAndIngestStoredKey(model = model, input = prompt)
                    onDismissRequest()
                }) { Text("直接调用并保存") }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = {
                    // 如果已经有返回且是 JSON，可以尝试解析保存
                    aiResponse?.let {
                        try { Toast.makeText(ctx, "尝试解析并保存AI返回", Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
                        Log.d("AiDialog", "ingestAiJson called, respLen=${it.length}")
                        mainViewModel.ingestAiJson(it)
                    }
                    onDismissRequest()
                }) { Text("保存到数据库") }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}
