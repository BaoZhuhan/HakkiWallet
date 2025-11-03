package com.example.account.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 交易记录和交易项目的关联数据模型
 * 用于一次性获取交易记录及其所有项目
 */
data class TransactionAndTransactionItems(
    @Embedded
    val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentTransactionId"
    )
    val items: List<TransactionItem>
)