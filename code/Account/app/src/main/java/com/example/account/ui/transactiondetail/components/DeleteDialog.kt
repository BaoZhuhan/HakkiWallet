package com.example.account.ui.transactiondetail.components

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.viewmodel.TransactionDetailViewModel

@Composable
fun DeleteDialog(
    transaction: Transaction,
    transactionDetailViewModel: TransactionDetailViewModel,
    activity: Activity,
    openDialog: androidx.compose.runtime.MutableState<Boolean>
) {
    val isDeleting = remember { mutableStateOf(false) }
    val deletionError = remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = {
            openDialog.value = false
        },
        title = {
            Text(text = "确认删除")
        },
        text = {
            Column {
                Text(text = "确定要删除此交易记录吗？")
                Text(
                    text = "此操作无法撤销，交易数据将被永久删除。",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (deletionError.value) {
                    Text(
                        text = "删除失败，请重试。",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isDeleting.value) {
                        isDeleting.value = true
                        deletionError.value = false
                        
                        try {
                            transactionDetailViewModel.deleteTransaction(transaction)
                            openDialog.value = false
                            activity.finish()
                        } catch (e: Exception) {
                            deletionError.value = true
                        } finally {
                            isDeleting.value = false
                        }
                    }
                },
                enabled = !isDeleting.value,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error
                )
            ) {
                Text(
                    text = if (isDeleting.value) "删除中..." else "删除",
                    color = MaterialTheme.colors.onError
                )
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    openDialog.value = false
                },
                enabled = !isDeleting.value,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.onSurface
                )
            ) {
                Text(text = "取消")
            }
        }
    )
}