package com.example.account.repository

import androidx.lifecycle.LiveData
import com.example.account.db.dao.TransactionDao
import com.example.account.model.Transaction
import javax.inject.Inject

/**
 * 交易记录仓库类
 * 提供交易记录的数据访问方法
 */
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    val transactions: LiveData<List<Transaction>> = transactionDao.readAllTransactions()

    suspend fun addAllTransactions(transactions: List<Transaction>) {
        for (transaction in transactions) {
            transactionDao.addTransaction(transaction)
        }
    }

    fun getTransactionById(id: String?): LiveData<Transaction> {
        return transactionDao.getTransactionById(id)
    }

    suspend fun createTransaction(transaction: Transaction) {
        transactionDao.addTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun updateTransactionId(oldId: String, newId: String) {
        transactionDao.updateTransactionId(oldId, newId)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun archiveTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }
}