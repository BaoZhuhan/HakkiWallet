package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.utils.Constants
import com.example.account.viewmodel.NewTransactionViewModel

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun TransactionInfoInput(newTransactionViewModel: NewTransactionViewModel) {
    val transaction = newTransactionViewModel.transaction

    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        // 交易日期
        CustomCalendarInput(
            label = "交易日期",
            value = transaction.transactionDate,
            onValueChange = { newTransactionViewModel.onTransactionChange(transaction.copy(transactionDate = it)) }
        )
        // 交易描述
        CustomTextInput(
            label = "交易描述",
            value = transaction.description,
            onValueChange = { newTransactionViewModel.onTransactionChange(transaction.copy(description = it)) },
            placeholder = "输入交易描述"
        )
        // 交易类型（收入/支出）
        CustomDropDownInput(
            label = "交易类型",
            options = listOf("收入", "支出"),
            selectedOption = transaction.transactionType,
            onOptionSelected = { newTransactionViewModel.onTransactionChange(transaction.copy(transactionType = it)) }
        )
        // 交易分类
        CustomDropDownInput(
            label = "交易分类",
            options = getCategoryOptions(transaction.transactionType),
            selectedOption = transaction.category,
            onOptionSelected = { newTransactionViewModel.onTransactionChange(transaction.copy(category = it)) }
        )
        // 交易状态
        CustomDropDownInput(
            label = "交易状态",
            options = listOf("活跃", "已归档"),
            selectedOption = if (transaction.status == Constants.ACTIVE) "活跃" else "已归档",
            onOptionSelected = {
                val newStatus = if (it == "活跃") Constants.ACTIVE else Constants.ARCHIVED
                newTransactionViewModel.onTransactionChange(transaction.copy(status = newStatus))
            }
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