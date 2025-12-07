package com.example.account.ui.newtransaction.components

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.viewmodel.NewTransactionViewModel
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults

@Composable
fun BottomBar(
    activity: Activity,
    newTransactionViewModel: NewTransactionViewModel
) {
    val validationError = newTransactionViewModel.validationError

    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { newTransactionViewModel.dismissValidationError() },
            title = { Text("错误") },
            text = { Text(validationError) },
            confirmButton = {
                Button(onClick = { newTransactionViewModel.dismissValidationError() }) {
                    Text("好的")
                }
            }
        )
    }

    Surface(
        color = MaterialTheme.colors.background,
        elevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            DiscardButton(onClick = { activity.finish() })
            Spacer(modifier = Modifier.width(10.dp))
            SaveButton(
                onClick = {
                    newTransactionViewModel.saveTransaction {
                        activity.finish()
                    }
                }
            )
        }
    }
}

@Composable
fun DiscardButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(5.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.surface
        )
    ) {
        Text(text = "放弃", color = MaterialTheme.colors.onBackground)
    }
}

@Composable
fun SaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(5.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary
        )
    ) {
        Text(text = "保存", color = MaterialTheme.colors.onPrimary)
    }
}