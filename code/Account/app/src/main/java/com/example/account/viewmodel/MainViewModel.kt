package com.example.account.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.preference.Preference
import com.example.account.preference.ApiKeyStore
import com.example.account.repository.TransactionRepository
import com.example.account.utils.AiJsonParser
import com.example.account.utils.AiHttpClient
import com.google.gson.Gson
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.NonCancellable

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val apiKeyStore: ApiKeyStore,
    application: Application,
) : AndroidViewModel(application) {

    private val dataStore = Preference(application)

    val transactions = repository.transactions

    // selection state for multi-select (stores selected transaction ids)
    var selectedIds = mutableStateOf(setOf<String>())
        private set

    // Confirmation dialog state for multi-delete
    var showDeleteConfirmation = mutableStateOf(false)
        private set

    // AI 调用相关状态
    var aiResponse = mutableStateOf<String?>(null)
        private set
    var aiLoading = mutableStateOf(false)
        private set

    // 调试用：在 UI 内显示最近的调试日志，便于快速定位问题（不会持久化）
    var aiDebugLog = mutableStateOf("")
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (!dataStore.initData.first()) {
                loadInitialData(application.assets.open("data.json"))
            }
            // Ensure transaction_items table is populated for analysis aggregation
            repository.ensureTransactionItemsPopulated()
        }
    }

    /** Append a short timestamped message to the in-memory AI debug log shown in UI. */
    fun appendAiDebug(msg: String) {
        try {
            val ts = System.currentTimeMillis()
            val line = "[$ts] $msg\n"
            aiDebugLog.value = aiDebugLog.value + line
            Log.d("MainViewModel", "DEBUGLOG: $msg")
        } catch (_: Throwable) {
            // ignore
        }
    }

    fun clearAiDebugLog() {
        aiDebugLog.value = ""
    }

    // Selection helpers
    fun toggleSelection(id: String) {
        val set = selectedIds.value.toMutableSet()
        if (!set.remove(id)) set.add(id)
        selectedIds.value = set
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    // Request/dismiss confirmation dialog for deleting selected items
    fun requestDeleteConfirmation() {
        if (selectedIds.value.isNotEmpty()) showDeleteConfirmation.value = true
    }

    fun dismissDeleteConfirmation() {
        showDeleteConfirmation.value = false
    }

    fun deleteSelected() {
        val toDelete = selectedIds.value.toList()
        if (toDelete.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            for (id in toDelete) {
                try {
                    // delete by primary key - constructing minimal Transaction entity
                    repository.deleteTransaction(Transaction(id = id))
                } catch (e: Exception) {
                    appendAiDebug("deleteSelected failed for $id: ${e.message}")
                }
            }
            // clear selection on UI thread
            viewModelScope.launch(Dispatchers.Main) { clearSelection() }
        }
    }

    /**
     * 调用 AI 接口并将返回的 JSON 直接写入数据库（会在 IO 线程中执行）。
     * 使用已保存的 API Key（免输入）。
     */
    fun callAiModelAndIngestStoredKey(model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        val apiKey = apiKeyStore.getApiKey() ?: run {
            appendAiDebug("callAiModelAndIngestStoredKey: API key not configured")
            return
        }
        appendAiDebug("callAiModelAndIngestStoredKey: apiKey present? ${!apiKey.isNullOrBlank()} model=$model promptLen=${input.length}")
        callAiModelAndIngest(apiKey, model, input, temperature, topP)
    }

    /**
     * 仅获取 AI 返回的字符串并保存在 aiResponse 状态中，供 UI 显示（不会自动保存到 DB）。
     * 使用已保存的 API Key（免输入）。
     */
    fun fetchAiResponseStoredKey(model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        val apiKey = apiKeyStore.getApiKey() ?: run {
            aiResponse.value = "API key not configured"
            appendAiDebug("fetchAiResponseStoredKey: API key not configured")
            Log.d("MainViewModel", "fetchAiResponseStoredKey: API key not configured")
            return
        }
        appendAiDebug("fetchAiResponseStoredKey: apiKey present? ${!apiKey.isNullOrBlank()} model=$model promptLen=${input.length}")
        fetchAiResponse(apiKey, model, input, temperature, topP)
    }

    /**
     * 调用 AI 接口并将返回的 JSON 直接写入数据库（会在 IO 线程中执行）。
     * apiKey: Bearer token（请从安全存储传入）
     */
    fun callAiModelAndIngest(apiKey: String, model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        if (apiKey.isBlank() || input.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            aiLoading.value = true
            try {
                appendAiDebug("callAiModelAndIngest: starting network call model=$model promptLen=${input.length}")
                Log.d("MainViewModel", "callAiModelAndIngest: starting network call model=$model promptLen=${input.length}")
                val prompt = buildAiJsonPrompt(input)
                appendAiDebug("callAiModelAndIngest: built JSON prompt len=${prompt.length}")
                val resp = AiHttpClient.requestResponse(apiKey = apiKey, model = model, input = prompt, temperature = temperature, topP = topP)
                appendAiDebug("callAiModelAndIngest: got response len=" + (resp?.length ?: 0) + " preview=" + (resp?.take(200) ?: "null"))
                if (!resp.isNullOrBlank()) {
                    try {
                        // Try to parse and ingest synchronously here so caller (UI) can observe aiLoading/aiResponse
                        val parsed = AiJsonParser.parse(resp)

                        // Ensure each parsed transaction has a unique id. If id missing/empty, request one from local ID service.
                        val ensured = mutableListOf<Transaction>()
                        for (p in parsed) {
                            var tx = p
                            if (tx.id.isBlank()) {
                                // attempt to obtain a unique id from local service with retries
                                var newId: String? = null
                                var attempts = 0
                                while (newId.isNullOrBlank() && attempts < 3) {
                                    attempts++
                                    newId = try {
                                        requestNewIdFromLocalService()
                                    } catch (e: Exception) {
                                        appendAiDebug("requestNewIdFromLocalService failed: ${e.message}")
                                        null
                                    }

                                    // If still blank or service failed, fallback to local random generator and ensure uniqueness
                                    if (newId.isNullOrBlank()) {
                                        newId = com.example.account.utils.getNewTransactionId()
                                    }

                                    // verify uniqueness against DB
                                    val exists = try { repository.isTransactionIdExists(newId) } catch (_: Exception) { false }
                                    if (!exists) break else newId = ""
                                }
                                if (newId.isNullOrBlank()) {
                                    // as a last resort, generate until unique (limited attempts)
                                    var candidate: String
                                    var guard = 0
                                    do {
                                        candidate = com.example.account.utils.getNewTransactionId()
                                        guard++
                                    } while (guard < 20 && try { repository.isTransactionIdExists(candidate) } catch (_: Exception) { false })
                                    tx.id = candidate
                                } else {
                                    tx.id = newId
                                }
                            }

                            // ensure all items reference correct parent id
                            for (it in tx.items) {
                                it.parentTransactionId = tx.id
                            }
                            ensured.add(tx)
                        }

                        if (ensured.isNotEmpty()) {
                            repository.addAllTransactions(ensured)
                            aiResponse.value = "已保存 ${ensured.size} 条交易"
                            appendAiDebug("callAiModelAndIngest: ingested ${ensured.size} transactions")
                            Log.d("MainViewModel", "callAiModelAndIngest: ingested ${ensured.size} transactions")
                        } else {
                            aiResponse.value = "AI 未返回可解析的交易"
                            appendAiDebug("callAiModelAndIngest: parsed 0 transactions")
                            Log.w("MainViewModel", "callAiModelAndIngest: parsed 0 transactions")
                        }
                    } catch (e: Exception) {
                        appendAiDebug("callAiModelAndIngest: ingest failed: ${e.message}")
                        aiResponse.value = "解析或保存失败: ${e.message}"
                        Log.d("callAiModelAndIngest", "Ingest failed: $e")
                    }
                } else {
                    aiResponse.value = "AI 返回为空或无内容（请检查 API key / 网络或查看日志）"
                    appendAiDebug("callAiModelAndIngest: AI returned empty response for model=$model inputLen=${input.length}")
                    Log.w("MainViewModel", "AI returned empty response for model=$model inputLen=${input.length}")
                }
            } catch (e: Exception) {
                appendAiDebug("callAiModelAndIngest: AI call failed: ${e.message}")
                Log.d("callAiModelAndIngest", "AI call failed: $e")
                aiResponse.value = "AI 调用或保存失败: ${e.message}"
            } finally {
                aiLoading.value = false
                appendAiDebug("callAiModelAndIngest: finished")
            }
        }
    }

    // Request a new ID string from a local HTTP service running on device/emulator.
    // The service is expected to return plain text id, e.g. "AB1234". If request fails, throw an exception.
    private fun requestNewIdFromLocalService(): String? {
        val hosts = listOf("127.0.0.1", "10.0.2.2", "10.0.3.2")
        for (host in hosts) {
            try {
                val url = java.net.URL("http://$host:8080/generate-id")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 2000
                conn.readTimeout = 2000
                conn.doInput = true
                val code = conn.responseCode
                if (code != 200) {
                    appendAiDebug("requestNewIdFromLocalService: $host returned $code")
                    continue
                }
                val stream = conn.inputStream
                val txt = stream.bufferedReader().use { it.readText() }.trim()
                stream.close()
                if (txt.isNotBlank()) return txt
            } catch (e: Exception) {
                appendAiDebug("requestNewIdFromLocalService exception for host $host: ${e.message}")
            }
        }
        return null
    }

    /**
     * 仅获取 AI 返回的字符串并保存在 aiResponse 状态中，供 UI 显示（不会自动保存到 DB）。
     */
    fun fetchAiResponse(apiKey: String, model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        if (apiKey.isBlank() || input.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                aiLoading.value = true
                // immediate UI feedback so users know a request was sent
                aiResponse.value = "请求已发送，等待返回..."
                appendAiDebug("fetchAiResponse: starting network call model=$model promptLen=${input.length}")
                Log.d("MainViewModel", "fetchAiResponse: starting network call model=$model promptLen=${input.length}")
                val prompt = buildAiJsonPrompt(input)
                appendAiDebug("fetchAiResponse: built JSON prompt len=${prompt.length}")
                val resp = AiHttpClient.requestResponse(apiKey = apiKey, model = model, input = prompt, temperature = temperature, topP = topP)
                appendAiDebug("fetchAiResponse: got response len=" + (resp?.length ?: 0) + " preview=" + (resp?.take(300) ?: "null"))
                Log.d("MainViewModel", "fetchAiResponse: got response len=${resp?.length ?: 0}")
                if (resp.isNullOrBlank()) {
                    aiResponse.value = "AI 返回为空或无内容（请检查 API key / 网络或查看日志）"
                    appendAiDebug("fetchAiResponse: AI returned empty response for model=$model inputLen=${input.length}")
                    Log.w("MainViewModel", "AI返回空响应 model=$model inputLen=${input.length}")
                } else {
                    aiResponse.value = resp
                }
            } catch (e: Exception) {
                appendAiDebug("fetchAiResponse: AI call failed: ${e.message}")
                Log.d("fetchAiResponse", "AI call failed: $e")
                aiResponse.value = "AI call failed: ${e.message}"
            } finally {
                aiLoading.value = false
                appendAiDebug("fetchAiResponse: finished")
            }
        }
    }

    fun clearAiResponse() {
        aiResponse.value = null
    }

    /** Debug helper: return a masked API key string or null if not configured.
     * Not intended for production display of secrets; this masks the middle of the token.
     */
    fun getMaskedApiKeyForDebug(): String? {
        val k = apiKeyStore.getApiKey() ?: return null
        return if (k.length <= 8) "configured" else k.take(4) + "..." + k.takeLast(4)
    }

    /** Debug: save API key at runtime (for testing only) */
    fun setApiKeyForDebug(key: String) {
        try {
            apiKeyStore.setApiKey(key)
            appendAiDebug("setApiKeyForDebug: saved key (masked)=${getMaskedApiKeyForDebug()}")
            Log.d("MainViewModel", "setApiKeyForDebug: saved key (masked)=${getMaskedApiKeyForDebug()}")
            aiResponse.value = "API key 已保存 (debug)"
        } catch (e: Exception) {
            appendAiDebug("setApiKeyForDebug failed: ${e.message}")
            Log.d("MainViewModel", "setApiKeyForDebug failed: $e")
            aiResponse.value = "保存 API key 失败: ${e.message}"
        }
    }

    /**
     * 从 AI 返回的 JSON 字符串中解析交易并保存到数据库。
     * 约定：AI 返回的 JSON 必须是对象或对象数组，字段应与 Transaction/TransactionItem 兼容。
     * 这个方法会在 IO 线程中执行并忽略解析失败的条目。
     */
    fun ingestAiJson(jsonString: String) {
        if (jsonString.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parsed = AiJsonParser.parse(jsonString)
                if (parsed.isNotEmpty()) {
                    repository.addAllTransactions(parsed)
                    appendAiDebug("ingestAiJson: ingested ${parsed.size} transactions")
                } else {
                    appendAiDebug("ingestAiJson: parsed 0 transactions")
                }
            } catch (e: Exception) {
                appendAiDebug("ingestAiJson: Failed to ingest AI JSON: ${e.message}")
                Log.d("ingestAiJson", "Failed to ingest AI JSON: $e")
            }
        }
    }

    private suspend fun loadInitialData(file: InputStream) {
        try {
            val jsonString = file.bufferedReader().use { it.readText() }
            val gson = Gson()
            val jsonArray = gson.fromJson(jsonString, com.google.gson.JsonArray::class.java)

            val mapped = mutableListOf<Transaction>()

            for (elem in jsonArray) {
                if (!elem.isJsonObject) continue
                val obj = elem.asJsonObject

                val id = if (obj.has("id") && !obj.get("id").isJsonNull) obj.get("id").asString else com.example.account.utils.getNewTransactionId()
                val createdAt = when {
                    obj.has("createdAt") && !obj.get("createdAt").isJsonNull -> obj.get("createdAt").asString
                    obj.has("created_at") && !obj.get("created_at").isJsonNull -> obj.get("created_at").asString
                    else -> ""
                }
                val description = if (obj.has("description") && !obj.get("description").isJsonNull) obj.get("description").asString else ""

                // category might be absent in original data; fall back to description or "其他"
                val category = if (obj.has("category") && !obj.get("category").isJsonNull) obj.get("category").asString else "其他"

                // transactionType not present in original invoice-like data; default to expense
                val transactionType = if (obj.has("transactionType") && !obj.get("transactionType").isJsonNull) obj.get("transactionType").asString else com.example.account.utils.Constants.EXPENSE_TYPE

                val items = mutableListOf<TransactionItem>()
                if (obj.has("items") && obj.get("items").isJsonArray) {
                    val itemsArr = obj.getAsJsonArray("items")
                    for (itemElem in itemsArr) {
                        if (!itemElem.isJsonObject) continue
                        val itObj = itemElem.asJsonObject
                        val name = when {
                            itObj.has("name") && !itObj.get("name").isJsonNull -> itObj.get("name").asString
                            itObj.has("title") && !itObj.get("title").isJsonNull -> itObj.get("title").asString
                            else -> "项目"
                        }
                        // determine amount: prefer 'amount', then 'total', then 'price'*'quantity', then 'price'
                        var amount = 0.0f
                        if (itObj.has("amount") && !itObj.get("amount").isJsonNull) {
                            try { amount = itObj.get("amount").asFloat } catch (_: Exception) {}
                        } else if (itObj.has("total") && !itObj.get("total").isJsonNull) {
                            try { amount = itObj.get("total").asFloat } catch (_: Exception) {}
                        } else if (itObj.has("price") && !itObj.get("price").isJsonNull) {
                            val price = try { itObj.get("price").asFloat } catch (_: Exception) { 0.0f }
                            val qty = if (itObj.has("quantity") && !itObj.get("quantity").isJsonNull) try { itObj.get("quantity").asFloat } catch (_: Exception) { 1.0f } else 1.0f
                            amount = price * qty
                        }

                        items.add(TransactionItem(name = name, amount = amount, note = "", parentTransactionId = id))
                    }
                }

                val tx = Transaction(
                    id = id,
                    transactionDate = createdAt,
                    description = description,
                    category = category,
                    payeeName = "",
                    transactionType = transactionType,
                    status = com.example.account.utils.Constants.ACTIVE,
                    items = items
                )

                mapped.add(tx)
            }

            repository.addAllTransactions(mapped)
            dataStore.updateInitData(true)
        } catch (ioException: IOException) {
            Log.d("loadInitialData", "Could not load initial data; $ioException")
        }
    }

    /**
     * 构造用于 AI 的提示词，要求 AI 仅返回与本地数据库模型兼容的 JSON（严格的 JSON，不含任何额外文字）。
     * 输出约定：返回一个 JSON 数组，每个元素为一个交易对象，字段兼容 `Transaction` 和 `TransactionItem` 模型兼容。
     */
    private fun buildAiJsonPrompt(input: String): String {
        // 获取系统当前日期（优先使用 java.time），格式为 YYYY-MM-DD；若不可用则使用系统时间毫秒转换
        val currentDate = try {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Throwable) {
            // Fallback: use millis -> simple date string
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
                sdf.format(java.util.Date(System.currentTimeMillis()))
            } catch (_: Throwable) {
                ""
            }
        }

        val dateLine = if (currentDate.isNotBlank()) "系统当前日期: $currentDate" else ""

        return """
        请注意下面一行：$dateLine

        你是一个 JSON 生成器。请根据下面的原始文本提取交易记录并返回严格的 JSON 数组，数组中每一项为一个交易对象，字段应与本地应用的 Transaction/TransactionItem 模型兼容。

        JSON 格式要求（必须遵守）：
        - 输出必须是纯 JSON（不允许任何说明性文字或多余字符）。
        - 输出应为一个数组（即使只有一条交易，也要放在数组中）。
        - 每个交易对象可以包含如下字段：
          - id (string, 可选)  
            - 如果原始文本中存在明显且可作为唯一标识的 ID，可返回该字段；否则不要生成或填写 id（应用会为缺失的 id 分配唯一 ID）。
          - createdAt (string, 建议使用 ISO 日期或可解析的日期文本)
          - description (string)
          - category (string)
          - transactionType (string, 例如 "EXPENSE" 或 "INCOME")
          - items (array): 每个 item 至少包含 name (string) 和 amount (number)。也可包含 price, quantity 等字段。

        注意：请以上面提供的“系统当前日期”作为参考来推断或填充交易日期字段（createdAt）。若原始文本中包含明确的日期信息，应使用文本中识别到的日期；若无法识别，请使用系统当前日期作为交易日期。

        示例输出：
        [
          {
            "id": "tx123",
            "createdAt": "2025-01-02",
            "description": "餐厅消费",
            "category": "餐饮",
            "transactionType": "EXPENSE",
            "items": [ { "name": "午餐", "amount": 45.6 } ]
          }
        ]

        原始文本：
        $input
        """.trimIndent()
    }
}
