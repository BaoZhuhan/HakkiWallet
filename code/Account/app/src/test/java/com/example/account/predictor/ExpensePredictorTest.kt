package com.example.account.predictor

import com.example.account.model.MonthlyAggregate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class ExpensePredictorTest {

    private val predictor = ExpensePredictor()

    @Test
    fun train_linearData_returnsExpectedSlopeIntercept() {
        // create perfectly linear data: total = 100 + 10 * monthIndex
        val base = YearMonth.of(2025, 1)
        val data = (0..5).map { i ->
            MonthlyAggregate(base.plusMonths(i.toLong()).toString(), 100.0 + 10.0 * i)
        }

        val model = predictor.train(data)
        // slope should be 10.0 per month, intercept around 100.0
        assertEquals(10.0, model.slope, 1e-6)
        assertEquals(100.0, model.intercept, 1e-6)
    }

    @Test
    fun predictFutureMonths_returnsCorrectCountAndMonths() {
        val base = YearMonth.of(2025, 1)
        val data = (0..5).map { i ->
            MonthlyAggregate(base.plusMonths(i.toLong()).toString(), 200.0)
        }

        val predicted = predictor.predictFutureMonths(data, YearMonth.of(2025, 7).toString(), 3)
        assertEquals(3, predicted.size)
        assertEquals("2025-07", predicted[0].first)
        assertEquals("2025-08", predicted[1].first)
        assertEquals("2025-09", predicted[2].first)
    }
}

