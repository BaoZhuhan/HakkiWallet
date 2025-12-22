package com.example.account.ui.main.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.viewmodel.MainViewModel

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun ActivityContent(context: Context, mainViewModel: MainViewModel, onAiClick: () -> Unit) {
    val transactions by mainViewModel.transactions.observeAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(top = 30.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            TransactionHeader()
            Buttons(
                modifier = Modifier.align(Alignment.CenterVertically),
                context = context,
                onAiClick = onAiClick,
                onPreNavigate = { mainViewModel.exitMultiSelect() }
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Body(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                transactions = transactions,
                context = context,
                mainViewModel = mainViewModel
            )
        }
    }
}
