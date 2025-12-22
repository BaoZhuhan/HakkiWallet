package com.example.account.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.account.model.TransactionAndTransactionItems
import com.example.account.model.TransactionItem

/**
 * 交易项目DAO接口
 * 提供交易项目的数据库操作方法
 */
@Dao
interface TransactionItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItem(item: TransactionItem)

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionWithItems(id: String): LiveData<TransactionAndTransactionItems>

    @Update
    suspend fun updateItem(item: TransactionItem)

    @Delete
    suspend fun deleteItem(item: TransactionItem)
}