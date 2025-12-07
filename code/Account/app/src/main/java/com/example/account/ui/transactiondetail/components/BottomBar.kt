package com.example.account.ui.transactiondetail.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.account.model.Transaction
import com.example.account.ui.newtransaction.NewTransactionActivity
import com.example.account.viewmodel.TransactionDetailViewModel

@OptIn(ExperimentalComposeApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun BottomBar(
    transaction: Transaction,
    transactionDetailViewModel: TransactionDetailViewModel,
    activity: Activity
) {
    Surface(
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            EditButton(transaction, activity, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            DeleteButton(transaction, transactionDetailViewModel, activity, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalComposeApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun EditButton(transaction: Transaction, activity: Activity, modifier: Modifier = Modifier) {
    Button(
        onClick = {
            activity.finish()
            @Suppress("ExperimentalApi")
            val intent = Intent(activity, NewTransactionActivity::class.java)
            @Suppress("ExperimentalApi")
            intent.putExtra("transaction", transaction)
            ContextCompat.startActivity(activity, intent, null)
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary
        )
    ) {
        Text(text = "编辑", color = MaterialTheme.colors.onPrimary)
    }
}

@OptIn(ExperimentalComposeApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun DeleteButton(
    transaction: Transaction,
    transactionDetailViewModel: TransactionDetailViewModel,
    activity: Activity,
    modifier: Modifier = Modifier
) {
    val openDialog = rememberSaveable { mutableStateOf(false) }
    
    Button(
        onClick = {
            openDialog.value = true
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.error
        )
    ) {
        Text(text = "删除", color = MaterialTheme.colors.onError)
    }
    
    if (openDialog.value) {
        DeleteDialog(transaction, transactionDetailViewModel, activity, openDialog)
    }
}

// DeleteDialog已在单独的DeleteDialog.kt文件中定义，此处不再重复定义