package com.example.account.ui.shared

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.account.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete

@Composable
fun TopAppBar(
    showGoBack: Boolean,
    activity: Activity?,
    isDarkTheme: Boolean,
    onToggleTheme: (() -> Unit)? = null,
    // Selection support
    selectedCount: Int = 0,
    isMultiSelect: Boolean = false,
    onDeleteSelected: (() -> Unit)? = null,
    onCancelSelection: (() -> Unit)? = null
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(color = MaterialTheme.colors.surface)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(0.dp, 20.dp, 20.dp, 0.dp))
                    .width(60.dp)
                    .height(60.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colors.primary,
                                MaterialTheme.colors.primaryVariant
                            ),
                            startY = 5.0f
                        )
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logo), "标志",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp)
                        .align(Alignment.Center)
                )
            }
            // If selection active (multi-select mode), show selection toolbar actions
            if (isMultiSelect) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${selectedCount} 已选", modifier = Modifier.padding(end = 8.dp))
                    IconButton(onClick = { onDeleteSelected?.invoke() }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                    IconButton(onClick = { onCancelSelection?.invoke() }) {
                        Icon(Icons.Default.Close, contentDescription = "取消")
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Theme toggle button (shown when a toggle handler is provided)
                    if (onToggleTheme != null) {
                        IconButton(onClick = { onToggleTheme.invoke() }) {
                            Icon(
                                painter = if (isDarkTheme) painterResource(R.drawable.ic_icon_moon) else painterResource(R.drawable.ic_icon_sun),
                                contentDescription = "切换主题",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        Divider(
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(0.2.dp)
                        )
                    }

                    if (showGoBack && activity != null) {
                        GoBack(activity)
                    }
                }
            }
        }
    }
}
