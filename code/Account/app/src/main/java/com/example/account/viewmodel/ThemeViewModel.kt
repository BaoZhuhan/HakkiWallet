package com.example.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.preference.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isDarkTheme: Flow<Boolean> = userPreferences.isDarkTheme

    fun toggleTheme() {
        viewModelScope.launch {
            userPreferences.toggleTheme()
        }
    }
}

