package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.viewmodel.NewTransactionViewModel

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun TransactionInfoInput(
    newTransactionViewModel: NewTransactionViewModel,
    toggleBottomBar: (Boolean) -> Unit
) {
    // 确保currentTransaction不为null
    val transaction = newTransactionViewModel.currentTransaction ?: Transaction()
    
    // 确保至少有一个TransactionItem用于存储金额
    LaunchedEffect(Unit) {
        if (transaction.items.isEmpty()) {
            transaction.items.add(TransactionItem(
                parentTransactionId = transaction.id,
                name = "交易金额",
                amount = 0f
            ))
        }
    }
    
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        // 交易日期
        CustomCalendarInput(
            label = "交易日期",
            value = transaction.transactionDate,
            onValueChange = { transaction.transactionDate = it }
        )
        // 交易类型（收入/支出）
        CustomDropDownInput(
            label = "交易类型",
            options = listOf("收入", "支出"),
            selectedOption = transaction.transactionType,
            onOptionSelected = { transaction.transactionType = it }
        )
        // 交易分类
        CustomDropDownInput(
            label = "交易分类",
            options = getCategoryOptions(transaction.transactionType),
            selectedOption = transaction.category,
            onOptionSelected = { transaction.category = it }
        )
        // 交易金额
        CustomPriceInput(
            label = "交易金额",
            value = if (transaction.items.isNotEmpty()) {
                val amount = transaction.items[0].amount
                if (amount == 0f) "" else amount.toString()
            } else "",
            onValueChange = { newAmount ->
                try {
                    val amount = if (newAmount.isEmpty()) 0f else (newAmount.toFloatOrNull() ?: 0f)
                    if (transaction.items.isEmpty()) {
                        transaction.items.add(TransactionItem(
                            parentTransactionId = transaction.id,
                            name = "交易金额",
                            amount = amount
                        ))
                    } else {
                        transaction.items[0].amount = amount
                    }
                } catch (e: Exception) {
                    // 保持原值
                }
            },
            modifier = Modifier
        )
        // 交易描述
        CustomTextInput(
            label = "交易描述",
            value = transaction.description,
            onValueChange = { transaction.description = it },
            placeholder = "输入交易描述（可选）"
        )
    }
}

// 根据交易类型获取分类选项
fun getCategoryOptions(transactionType: String): List<String> {
    return when (transactionType) {
        "收入" -> listOf("工资", "奖金", "投资收益", "兼职", "其他收入")
        "支出" -> listOf("餐饮", "交通", "购物", "住房", "娱乐", "医疗", "教育", "其他支出")
        else -> listOf("餐饮", "交通", "购物", "住房", "娱乐", "医疗", "教育", "其他")
    }
}