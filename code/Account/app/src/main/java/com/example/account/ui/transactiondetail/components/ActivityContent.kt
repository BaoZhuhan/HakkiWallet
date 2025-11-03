package com.example.account.ui.transactiondetail.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.example.account.model.Transaction

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun ActivityContent(transaction: Transaction) {
    Column(modifier = Modifier) {
        TransactionTitle(transaction)
        Body(transaction)
    }
}