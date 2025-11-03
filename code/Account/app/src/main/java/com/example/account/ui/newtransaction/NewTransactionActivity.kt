package com.example.account.ui.newtransaction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.account.model.Transaction
import com.example.account.ui.newtransaction.components.ActivityContent
import com.example.account.ui.newtransaction.components.BottomBar
import com.example.account.ui.shared.ActivityTemplate
import com.example.account.viewmodel.NewTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class NewTransactionActivity : ComponentActivity() {

    private val newTransactionViewModel: NewTransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
            newTransactionViewModel.setTransactionData(
                intent.getSerializableExtra("transaction") as Transaction?
            )

            ActivityTemplate(
                content = {
                    ActivityContent(
                        toggleBottomBar = {
                            bottomBarState.value = it
                        },
                        newTransactionViewModel = newTransactionViewModel
                    )
                },
                showGoBack = true,
                activity = this,
                bottomBar = {
                    if (bottomBarState.value) {
                        BottomBar(this, newTransactionViewModel)
                    }
                }
            )
        }
    }
}