package com.example.account.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.account.db.dao.TransactionDao
import com.example.account.db.dao.TransactionItemDao
import com.example.account.model.Transaction
import com.example.account.model.TransactionItem
import com.example.account.model.converter.TransactionItemConverter

/**
 * 交易数据库类
 * 管理交易记录和交易项目的数据存储
 */
@Database(
    entities = [Transaction::class, TransactionItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TransactionItemConverter::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun getTransactionDao(): TransactionDao
    abstract fun getTransactionItemDao(): TransactionItemDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getDatabase(context: Context): TransactionDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transactions_db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}