package com.example.account.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.model.CategoryTotal
import com.example.account.model.CategoryTotalByType
import com.example.account.model.Transaction
import com.example.account.model.MonthlyAggregate
import com.example.account.predictor.ExpensePredictor
import com.example.account.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
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

    // Predicted monthly expenses exposed to UI as List of Pair(yearMonth, predictedAmount)
    private val _predictedMonthlyExpenses = MutableLiveData<List<Pair<String, Double>>>()
    val predictedMonthlyExpenses: LiveData<List<Pair<String, Double>>> = _predictedMonthlyExpenses

    private val predictor = ExpensePredictor()

    // Trigger a manual ensure/backfill from UI
    fun ensureTransactionItemsPopulated() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureTransactionItemsPopulated()
        }
    }

    /**
     * Load historical transactions, aggregate by month, then predict next `monthsAhead` months.
     * startFromNextMonth: if true, start predictions from the month after the last historical month.
     */
    fun loadPredictedMonthlyExpenses(monthsAhead: Int = 3, startFromNextMonth: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllTransactionsList()
            if (all.isEmpty()) {
                _predictedMonthlyExpenses.postValue(emptyList())
                return@launch
            }

            // aggregate by YearMonth (assumes Transaction.transactionDate in yyyy-MM-dd or similar)
            val byMonth = mutableMapOf<YearMonth, Double>()
            for (tx in all) {
                val date = try {
                    LocalDate.parse(tx.transactionDate)
                } catch (e: Exception) {
                    // skip unparsable dates
                    continue
                }
                val ym = YearMonth.from(date)
                // sum item amounts; Transaction doesn't have a direct amount field
                val txTotal = tx.items.sumOf { it.amount.toDouble() }
                byMonth[ym] = (byMonth[ym] ?: 0.0) + txTotal
            }

            val aggregates = byMonth.entries.map { MonthlyAggregate(it.key.toString(), it.value) }.sortedBy { it.yearMonth }

            val start = if (startFromNextMonth) {
                val lastYm = aggregates.lastOrNull()?.yearMonth ?: YearMonth.now().toString()
                YearMonth.parse(lastYm).plusMonths(1).toString()
            } else {
                aggregates.lastOrNull()?.yearMonth ?: YearMonth.now().toString()
            }

            val predicted = predictor.predictFutureMonths(aggregates, start, monthsAhead)
            _predictedMonthlyExpenses.postValue(predicted)
        }
    }
}
