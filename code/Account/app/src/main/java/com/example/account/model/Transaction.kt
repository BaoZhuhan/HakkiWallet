package com.example.account.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.account.utils.Constants
import com.example.account.utils.getNewTransactionId
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 交易记录数据模型
 * 代表用户的一条记账记录
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey @SerializedName("id") var id: String = getNewTransactionId(),
    @SerializedName("createdAt") var transactionDate: String = "",
    @SerializedName("description") var description: String = "",
    @SerializedName("category") var category: String = "",
    @SerializedName("payeeName") var payeeName: String = "",
    @SerializedName("transactionType") var transactionType: String = "",
    @SerializedName("status") var status: String = Constants.ACTIVE,
    @SerializedName("items") var items: MutableList<TransactionItem> = mutableListOf(),
) : Serializable