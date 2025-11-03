package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ItemListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "项目",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.weight(3f)
        )
        Text(
            text = "单价",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
        Text(
            text = "数量",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}