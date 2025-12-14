package com.example.account.model

/**
 * Represents aggregated total per category and transaction type (income/expense)
 */
data class CategoryTotalByType(
    val type: String,
    val category: String,
    val total: Float
)

