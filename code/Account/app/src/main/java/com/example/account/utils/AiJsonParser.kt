package com.example.account.utils

import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.google.gson.Gson
import com.google.gson.JsonElement

/**
 * 解析来自 AI 的 JSON 响应，并将其映射为应用内的 Transaction 列表。
 * 约定：AI 返回 JSON 可以是单个对象或对象数组，每个对象的字段兼容 Transaction 模型（id, createdAt/created_at, description, category, transactionType, items）
 */
object AiJsonParser {

    private val gson = Gson()

    fun parse(jsonString: String): List<Transaction> {
        val result = mutableListOf<Transaction>()
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return result

        val elem: JsonElement = try { gson.fromJson(trimmed, JsonElement::class.java) } catch (_: Exception) { return result }

        if (elem.isJsonArray) {
            val arr = elem.asJsonArray
            for (e in arr) {
                parseObjectSafe(e)?.let { result.add(it) }
            }
        } else {
            parseObjectSafe(elem)?.let { result.add(it) }
        }

        return result
    }

    private fun parseObjectSafe(elem: JsonElement): Transaction? {
        if (!elem.isJsonObject) return null
        val obj = elem.asJsonObject

        val id = when {
            obj.has("id") && !obj.get("id").isJsonNull -> obj.get("id").asString
            obj.has("_id") && !obj.get("_id").isJsonNull -> obj.get("_id").asString
            else -> getNewTransactionId()
        }

        val createdAt = when {
            obj.has("createdAt") && !obj.get("createdAt").isJsonNull -> obj.get("createdAt").asString
            obj.has("created_at") && !obj.get("created_at").isJsonNull -> obj.get("created_at").asString
            else -> ""
        }

        val description = if (obj.has("description") && !obj.get("description").isJsonNull) obj.get("description").asString else ""

        val category = if (obj.has("category") && !obj.get("category").isJsonNull) obj.get("category").asString else "其他"

        val transactionType = if (obj.has("transactionType") && !obj.get("transactionType").isJsonNull) obj.get("transactionType").asString else Constants.EXPENSE_TYPE

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

                var amount = 0.0f
                try {
                    if (itObj.has("amount") && !itObj.get("amount").isJsonNull) {
                        amount = itObj.get("amount").asFloat
                    } else if (itObj.has("total") && !itObj.get("total").isJsonNull) {
                        amount = itObj.get("total").asFloat
                    } else if (itObj.has("price") && !itObj.get("price").isJsonNull) {
                        val price = try { itObj.get("price").asFloat } catch (_: Exception) { 0.0f }
                        val qty = if (itObj.has("quantity") && !itObj.get("quantity").isJsonNull) try { itObj.get("quantity").asFloat } catch (_: Exception) { 1.0f } else 1.0f
                        amount = price * qty
                    }
                } catch (_: Exception) { amount = 0.0f }

                items.add(TransactionItem(name = name, amount = amount, note = "", parentTransactionId = id))
            }
        }

        return Transaction(
            id = id,
            transactionDate = createdAt,
            description = description,
            category = category,
            payeeName = "",
            transactionType = transactionType,
            status = Constants.ACTIVE,
            items = items
        )
    }
}
