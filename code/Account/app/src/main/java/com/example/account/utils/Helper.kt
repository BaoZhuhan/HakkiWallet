package com.example.account.utils

import com.example.account.model.TransactionItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * 生成新的交易ID
 * 格式为：2个大写字母 + 4个数字
 */
fun getNewTransactionId(): String {
    val alphabets: CharRange = ('A'..'Z')
    val numbers: CharRange = ('0'..'9')
    val prefix: String = List(2) { alphabets.random() }.joinToString("")
    val suffix: String = List(4) { numbers.random() }.joinToString("")
    return prefix + suffix
}

/**
 * 将交易日期转换为数据库存储格式
 */
fun getTransactionDateForDbFormat(transactionDate: String): String {
    return try {
        val originalFormat = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.CHINA)
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = originalFormat.parse(transactionDate)
        val newFormat = SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.CHINA)
        newFormat.format(calendar.time)
    } catch (e: Exception) {
        ""
    }
}

/**
 * 将数据库格式的交易日期转换为显示格式
 */
fun getTransactionDate(transactionDate: String): String {
    return try {
        val originalFormat = SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.CHINA)
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = originalFormat.parse(transactionDate)
        val newFormat = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.CHINA)
        newFormat.format(calendar.time)
    } catch (e: Exception) {
        ""
    }
}

/**
 * 计算交易项目的总金额
 */
fun getTransactionTotal(items: List<TransactionItem>): Float {
    return items.sumOf { it.amount.toDouble() }.toFloat()
}

/**
 * 计算交易项目的总金额（兼容现有代码）
 */
fun getTotal(items: List<TransactionItem>): Float {
    return getTransactionTotal(items)
}

/**
 * 获取当前日期，格式为数据库存储格式
 */
fun getCurrentDateForDb(): String {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.CHINA)
    return dateFormat.format(calendar.time)
}

/**
 * 获取当前日期，格式为显示格式
 */
fun getCurrentDateDisplay(): String {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.CHINA)
    return dateFormat.format(calendar.time)
}