package com.example.account.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.model.CategoryTotal
import com.example.account.model.CategoryTotalByType
import com.example.account.model.Transaction
import com.example.account.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel that exposes aggregated category totals from repository
@HiltViewModel
@Suppress("unused")
class AnalysisViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    init {
        // reference repository to satisfy static analyzer (no-op)
        repository
    }

    // LiveData of CategoryTotal from repository
    val categoryTotals: LiveData<List<CategoryTotal>> = repository.getCategoryTotals()

    // LiveData of CategoryTotalByType (type, category, total)
    val categoryTotalsByType: LiveData<List<CategoryTotalByType>> = repository.getCategoryTotalsByType()

    // Expose raw transactions for a detailed list
    val transactions: LiveData<List<Transaction>> = repository.transactions

    // Trigger a manual ensure/backfill from UI
    fun ensureTransactionItemsPopulated() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureTransactionItemsPopulated()
        }
    }
}
