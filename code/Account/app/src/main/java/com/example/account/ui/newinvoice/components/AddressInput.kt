package com.example.account.ui.newinvoice.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.account.R

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun AddressInput(
    street: String,
    city: String,
    postCode: String,
    country: String,
    onClickStreet: (String) -> Unit,
    onClickCity: (String) -> Unit,
    onClickPostCode: (String) -> Unit,
    onClickCountry: (String) -> Unit,
    toggleBottomBar: (value: Boolean) -> Unit
) {
    Column {
        CustomTextInput(
            header = stringResource(id = R.string.input_street_address),
            value = street,
            modifier = Modifier.fillMaxWidth(),
            toggleBottomBar = toggleBottomBar,
            onClick = onClickStreet
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            CustomTextInput(
                header = stringResource(id = R.string.input_city),
                value = city,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 15.dp),
                toggleBottomBar = toggleBottomBar,
                onClick = onClickCity
            )
            CustomTextInput(
                header = stringResource(id = R.string.input_postal_code),
                value = postCode,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 15.dp),
                toggleBottomBar = toggleBottomBar,
                onClick = onClickPostCode
            )
        }
        CustomTextInput(
            header = stringResource(id = R.string.input_country),
            value = country,
            modifier = Modifier.fillMaxWidth(),
            toggleBottomBar = toggleBottomBar,
            onClick = onClickCountry
        )
    }
}