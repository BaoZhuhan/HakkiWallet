package com.example.account.ui.shared

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.account.ui.theme.AccountTheme

@Composable
fun ActivityTemplate(
    content: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    showGoBack: Boolean = false,
    activity: Activity? = null,
    isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    onToggleTheme: (() -> Unit)? = null,
    // Selection support
    selectedCount: Int = 0,
    isMultiSelect: Boolean = false,
    onDeleteSelected: (() -> Unit)? = null,
    onCancelSelection: (() -> Unit)? = null,
    // Whether to show the bottom-left chat icon. Caller should pass `selectedIndex == 0` to show it only on home.
    showChat: Boolean = true,
    // Optional callback for opening the chat/AI dialog. Parent can supply and control visibility.
    onOpenChat: (() -> Unit)? = null
) {
    AccountTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    showGoBack,
                    activity,
                    isDarkTheme,
                    onToggleTheme,
                    selectedCount = selectedCount,
                    isMultiSelect = isMultiSelect,
                    onDeleteSelected = onDeleteSelected,
                    onCancelSelection = onCancelSelection
                )
            },
            bottomBar = {
                bottomBar()
            },
            floatingActionButton = floatingActionButton,
            content = { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    content()

                    // Show a bottom-left chat icon only when caller allows it and there's no explicit GoBack
                    if (showChat && !showGoBack) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            FloatingActionButton(
                                onClick = {
                                    // Delegate opening to parent if provided
                                    onOpenChat?.invoke()
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Icon(Icons.Filled.Chat, contentDescription = "对话")
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background)
        )
    }
}