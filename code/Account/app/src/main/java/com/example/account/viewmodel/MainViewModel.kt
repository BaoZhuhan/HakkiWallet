package com.example.account.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.preference.Preference
import com.example.account.repository.TransactionRepository
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
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
    application: Application,
) : AndroidViewModel(application) {

    private val dataStore = Preference(application)

    val transactions = repository.transactions

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (!dataStore.initData.first()) {
                loadInitialData(application.assets.open("data.json"))
            }
            // Ensure transaction_items table is populated for analysis aggregation
            repository.ensureTransactionItemsPopulated()
        }
    }

    private suspend fun loadInitialData(file: InputStream) {
        try {
            val jsonString = file.bufferedReader().use { it.readText() }
            val gson = Gson()
            val jsonArray = gson.fromJson(jsonString, JsonArray::class.java)

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