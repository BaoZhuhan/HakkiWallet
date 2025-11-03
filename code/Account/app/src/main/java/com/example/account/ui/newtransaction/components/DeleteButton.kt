package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.account.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colors.surface,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_icon_delete),
            contentDescription = "删除",
            tint = MaterialTheme.colors.error,
            modifier = Modifier
                .size(24.dp)
                .padding(0.dp)
        )
    }
}