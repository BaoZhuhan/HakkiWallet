package com.example.account.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.account.ui.newtransaction.NewTransactionActivity
import com.example.account.ui.shared.ActivityTemplate
import com.example.account.ui.main.components.ActivityContent
import com.example.account.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ActivityTemplate(
                content = {
                    ActivityContent(this, mainViewModel)
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        startActivity(Intent(this, NewTransactionActivity::class.java))
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Bill")
                    }
                }
            )
        }
    }
}