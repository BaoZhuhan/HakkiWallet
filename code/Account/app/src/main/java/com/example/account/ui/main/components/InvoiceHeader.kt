package com.example.account.ui.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.account.R

@Composable
fun InvoiceHeader(num: Int?) {
    var numInvoice = stringResource(id = R.string.no_invoices)
    if (num != null && num > 0) {
        numInvoice = if (num > 1) {
            stringResource(id = R.string.invoice_count_plural, num)
        } else {
            stringResource(id = R.string.invoice_count_singular, num)
        }
    }
    Column {
        Text(
            stringResource(id = R.string.invoices),
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.h1
        )
        Text(
            numInvoice, color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.h4
        )
    }
}