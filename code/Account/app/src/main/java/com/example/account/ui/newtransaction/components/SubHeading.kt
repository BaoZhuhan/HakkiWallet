package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SubHeading(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.h1,
        color = MaterialTheme.colors.onBackground,
        textAlign = TextAlign.Start,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}