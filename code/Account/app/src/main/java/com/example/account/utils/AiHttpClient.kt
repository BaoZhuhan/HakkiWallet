package com.example.account.utils

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * 简单的 AI HTTP 客户端（用于调用像 OpenRouter 这样的响应 API）
 * 注意：不要在代码中硬编码 API Key；将其从安全存储传入此函数。
 */
object AiHttpClient {

    /**
     * 向 OpenRouter 风格的 /api/v1/responses 发送请求并返回解析到的字符串回复。
     * @param apiKey Bearer token（不要把真实 key 硬编码在源码中）
     * @param model 模型标识
     * @param input 模型的输入文本
     * @param temperature 可选温度
     * @param topP 可选 top_p
     * @return 响应的文本（优先使用 output_text，其次尝试 output[0].content[0].text），失败时返回原始响应或 null
     */
    suspend fun requestResponse(
        apiKey: String,
        model: String,
        input: String,
        temperature: Double = 0.7,
        topP: Double = 0.9
    ): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw IllegalArgumentException("API key must not be blank")

        val endpoint = "https://openrouter.ai/api/v1/responses"
        val url = URL(endpoint)
        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val bodyMap = mapOf(
                "model" to model,
                "temperature" to temperature,
                "top_p" to topP,
                "input" to input
            )
            val bodyJson = Gson().toJson(bodyMap)

            conn.outputStream.use { os ->
                os.write(bodyJson.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val resp = stream.bufferedReader().use { it.readText() }

            return@withContext try {
                // 尝试解析常见字段
                val gson = Gson()
                val root = gson.fromJson(resp, com.google.gson.JsonObject::class.java)

                if (root.has("output_text") && !root.get("output_text").isJsonNull) {
                    root.get("output_text").asString
                } else if (root.has("output") && root.get("output").isJsonArray) {
                    val arr = root.getAsJsonArray("output")
                    if (arr.size() > 0) {
                        val first = arr[0].asJsonObject
                        if (first.has("content") && first.get("content").isJsonArray) {
                            val cont = first.getAsJsonArray("content")
                            if (cont.size() > 0) {
                                val c0 = cont[0].asJsonObject
                                if (c0.has("text") && !c0.get("text").isJsonNull) c0.get("text").asString else resp
                            } else resp
                        } else resp
                    } else resp
                } else {
                    resp
                }
            } catch (_: Exception) {
                // 解析失败，返回原始响应
                resp
            }
        } finally {
            conn?.disconnect()
        }
    }
}
