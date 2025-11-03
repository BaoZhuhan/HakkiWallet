package com.example.account.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.account.model.Transaction
import com.example.account.repository.TransactionRepository
import com.example.account.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 交易详情视图模型
 * 处理交易记录详情页面的业务逻辑
 */
@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val repository: TransactionRepository,
    application: Application,
) : AndroidViewModel(application) {

    fun getTransactionById(id: String?): LiveData<Transaction> = repository.getTransactionById(id)

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            repository.deleteTransaction(transaction)
        }
    }

    fun archiveTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            transaction.status = Constants.ARCHIVED
            repository.archiveTransaction(transaction)
        }
    }
}