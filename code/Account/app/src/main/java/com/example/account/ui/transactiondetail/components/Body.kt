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
import java.util.Locale
import com.example.account.ui.theme.appBodyStyle
import com.example.account.ui.theme.appTitleStyle

@Composable
fun Body(transaction: Transaction) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 交易信息
        TransactionInfoSection(transaction)
        
        // 交易项目列表
        TransactionItemsSection(transaction.items)
    }
}

@Composable
private fun TransactionInfoSection(transaction: Transaction) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        InfoRow(label = "交易日期", value = transaction.transactionDate)
        InfoRow(label = "交易类型", value = Constants.transactionTypeLabel(transaction.transactionType))
        InfoRow(label = "交易分类", value = transaction.category)
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
            style = appBodyStyle(),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = appBodyStyle(),
            color = MaterialTheme.colors.onBackground
        )
    }
}

@Composable
private fun TransactionItemsSection(items: List<TransactionItem>) {
    Column {
        Text(
            text = "交易项目",
            style = appTitleStyle(),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        items.forEachIndexed { index, item ->
            ItemRow(
                index = index + 1,
                name = item.name,
                amount = item.amount
            )
        }
        TotalRow(total = calculateTotal(items))
    }
}

@Composable
private fun ItemRow(index: Int, name: String, amount: Float) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Text(
            text = "$index.",
            style = appBodyStyle(),
            modifier = Modifier.width(30.dp)
        )
        Text(
            text = name,
            style = appBodyStyle(),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "¥${amount.toString()}",
            style = appBodyStyle()
        )
    }
}

@Composable
private fun TotalRow(total: Double) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp)
        .border(1.dp, MaterialTheme.colors.onBackground.copy(alpha = 0.3f))
        .padding(top = 12.dp)) {
        Text(
            text = "总计",
            style = appTitleStyle(),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = String.format(Locale.getDefault(), "¥%.2f", total),
            style = appTitleStyle(),
            color = MaterialTheme.colors.primary
        )
    }
}


private fun getStatusName(status: String): String {
    return if (status == Constants.ACTIVE) "活跃" else "已归档"
}

private fun calculateTotal(items: List<TransactionItem>): Double {
    return items.sumOf { it.amount.toDouble() }
}