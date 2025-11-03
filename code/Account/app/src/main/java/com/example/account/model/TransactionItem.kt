package com.example.account.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 交易项目数据模型
 * 代表交易记录中的一个项目
 */
@Entity(
    tableName = "transaction_items",
    foreignKeys = [ForeignKey(
        entity = Transaction::class,
        parentColumns = ["id"],
        childColumns = ["parentTransactionId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
    )]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @SerializedName("name") var name: String = "",
    @SerializedName("amount") var amount: Float = 0.00f,
    @SerializedName("note") var note: String = "",
    @ColumnInfo(index = true) var parentTransactionId: String
) : Serializable