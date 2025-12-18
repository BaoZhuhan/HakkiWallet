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

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val apiKeyStore: ApiKeyStore,
    application: Application,
) : AndroidViewModel(application) {

    private val dataStore = Preference(application)

    val transactions = repository.transactions

    // AI 调用相关状态
    var aiResponse = mutableStateOf<String?>(null)
        private set
    var aiLoading = mutableStateOf(false)
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

    /**
     * 调用 AI 接口并将返回的 JSON 直接写入数据库（会在 IO 线程中执行）。
     * 使用已保存的 API Key（免输入）。
     */
    fun callAiModelAndIngestStoredKey(model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        val apiKey = apiKeyStore.getApiKey() ?: return
        callAiModelAndIngest(apiKey, model, input, temperature, topP)
    }

    /**
     * 仅获取 AI 返回的字符串并保存在 aiResponse 状态中，供 UI 显示（不会自动保存到 DB）。
     * 使用已保存的 API Key（免输入）。
     */
    fun fetchAiResponseStoredKey(model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        val apiKey = apiKeyStore.getApiKey() ?: run {
            aiResponse.value = "API key not configured"
            return
        }
        fetchAiResponse(apiKey, model, input, temperature, topP)
    }

    /**
     * 调用 AI 接口并将返回的 JSON 直接写入数据库（会在 IO 线程中执行）。
     * apiKey: Bearer token（请从安全存储传入）
     */
    fun callAiModelAndIngest(apiKey: String, model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        if (apiKey.isBlank() || input.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = AiHttpClient.requestResponse(apiKey = apiKey, model = model, input = input, temperature = temperature, topP = topP)
                if (!resp.isNullOrBlank()) {
                    // resp may be plain text or JSON string; try ingesting as JSON
                    ingestAiJson(resp)
                }
            } catch (e: Exception) {
                Log.d("callAiModelAndIngest", "AI call failed: $e")
            }
        }
    }

    /**
     * 仅获取 AI 返回的字符串并保存在 aiResponse 状态中，供 UI 显示（不会自动保存到 DB）。
     */
    fun fetchAiResponse(apiKey: String, model: String, input: String, temperature: Double = 0.7, topP: Double = 0.9) {
        if (apiKey.isBlank() || input.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                aiLoading.value = true
                val resp = AiHttpClient.requestResponse(apiKey = apiKey, model = model, input = input, temperature = temperature, topP = topP)
                aiResponse.value = resp
            } catch (e: Exception) {
                Log.d("fetchAiResponse", "AI call failed: $e")
                aiResponse.value = "AI call failed: ${e.message}"
            } finally {
                aiLoading.value = false
            }
        }
    }

    fun clearAiResponse() {
        aiResponse.value = null
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
                }
            } catch (e: Exception) {
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
}