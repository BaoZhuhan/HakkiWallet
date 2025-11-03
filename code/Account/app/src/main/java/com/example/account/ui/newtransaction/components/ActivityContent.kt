package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.example.account.viewmodel.NewTransactionViewModel

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun ActivityContent(
    toggleBottomBar: (Boolean) -> Unit,
    newTransactionViewModel: NewTransactionViewModel
) {
    Column(modifier = Modifier) {
        Heading()
        TransactionDetailInput(toggleBottomBar, newTransactionViewModel)
    }
}