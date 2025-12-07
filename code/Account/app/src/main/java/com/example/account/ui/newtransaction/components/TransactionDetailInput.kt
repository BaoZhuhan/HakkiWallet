package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.viewmodel.NewTransactionViewModel

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun TransactionDetailInput(
    toggleBottomBar: (Boolean) -> Unit,
    newTransactionViewModel: NewTransactionViewModel
) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        SubHeading(title = "交易详情")
        TransactionInfoInput(newTransactionViewModel)
        SubHeading(title = "交易项目")
        ItemListHeader()
        TransactionItemInput(newTransactionViewModel, toggleBottomBar)
        CustomTotal(newTransactionViewModel)
    }
}