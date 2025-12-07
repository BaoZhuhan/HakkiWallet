package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.viewmodel.NewTransactionViewModel

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun TransactionItemInput(
    newTransactionViewModel: NewTransactionViewModel,
    toggleBottomBar: (Boolean) -> Unit
) {
    val transaction = newTransactionViewModel.transaction

    Column(modifier = Modifier) {
        // 显示现有的项目
        transaction.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CustomTextInput(
                    label = "",
                    value = item.name,
                    onValueChange = { newName ->
                        val updatedItems = transaction.items.toMutableList()
                        updatedItems[index] = item.copy(name = newName)
                        newTransactionViewModel.onTransactionChange(transaction.copy(items = updatedItems))
                    },
                    placeholder = "项目名称",
                    modifier = Modifier.weight(3f)
                )
                CustomPriceInput(
                    label = "",
                    value = item.amount.toString(),
                    onValueChange = { newAmount ->
                        val updatedItems = transaction.items.toMutableList()
                        updatedItems[index] = item.copy(amount = newAmount.toFloatOrNull() ?: 0f)
                        newTransactionViewModel.onTransactionChange(transaction.copy(items = updatedItems))
                    },
                    modifier = Modifier.weight(2f)
                )
                DeleteButton(
                    onClick = {
                        val updatedItems = transaction.items.toMutableList()
                        updatedItems.removeAt(index)
                        newTransactionViewModel.onTransactionChange(transaction.copy(items = updatedItems))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 添加新项目按钮
        AddNewItemButton(
            onClick = {
                val updatedItems = transaction.items.toMutableList()
                updatedItems.add(TransactionItem(parentTransactionId = transaction.id))
                newTransactionViewModel.onTransactionChange(transaction.copy(items = updatedItems))
            },
            toggleBottomBar = toggleBottomBar
        )
    }
}