package com.example.account.ui.main.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun Buttons(modifier: Modifier, context: Context, onAiClick: () -> Unit) {
    Row(modifier = modifier) {
        ThemeToggleButton(
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        FilterButton(
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        AddNewButton(modifier = Modifier.align(Alignment.CenterVertically), context)
        // Left-side AI chat icon — call onAiClick when pressed
        IconButton(onClick = {
            try { Toast.makeText(context, "AI 按钮被点击", Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
            Log.d("Buttons", "AI icon clicked - invoking onAiClick")
            onAiClick.invoke()
        }, modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = "AI")
        }
    }
}