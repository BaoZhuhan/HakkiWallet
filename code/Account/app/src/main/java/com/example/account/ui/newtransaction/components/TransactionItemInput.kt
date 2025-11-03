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
    // 确保currentTransaction不为null
    val transaction = newTransactionViewModel.currentTransaction ?: Transaction()
    
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
                    onValueChange = { item.name = it },
                    placeholder = "项目名称",
                    modifier = Modifier.weight(3f)
                )
                CustomPriceInput(
                    label = "",
                    value = item.amount.toString(),
                    onValueChange = { 
                        try {
                            item.amount = it.toFloatOrNull() ?: 0f
                        } catch (e: Exception) {
                            item.amount = 0f
                        }
                    },
                    modifier = Modifier.weight(2f)
                )
                DeleteButton(
                    onClick = {
                        transaction.items.removeAt(index)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 添加新项目按钮
        AddNewItemButton(
            onClick = {
                // 添加新的交易项目
                if (transaction.id.isNotEmpty()) {
                    transaction.items.add(TransactionItem(parentTransactionId = transaction.id))
                }
            },
            toggleBottomBar = toggleBottomBar
        )
    }
}