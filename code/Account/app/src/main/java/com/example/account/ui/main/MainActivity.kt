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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import com.example.account.ui.main.components.AiDialog
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

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

            // Pager state for horizontal swiping between pages
            val pagerState = rememberPagerState(initialPage = selectedIndexState.value, pageCount = { 2 })
            val coroutineScope = rememberCoroutineScope()

            // AI Dialog visibility
            // Use explicit MutableState to avoid delegation/operator resolution issues in some Compose/Kotlin setups
            val showAiDialogState = remember { mutableStateOf(false) }

            val context = LocalContext.current
            // Bottom navigation bar composable
            val bottomBar: @Composable () -> Unit = {
                BottomNavigation {
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "主页") },
                        label = { Text("主页") },
                        selected = selectedIndexState.value == 0,
                        onClick = {
                            // If currently in multi-select mode, clear selection before switching
                            if (mainViewModel.selectedIds.value.isNotEmpty()) {
                                mainViewModel.clearSelection()
                            }
                            // animate pager to page 0
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )

                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Assessment, contentDescription = "分析") },
                        label = { Text("分析") },
                        selected = selectedIndexState.value == 1,
                        onClick = {
                            // If currently in multi-select mode, clear selection before switching
                            if (mainViewModel.selectedIds.value.isNotEmpty()) {
                                mainViewModel.clearSelection()
                            }
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )
                }
            }

            // Keep selectedIndexState in sync with pager's current page, and clear selection on page change
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { newPage: Int ->
                    if (mainViewModel.selectedIds.value.isNotEmpty()) {
                        mainViewModel.clearSelection()
                    }
                    selectedIndexState.value = newPage
                }
            }

            ActivityTemplate(
                content = {
                    HorizontalPager(state = pagerState) { page ->
                        if (page == 0) {
                            ActivityContent(this@MainActivity, mainViewModel) { showAiDialogState.value = true }
                        } else {
                            AnalysisScreen()
                        }
                    }
                },
                bottomBar = bottomBar,
                floatingActionButton = {
                    // Keep FAB but hide it on Analysis screen if desired
                    if (selectedIndexState.value == 0) {
                        Row {
                            FloatingActionButton(onClick = {
                                // clear selection if active before opening new screen
                                if (mainViewModel.selectedIds.value.isNotEmpty()) {
                                    mainViewModel.clearSelection()
                                }
                                context.startActivity(Intent(context, NewTransactionActivity::class.java))
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Bill")
                            }

                            Spacer(modifier = androidx.compose.ui.Modifier.width(12.dp))

                            // Chat FAB removed; left-side chat icon overlays this functionality
                        }
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { themeViewModel.toggleTheme() },
                onOpenChat = { showAiDialogState.value = true },
                selectedCount = mainViewModel.selectedIds.value.size,
                // Instead of deleting immediately, ask ViewModel to request confirmation
                onDeleteSelected = { mainViewModel.requestDeleteConfirmation() },
                onCancelSelection = { mainViewModel.clearSelection() }
            )

            // Show AI dialog when requested
            if (showAiDialogState.value) {
                AiDialog(mainViewModel = mainViewModel) { showAiDialogState.value = false }
            }

            // Show delete confirmation dialog when ViewModel requests it
            if (mainViewModel.showDeleteConfirmation.value) {
                AlertDialog(
                    onDismissRequest = { mainViewModel.dismissDeleteConfirmation() },
                    title = { Text(text = "确认删除") },
                    text = { Text(text = "确定要删除选中的 ${mainViewModel.selectedIds.value.size} 条交易吗？此操作无法撤销。") },
                    confirmButton = {
                        Button(
                            onClick = {
                                mainViewModel.deleteSelected()
                                mainViewModel.dismissDeleteConfirmation()
                            },
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text(text = "删除")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { mainViewModel.dismissDeleteConfirmation() }) {
                            Text(text = "取消")
                        }
                    }
                )
            }
        }
    }
}