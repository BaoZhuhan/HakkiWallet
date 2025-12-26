package com.example.account.model

/**
 * Represents an aggregated total for a calendar month.
 * yearMonth is in format "yyyy-MM", e.g. "2025-12".
 */
data class MonthlyAggregate(
    val yearMonth: String,
    val total: Double
)

