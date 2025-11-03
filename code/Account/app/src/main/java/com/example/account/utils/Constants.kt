package com.example.account.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * 应用常量类
 * 定义记账软件中使用的各种常量
 */
class Constants {
    companion object {
        // UI相关常量
        val cardShape = RoundedCornerShape(10.dp)
        val cardPadding = 20.dp
        val outerPadding = 20.dp
        
        // 交易状态常量
        const val ACTIVE = "active"
        const val ARCHIVED = "archived"
        
        // 交易类型常量
        const val EXPENSE_TYPE = "expense"
        const val INCOME_TYPE = "income"
        
        // 交易分类常量
        val EXPENSE_CATEGORIES = listOf("餐饮", "交通", "购物", "娱乐", "医疗", "教育", "住房", "其他")
        val INCOME_CATEGORIES = listOf("工资", "奖金", "投资", "兼职", "礼金", "其他")
        
        // 日期格式常量
        const val DATE_FORMAT_DB = "yyyy-MM-dd"
        const val DATE_FORMAT_DISPLAY = "yyyy年MM月dd日"
    }
}
