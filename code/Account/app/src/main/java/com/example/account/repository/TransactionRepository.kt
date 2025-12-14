package com.example.account.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.account.db.dao.TransactionDao
import com.example.account.db.dao.TransactionItemDao
import com.example.account.model.Transaction
import com.example.account.model.CategoryTotalByType
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
            var inserted = 0
            for (item in transaction.items) {
                // parentTransactionId may be missing; ensure it's set to transaction.id
                item.parentTransactionId = transaction.id
                transactionItemDao.addItem(item)
                inserted++
            }
            if (inserted > 0) {
                Log.d("TransactionRepository", "Inserted $inserted items for transaction ${transaction.id}")
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
        var inserted = 0
        for (item in transaction.items) {
            item.parentTransactionId = transaction.id
            transactionItemDao.addItem(item)
            inserted++
        }
        if (inserted > 0) Log.d("TransactionRepository", "createTransaction: inserted $inserted items for ${transaction.id}")
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

    fun getCategoryTotalsByType(): LiveData<List<CategoryTotalByType>> {
        return transactionDao.getCategoryTotalsByType()
    }

    // Backfill: if transaction_items table empty, populate it from transactions JSON stored in transactions.items
    suspend fun ensureTransactionItemsPopulated() {
        val count = transactionDao.getTransactionItemsCount()
        Log.d("TransactionRepository", "transaction_items current count = $count")
        if (count > 0) return

        val all = transactionDao.getAllTransactionsList()
        var totalInserted = 0
        for (t in all) {
            for (item in t.items) {
                item.parentTransactionId = t.id
                transactionItemDao.addItem(item)
                totalInserted++
            }
        }
        Log.d("TransactionRepository", "ensureTransactionItemsPopulated: inserted total items = $totalInserted")
    }
}