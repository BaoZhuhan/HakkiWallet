package com.example.account.ui.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.ui.transactiondetail.TransactionDetailActivity
import com.example.account.utils.Constants
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongPressToggle: (() -> Unit)? = null,
    onClickToggle: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val backgroundModifier = if (isSelected) {
        modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colors.primary.copy(alpha = 0.12f))
    } else {
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    }

    Card(
        modifier = backgroundModifier
            .combinedClickable(
                onClick = {
                    if (onClickToggle != null) {
                        onClickToggle()
                    } else {
                        val intent = Intent(context, TransactionDetailActivity::class.java)
                        intent.putExtra("id", transaction.id)
                        context.startActivity(intent)
                    }
                },
                onLongClick = {
                    onLongPressToggle?.invoke()
                }
            ),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = String.format(Locale.getDefault(), "¥%.2f", calculateTotal(transaction.items)),
                    style = MaterialTheme.typography.h6,
                    color = if (transaction.transactionType == Constants.INCOME_TYPE) {
                        MaterialTheme.colors.primary
                    } else {
                        MaterialTheme.colors.error
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = formatDate(transaction.transactionDate),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Bottom color bar to indicate selection state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    if (isSelected) MaterialTheme.colors.primary
                    else MaterialTheme.colors.onSurface.copy(alpha = 0.06f)
                )
        )
    }
}

private fun calculateTotal(items: List<TransactionItem>): Double {
    return items.sumOf { it.amount.toDouble() }
}

private fun formatDate(dateString: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        return dateString
    }
}