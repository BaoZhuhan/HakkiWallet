package com.example.account.predictor

import com.example.account.model.MonthlyAggregate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Simple linear regression predictor for monthly aggregated expenses.
 * Uses ordinary least squares on (x, y) where x is months since base and y is total.
 */
class ExpensePredictor {

    data class LinearModel(val intercept: Double, val slope: Double)

    /**
     * Train a linear model on ordered monthly aggregates. Returns (intercept, slope).
     * If input is insufficient or variance is zero, returns slope=0 and intercept=mean.
     */
    fun train(aggregates: List<MonthlyAggregate>): LinearModel {
        if (aggregates.isEmpty()) return LinearModel(0.0, 0.0)
        val sorted = aggregates.sortedBy { it.yearMonth }
        val base = YearMonth.parse(sorted.first().yearMonth)
        val xs = sorted.map { monthsBetween(base, YearMonth.parse(it.yearMonth)).toDouble() }
        val ys = sorted.map { it.total }

        val n = xs.size
        if (n < 2) {
            val meanY = ys.average()
            return LinearModel(meanY, 0.0)
        }

        val meanX = xs.average()
        val meanY = ys.average()

        var covXY = 0.0
        var varX = 0.0
        for (i in xs.indices) {
            val dx = xs[i] - meanX
            covXY += dx * (ys[i] - meanY)
            varX += dx * dx
        }

        if (abs(varX) < 1e-9) {
            return LinearModel(meanY, 0.0)
        }

        val slope = covXY / varX
        val intercept = meanY - slope * meanX
        return LinearModel(intercept, slope)
    }

    /**
     * Predict future months starting from startYearMonth (inclusive) for monthsAhead months.
     * Returns list of Pair("yyyy-MM", predictedValue).
     * If monthsAhead <= 0 or aggregates empty, returns empty list.
     */
    fun predictFutureMonths(
        aggregates: List<MonthlyAggregate>,
        startYearMonth: String,
        monthsAhead: Int
    ): List<Pair<String, Double>> {
        if (monthsAhead <= 0) return emptyList()
        if (aggregates.isEmpty()) return emptyList()

        val model = train(aggregates)
        val sorted = aggregates.sortedBy { it.yearMonth }
        val base = YearMonth.parse(sorted.first().yearMonth)

        val start = YearMonth.parse(startYearMonth)
        val results = mutableListOf<Pair<String, Double>>()
        var cursor = start
        for (i in 0 until monthsAhead) {
            val x = monthsBetween(base, cursor).toDouble()
            var pred = model.intercept + model.slope * x
            if (!pred.isFinite()) pred = 0.0
            if (pred < 0.0) pred = 0.0
            results.add(cursor.toString() to pred)
            cursor = cursor.plusMonths(1)
        }
        return results
    }

    // helper: months between two YearMonth values
    private fun monthsBetween(start: YearMonth, end: YearMonth): Long {
        return start.until(end, ChronoUnit.MONTHS)
    }
}
