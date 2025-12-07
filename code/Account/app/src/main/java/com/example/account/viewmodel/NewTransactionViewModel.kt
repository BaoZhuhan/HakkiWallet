package com.example.account.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 新建交易视图模型
 * 处理新建和编辑交易记录的业务逻辑
 */
@HiltViewModel
class NewTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    application: Application,
) : AndroidViewModel(application) {

    // 当前正在编辑或创建的交易
    var transaction by mutableStateOf(Transaction())
        private set

    // 是否为新交易
    private var isNew by mutableStateOf(true)

    private var originalTransactionId: String? = null

    /**
     * 设置交易数据，如果为null则创建新交易
     */
    fun setTransactionData(transaction: Transaction?) {
        isNew = transaction == null
        this.transaction = transaction ?: Transaction()
        if (!isNew) {
            originalTransactionId = transaction?.id
        }
    }

    fun onTransactionChange(newTransaction: Transaction) {
        transaction = newTransaction
    }

    fun createTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            repository.createTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            repository.updateTransaction(transaction)
        }
    }

    fun saveTransaction() {
        if (isNew) {
            createTransaction(transaction)
        } else {
            viewModelScope.launch(Dispatchers.IO + NonCancellable) {
                originalTransactionId?.let { oldId ->
                    if (oldId != transaction.id) {
                        repository.updateTransactionId(oldId, transaction.id)
                    }
                }
                repository.updateTransaction(transaction)
            }
        }
    }

    fun addItemToTransaction(transaction: Transaction, item: TransactionItem) {
        item.parentTransactionId = transaction.id
        transaction.items.add(item)
    }

    fun removeItemFromTransaction(transaction: Transaction, item: TransactionItem) {
        transaction.items.remove(item)
    }

    fun calculateTotal(transaction: Transaction): Float {
        return transaction.items.sumOf { it.amount.toDouble() }.toFloat()
    }
}