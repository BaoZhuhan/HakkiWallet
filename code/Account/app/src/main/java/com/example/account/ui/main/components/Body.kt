package com.example.account.ui.main.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.model.Transaction
import com.example.account.utils.Constants
import com.example.account.viewmodel.MainViewModel

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun Body(modifier: Modifier, transactions: List<Transaction>?, context: Context, mainViewModel: MainViewModel) {
    if (transactions?.isNotEmpty() == true) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Constants.outerPadding),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            items(transactions) { transaction ->
                val selectedIds = mainViewModel.selectedIds.value
                val isSelected = selectedIds.contains(transaction.id)

                // Only supply onClickToggle when selection mode is active; otherwise allow click to open detail
                val onClickToggleParam: (() -> Unit)? = if (mainViewModel.selectedIds.value.isNotEmpty()) {
                    { mainViewModel.toggleSelection(transaction.id) }
                } else null

                TransactionCard(
                    transaction,
                    modifier = Modifier.animateItemPlacement(),
                    isSelected = isSelected,
                    onLongPressToggle = { mainViewModel.toggleSelection(transaction.id) },
                    onClickToggle = onClickToggleParam
                )
            }
        }
    } else {
        // if no transactions show placeholder
        NoTransactionBody()
    }
}