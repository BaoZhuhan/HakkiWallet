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

    // Helpers to convert between internal code and UI label
    fun typeCodeToLabel(typeCode: String): String {
        return if (typeCode == Constants.INCOME_TYPE) "收入" else "支出"
    }
    fun typeLabelToCode(label: String): String {
        return if (label == "收入") Constants.INCOME_TYPE else Constants.EXPENSE_TYPE
    }

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
        // 交易类型（收入/支出） - show labels but store codes
        val currentTypeLabel = if (transaction.transactionType.isBlank()) typeCodeToLabel(Constants.EXPENSE_TYPE) else typeCodeToLabel(transaction.transactionType)
        CustomDropDownInput(
            label = "交易类型",
            options = listOf("收入", "支出"),
            selectedOption = currentTypeLabel,
            onOptionSelected = {
                val code = typeLabelToCode(it)
                newTransactionViewModel.onTransactionChange(transaction.copy(transactionType = code))
            }
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

// 根据交易类型 code 获取分类选项
fun getCategoryOptions(transactionTypeCode: String): List<String> {
    return when (transactionTypeCode) {
        Constants.INCOME_TYPE -> Constants.INCOME_CATEGORIES
        Constants.EXPENSE_TYPE -> Constants.EXPENSE_CATEGORIES
        // If stored value is legacy Chinese label, handle that too
        "收入" -> Constants.INCOME_CATEGORIES
        "支出" -> Constants.EXPENSE_CATEGORIES
        else -> Constants.EXPENSE_CATEGORIES
    }
}