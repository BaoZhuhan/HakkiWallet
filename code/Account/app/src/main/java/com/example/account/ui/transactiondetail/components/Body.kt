package com.example.account.ui.transactiondetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.utils.Constants
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun Body(transaction: Transaction) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 交易信息
        TransactionInfoSection(transaction)
    }
}

@Composable
private fun TransactionInfoSection(transaction: Transaction) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        InfoRow(label = "交易日期", value = transaction.transactionDate)
        InfoRow(label = "交易类型", value = getTransactionTypeName(transaction.transactionType))
        InfoRow(label = "交易分类", value = transaction.category)
        InfoRow(label = "交易金额", value = String.format("¥%.2f", calculateTotal(transaction.items)))
        if (transaction.description.isNotEmpty()) {
            InfoRow(label = "交易描述", value = transaction.description)
        }
        InfoRow(label = "交易状态", value = getStatusName(transaction.status))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground
        )
    }
}

private fun getTransactionTypeName(type: String): String {
    return if (type == Constants.INCOME_TYPE) "收入" else "支出"
}

private fun getStatusName(status: String): String {
    return if (status == Constants.ACTIVE) "活跃" else "已归档"
}

private fun calculateTotal(items: List<TransactionItem>): Double {
    return items.sumOf { it.amount.toDouble() }
}