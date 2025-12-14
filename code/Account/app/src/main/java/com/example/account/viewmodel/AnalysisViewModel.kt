package com.example.account.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.account.model.CategoryTotal
import com.example.account.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
}
