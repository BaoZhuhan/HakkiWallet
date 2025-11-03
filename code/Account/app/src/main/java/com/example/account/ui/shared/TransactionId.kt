package com.example.account.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TransactionId(id: String) {
    Column(modifier = Modifier) {
        Text(
            text = "交易编号",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = id,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground
        )
    }
}