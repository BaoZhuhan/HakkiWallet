package com.example.account.ui.transactiondetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.account.ui.transactiondetail.components.ActivityContent
import com.example.account.ui.transactiondetail.components.BottomBar
import com.example.account.ui.shared.ActivityTemplate
import com.example.account.viewmodel.TransactionDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class TransactionDetailActivity : ComponentActivity() {

    private val transactionDetailViewModel: TransactionDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val transaction = transactionDetailViewModel.getTransactionById(intent.getStringExtra("id"))
                .observeAsState(null)
            ActivityTemplate(
                content = {
                    transaction.value?.let {
                        ActivityContent(it)
                    }
                },
                bottomBar = {
                    transaction.value?.let {
                        BottomBar(it, transactionDetailViewModel, this)
                    }
                },
                showGoBack = true,
                activity = this
            )
        }
    }
}