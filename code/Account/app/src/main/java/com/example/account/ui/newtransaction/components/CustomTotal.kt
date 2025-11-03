package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.viewmodel.NewTransactionViewModel

@Composable
fun CustomTotal(newTransactionViewModel: NewTransactionViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "总计",
            style = MaterialTheme.typography.h3,
            color = MaterialTheme.colors.onBackground
        )
        Text(
            text = "¥${newTransactionViewModel.calculateTotal(newTransactionViewModel.currentTransaction ?: Transaction())}",
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.onBackground
        )
    }
}