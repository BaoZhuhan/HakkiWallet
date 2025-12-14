package com.example.account.repository

import androidx.lifecycle.LiveData
import com.example.account.db.dao.TransactionDao
import com.example.account.db.dao.TransactionItemDao
import com.example.account.model.Transaction
import javax.inject.Inject

/**
 * 交易记录仓库类
 * 提供交易记录的数据访问方法
 */
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionItemDao: TransactionItemDao
) {

    val transactions: LiveData<List<Transaction>> = transactionDao.readAllTransactions()

    suspend fun addAllTransactions(transactions: List<Transaction>) {
        for (transaction in transactions) {
            // insert transaction row
            transactionDao.addTransaction(transaction)
            // ensure transaction items are saved into transaction_items table
            // set parentTransactionId for each item and insert
            for (item in transaction.items) {
                // parentTransactionId may be missing; ensure it's set to transaction.id
                item.parentTransactionId = transaction.id
                transactionItemDao.addItem(item)
            }
        }
    }

    fun getTransactionById(id: String?): LiveData<Transaction> {
        return transactionDao.getTransactionById(id)
    }

    suspend fun isTransactionIdExists(id: String): Boolean {
        return transactionDao.isTransactionIdExists(id)
    }

    suspend fun createTransaction(transaction: Transaction) {
        transactionDao.addTransaction(transaction)
        // persist items into transaction_items table
        for (item in transaction.items) {
            item.parentTransactionId = transaction.id
            transactionItemDao.addItem(item)
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        // optional: update items handling is left as-is (could be improved later)
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

    fun getCategoryTotals(): LiveData<List<com.example.account.model.CategoryTotal>> {
        return transactionDao.getCategoryTotals()
    }

    // Backfill: if transaction_items table empty, populate it from transactions JSON stored in transactions.items
    suspend fun ensureTransactionItemsPopulated() {
        val count = transactionDao.getTransactionItemsCount()
        if (count > 0) return

        val all = transactionDao.getAllTransactionsList()
        for (t in all) {
            for (item in t.items) {
                item.parentTransactionId = t.id
                transactionItemDao.addItem(item)
            }
        }
    }
}