package com.example.account.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.model.Transaction
import com.example.account.preference.Preference
import com.example.account.repository.TransactionRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository,
    application: Application,
) : AndroidViewModel(application) {

    private val dataStore = Preference(application)

    val transactions = repository.transactions

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (!dataStore.initData.first()) {
                loadInitialData(application.assets.open("data.json"))
            }
            // Ensure transaction_items table is populated for analysis aggregation
            repository.ensureTransactionItemsPopulated()
        }
    }

    private suspend fun loadInitialData(file: InputStream) {
        lateinit var jsonString: String
        try {
            jsonString = file.bufferedReader()
                .use { it.readText() }
            val listCountryType = object : TypeToken<List<Transaction>>() {}.type
            val transactions: List<Transaction> = Gson().fromJson(jsonString, listCountryType)
            repository.addAllTransactions(transactions)
            dataStore.updateInitData(true)
        } catch (ioException: IOException) {
            Log.d("loadInitialData", "Could not load initial data; $ioException")
        }
    }
}