package com.example.account.model.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.account.model.TransactionItem

/**
 * 交易项目类型转换器
 * 用于在Room数据库中存储和检索交易项目列表
 */
class TransactionItemConverter {
    private val gson = Gson()

    /**
     * 将交易项目列表转换为JSON字符串
     */
    @TypeConverter
    fun fromTransactionItemList(items: List<TransactionItem>?): String? {
        if (items == null) {
            return null
        }
        val type = object : TypeToken<List<TransactionItem>>() {}.type
        return gson.toJson(items, type)
    }

    /**
     * 将JSON字符串转换为交易项目列表
     */
    @TypeConverter
    fun toTransactionItemList(json: String?): List<TransactionItem>? {
        if (json == null) {
            return null
        }
        val type = object : TypeToken<List<TransactionItem>>() {}.type
        return gson.fromJson(json, type)
    }
}