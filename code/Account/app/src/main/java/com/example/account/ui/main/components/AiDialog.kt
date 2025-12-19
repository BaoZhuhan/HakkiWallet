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
import kotlinx.coroutines.launch

@Composable
fun AiDialog(
    mainViewModel: MainViewModel,
    onDismissRequest: () -> Unit
) {
    val ctx = LocalContext.current
    val model = "glm-4.5-air"
    var prompt by remember { mutableStateOf("") }

    // Directly observe ViewModel MutableState so Compose recomposes on changes
    val aiResponse by mainViewModel.aiResponse
    val aiLoading by mainViewModel.aiLoading

    // Local flag to indicate a request was initiated from this dialog instance
    var requested by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Watch for completion and show a transient Toast (success/failure) when request finishes
    LaunchedEffect(requested, aiLoading, aiResponse) {
        if (!requested) return@LaunchedEffect
        // Wait until loading finishes
        if (aiLoading) return@LaunchedEffect

        // Decide success vs failure based on response text heuristics
        val resp = aiResponse ?: ""
        val lower = resp.lowercase()
        val failureKeywords = listOf("未配置", "not configured", "failed", "为空", "失败")
        val isFailure = failureKeywords.any { it in lower }

        val message = if (isFailure || resp.isBlank()) {
            // show a short failure message (include brief reason when available)
            if (resp.isNotBlank()) "AI 操作失败: ${resp.take(100)}" else "AI 操作失败"
        } else {
            "AI 操作成功"
        }

        // Show transient Toast
        try {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
        } catch (_: Throwable) {}

        // clear viewModel response and reset flag, then dismiss dialog
        try { mainViewModel.clearAiResponse() } catch (_: Throwable) {}
        requested = false
        onDismissRequest()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "AI 记账") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("输入") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (aiLoading) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Note: we intentionally no longer show the full AI response here; result is reported via transient Toast.
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
                }) { Text("取消") }

                Spacer(modifier = Modifier.width(8.dp))

                val callEnabled = prompt.isNotBlank() && !aiLoading

                TextButton(onClick = {
                    // Initiate request using stored API key and ingest result into DB
                    Log.d("AiDialog", "callAiModelAndIngestStoredKey called with model=$model, promptLen=${prompt.length}")
                    requested = true
                    scope.launch {
                        try {
                            mainViewModel.callAiModelAndIngestStoredKey(model = model, input = prompt)
                        } catch (e: Exception) {
                            // Immediate failure: show Toast and reset
                            try { Toast.makeText(ctx, "请求发送失败: ${e.message}", Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
                            requested = false
                        }
                    }
                }, enabled = callEnabled) { Text("确认") }

                Spacer(modifier = Modifier.width(8.dp))

            }
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}
