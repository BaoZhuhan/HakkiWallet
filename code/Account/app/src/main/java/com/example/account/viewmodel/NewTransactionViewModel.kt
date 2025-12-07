package com.example.account.viewmodel

import android.app.Application
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
    var currentTransaction: Transaction? = null

    /**
     * 设置交易数据，如果为null则创建新交易
     */
    fun setTransactionData(transaction: Transaction?) {
        currentTransaction = transaction ?: Transaction()
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