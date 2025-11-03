package com.example.account.ui.transactiondetail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.ui.shared.TransactionId

@Composable
fun TransactionTitle(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransactionId(transaction.id)
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.h2,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}