package com.example.account.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.account.ui.newtransaction.NewTransactionActivity
import com.example.account.ui.main.components.AnalysisScreen
import com.example.account.ui.shared.ActivityTemplate
import com.example.account.ui.main.components.ActivityContent
import com.example.account.viewmodel.MainViewModel
import com.example.account.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.saveable.rememberSaveable

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = false)

            // Navigation state: 0 = 主页, 1 = 分析
            val selectedIndexState = rememberSaveable { mutableStateOf(0) }

            // Bottom navigation bar composable
            val bottomBar: @Composable () -> Unit = {
                BottomNavigation {
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "主页") },
                        label = { Text("主页") },
                        selected = selectedIndexState.value == 0,
                        onClick = { selectedIndexState.value = 0 }
                    )

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Assessment, contentDescription = "分析") },
                        label = { Text("分析") },
                        selected = selectedIndexState.value == 1,
                        onClick = { selectedIndexState.value = 1 }
                    )
                }
            }


            ActivityTemplate(
                content = {
                    // Switch content by selected index
                    if (selectedIndexState.value == 0) {
                        ActivityContent(this, mainViewModel)
                    } else {
                        AnalysisScreen()
                    }
                },
                bottomBar = bottomBar,
                floatingActionButton = {
                    // Keep FAB but hide it on Analysis screen if desired
                    if (selectedIndexState.value == 0) {
                        FloatingActionButton(onClick = {
                            startActivity(Intent(this, NewTransactionActivity::class.java))
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Bill")
                        }
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { themeViewModel.toggleTheme() }
            )
        }
    }
}