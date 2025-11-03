package com.example.account.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.account.R
import com.example.account.model.enums.InvoiceButton
import com.example.account.ui.theme.getCardOnCardColor

@Composable
fun CustomButton(type: InvoiceButton? = null, modifier: Modifier) {
    val backgroundColor: Color
    val foregroundColor: Color
    val text: String
    val startPadding: Dp
    val endPadding: Dp
    val textModifier = Modifier
    when (type) {
        InvoiceButton.Delete -> {
            backgroundColor = MaterialTheme.colors.error
            foregroundColor = MaterialTheme.colors.onError
            text = stringResource(id = R.string.button_delete)
            startPadding = 20.dp
            endPadding = 20.dp
        }
        InvoiceButton.Edit -> {
            backgroundColor = getCardOnCardColor()
            foregroundColor = MaterialTheme.colors.onBackground
            text = stringResource(id = R.string.button_edit)
            startPadding = 20.dp
            endPadding = 20.dp
        }
        InvoiceButton.MarkAsPaid -> {
            backgroundColor = MaterialTheme.colors.primary
            foregroundColor = MaterialTheme.colors.onPrimary
            text = stringResource(id = R.string.button_mark_as_paid)
            startPadding = 30.dp
            endPadding = 30.dp
        }
        InvoiceButton.Cancel -> {
            backgroundColor = getCardOnCardColor()
            foregroundColor = MaterialTheme.colors.onBackground
            text = stringResource(id = R.string.button_cancel)
            startPadding = 20.dp
            endPadding = 20.dp
        }
        InvoiceButton.Discard -> {
            backgroundColor = MaterialTheme.colors.error
            foregroundColor = MaterialTheme.colors.onError
            text = stringResource(id = R.string.button_discard)
            startPadding = 12.dp
            endPadding = 12.dp
        }
        InvoiceButton.SaveAsDraft -> {
            backgroundColor = getCardOnCardColor()
            foregroundColor = MaterialTheme.colors.onBackground
            text = stringResource(id = R.string.button_save_as_draft)
            startPadding = 12.dp
            endPadding = 12.dp
        }
        InvoiceButton.SaveAndSend -> {
            backgroundColor = MaterialTheme.colors.primary
            foregroundColor = MaterialTheme.colors.onPrimary
            text = stringResource(id = R.string.button_save_and_send)
            startPadding = 12.dp
            endPadding = 12.dp
        }
        InvoiceButton.SaveChanges -> {
            backgroundColor = MaterialTheme.colors.primary
            foregroundColor = MaterialTheme.colors.onPrimary
            text = stringResource(id = R.string.button_save_changes)
            startPadding = 20.dp
            endPadding = 20.dp
        }
        else -> {
            backgroundColor = MaterialTheme.colors.surface
            foregroundColor = MaterialTheme.colors.onSurface
            text = stringResource(id = R.string.button_add_new_item)
            startPadding = 20.dp
            endPadding = 20.dp
            textModifier.fillMaxWidth()
        }
    }
    Box(
        modifier = modifier.background(color = backgroundColor)
    ) {
        Text(
            text,
            color = foregroundColor,
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = textModifier
                .align(Alignment.Center)
                .padding(top = 15.dp, bottom = 15.dp, start = startPadding, end = endPadding)
        )
    }
}