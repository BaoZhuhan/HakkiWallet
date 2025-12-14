package com.example.account.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.account.model.Transaction

/**
 * 交易记录DAO接口
 * 提供交易记录的数据库操作方法
 */
@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions")
    fun readAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id=:id")
    fun getTransactionById(id: String?): LiveData<Transaction>

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE id = :id)")
    suspend fun isTransactionIdExists(id: String): Boolean

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET id = :newId WHERE id = :oldId")
    suspend fun updateTransactionId(oldId: String, newId: String)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE transactionType = :type")
    fun getTransactionsByType(type: String): LiveData<List<Transaction>>

    // Sum amounts from transaction_items grouped by transaction category
    @Query("SELECT t.category as category, SUM(i.amount) as total FROM transactions t JOIN transaction_items i ON t.id = i.parentTransactionId GROUP BY t.category")
    fun getCategoryTotals(): LiveData<List<com.example.account.model.CategoryTotal>>

    // New: return full list (suspending) for backfilling
    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<Transaction>

    // New: count transaction_items rows
    @Query("SELECT COUNT(*) FROM transaction_items")
    suspend fun getTransactionItemsCount(): Int

    // New: get totals grouped by transactionType and category (for income/expense per category)
    @Query("SELECT t.transactionType as type, t.category as category, SUM(i.amount) as total FROM transactions t JOIN transaction_items i ON t.id = i.parentTransactionId GROUP BY t.transactionType, t.category")
    fun getCategoryTotalsByType(): LiveData<List<com.example.account.model.CategoryTotalByType>>
}