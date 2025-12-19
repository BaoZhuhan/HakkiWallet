package com.example.account.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * 简单的 AI HTTP 客户端（用于调用像 BigModel / OpenRouter 等响应 API）
 * 注意：不要在代码中硬编码 API Key；将其从安全存储传入此函数。
 */
object AiHttpClient {

    // Default timeouts (ms). Increased from 15s to 60s to avoid premature timeouts on slower AI backends.
    private const val DEFAULT_CONNECT_TIMEOUT_MS = 60_000
    private const val DEFAULT_READ_TIMEOUT_MS = 60_000

    /**
     * 向模型服务发送请求并返回解析到的字符串回复。
     * 使用的请求体遵循类似于 BigModel chat/completions 的格式：
     * {
     *   "model":"...",
     *   "messages":[{"role":"user","content":"..."}],
     *   ...
     * }
     *
     * 响应解析优先级（尽可能兼容多种实现）：
     * 1) choices[0].message.content (string)
     * 2) choices[0].message.content.text / content.parts[0] / output_text
     * 3) 返回原始响应字符串
     */
    suspend fun requestResponse(
        apiKey: String,
        model: String,
        input: String,
        temperature: Double = 1.0,
        topP: Double = 0.95,
        doSample: Boolean = false,
        stream: Boolean = false,
        // optional per-call overrides (milliseconds). If null or non-positive, defaults are used.
        connectTimeoutMs: Int? = null,
        readTimeoutMs: Int? = null
    ): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw IllegalArgumentException("API key must not be blank")

        val endpoint = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
        val url = URL(endpoint)
        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true // ensure we can read responses
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                // use provided overrides when valid, otherwise fall back to defaults
                connectTimeout = connectTimeoutMs?.takeIf { it > 0 } ?: DEFAULT_CONNECT_TIMEOUT_MS
                readTimeout = readTimeoutMs?.takeIf { it > 0 } ?: DEFAULT_READ_TIMEOUT_MS
            }

            // Build JSON body following the provided BigModel example
            val gson = Gson()
            val root = JsonObject()
            root.addProperty("model", model)
            // messages: [{role: "user", content: "..."}]
            val msg = JsonObject()
            msg.addProperty("role", "user")
            msg.addProperty("content", input)
            val messages = JsonArray()
            messages.add(msg)
            root.add("messages", messages)

            root.addProperty("temperature", temperature)
            root.addProperty("stream", stream)
            // thinking object (optional) - enable by default to match example
            val thinking = JsonObject()
            thinking.addProperty("type", "enabled")
            root.add("thinking", thinking)
            root.addProperty("do_sample", doSample)
            root.addProperty("top_p", topP)
            root.addProperty("tool_stream", false)
            val respFormat = JsonObject()
            respFormat.addProperty("type", "text")
            root.add("response_format", respFormat)

            val bodyJson = gson.toJson(root)
            val bodyBytes = bodyJson.toByteArray(Charsets.UTF_8)

            Log.d("AiHttpClient", "POST $endpoint model=$model inputLen=${input.length}")
            Log.i("AiHttpClient", "request body preview: ${bodyJson.take(1000)}")

            try {
                conn.setFixedLengthStreamingMode(bodyBytes.size)
            } catch (_: Throwable) {
                // ignore; not critical
            }

            conn.outputStream.use { os ->
                os.write(bodyBytes)
                os.flush()
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val resp = stream?.bufferedReader()?.use { it.readText() } ?: ""

            Log.d("AiHttpClient", "response code=$code respLen=${resp.length}")
            if (resp.isBlank()) {
                Log.w("AiHttpClient", "Empty response body from AI endpoint (code=$code)")
                return@withContext null
            } else {
                Log.i("AiHttpClient", "raw response: ${resp.take(2000)}")
            }

            return@withContext try {
                val rootObj = gson.fromJson(resp, JsonObject::class.java)

                // Flexible extractor that attempts several common locations
                fun extractFromChoices(obj: JsonObject): String? {
                    if (!obj.has("choices") || !obj.get("choices").isJsonArray) return null
                    val arr = obj.getAsJsonArray("choices")
                    if (arr.size() == 0) return null
                    val first = arr[0].asJsonObject
                    // choices[].message.content (string)
                    if (first.has("message") && first.get("message").isJsonObject) {
                        val message = first.getAsJsonObject("message")
                        if (message.has("content") && message.get("content") is JsonPrimitive) {
                            val prim = message.getAsJsonPrimitive("content")
                            if (prim.isString) return prim.asString
                        }
                        // content could be object with text field
                        if (message.has("content") && message.get("content").isJsonObject) {
                            val cobj = message.getAsJsonObject("content")
                            if (cobj.has("text") && cobj.get("text").isJsonPrimitive) return cobj.getAsJsonPrimitive("text").asString
                        }
                    }
                    // choices[].message.content might sometimes be an array
                    if (first.has("message") && first.get("message").isJsonObject) {
                        val message = first.getAsJsonObject("message")
                        if (message.has("content") && message.get("content").isJsonArray) {
                            val carr = message.getAsJsonArray("content")
                            if (carr.size() > 0) {
                                val el = carr[0]
                                if (el.isJsonPrimitive && el.asJsonPrimitive.isString) return el.asString
                                if (el.isJsonObject) {
                                    val elobj = el.asJsonObject
                                    if (elobj.has("text") && elobj.get("text").isJsonPrimitive) return elobj.getAsJsonPrimitive("text").asString
                                    if (elobj.has("parts") && elobj.get("parts").isJsonArray) {
                                        val parts = elobj.getAsJsonArray("parts")
                                        if (parts.size() > 0 && parts[0].isJsonPrimitive) return parts[0].asString
                                    }
                                }
                            }
                        }
                    }

                    // fallback: choices[].message (string)
                    if (first.has("message") && first.get("message").isJsonPrimitive) {
                        val prim = first.getAsJsonPrimitive("message")
                        if (prim.isString) return prim.asString
                    }

                    // fallback: choices[].content
                    if (first.has("content") && first.get("content").isJsonPrimitive) return first.getAsJsonPrimitive("content").asString

                    return null
                }

                // Try several spots in order
                extractFromChoices(rootObj)
                    ?: if (rootObj.has("output_text") && rootObj.get("output_text").isJsonPrimitive) rootObj.getAsJsonPrimitive("output_text").asString else null
                    ?: run {
                        // older OpenRouter style: output[0].content[0].text
                        try {
                            if (rootObj.has("output") && rootObj.get("output").isJsonArray) {
                                val outArr = rootObj.getAsJsonArray("output")
                                if (outArr.size() > 0) {
                                    val firstOut = outArr[0].asJsonObject
                                    if (firstOut.has("content") && firstOut.get("content").isJsonArray) {
                                        val c = firstOut.getAsJsonArray("content")[0].asJsonObject
                                        if (c.has("text") && c.get("text").isJsonPrimitive) return@run c.getAsJsonPrimitive("text").asString
                                    }
                                }
                            }
                            null
                        } catch (_: Exception) { null }
                    } ?: resp

            } catch (ex: Exception) {
                Log.w("AiHttpClient", "Failed to parse response JSON: ${ex.message}")
                resp
            }
        } catch (io: Exception) {
            Log.e("AiHttpClient", "Network error when calling AI endpoint: ${io.message}")
            throw io
        } finally {
            conn?.disconnect()
        }
    }
}
