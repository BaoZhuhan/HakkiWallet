package com.example.account.module

import android.content.Context
import androidx.room.Room
import com.example.account.db.TransactionDatabase
import com.example.account.db.dao.TransactionDao
import com.example.account.db.dao.TransactionItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTransactionDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        TransactionDatabase::class.java,
        "transactions_db"
    ).build()

    @Singleton
    @Provides
    fun provideTransactionDao(db: TransactionDatabase): TransactionDao = db.getTransactionDao()

    @Singleton
    @Provides
    fun provideTransactionItemDao(db: TransactionDatabase): TransactionItemDao = db.getTransactionItemDao()

}