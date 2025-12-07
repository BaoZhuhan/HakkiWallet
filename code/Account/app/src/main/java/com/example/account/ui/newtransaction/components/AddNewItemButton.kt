package com.example.account.ui.newtransaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.account.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddNewItemButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = {
            onClick()
        },
        color = MaterialTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_icon_plus),
                contentDescription = "添加项目",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "添加项目",
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.h3,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}