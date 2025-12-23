package com.example.account.ui.main.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.account.viewmodel.AnalysisViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.min
import com.example.account.utils.Constants
import com.example.account.ui.theme.appBodyStyle
import com.example.account.ui.theme.appTitleStyle

// Simple data model for analysis summary per category
data class CategorySummary(
    val category: String,
    val amount: Float,
    val color: Color
)

// UI-only composable for Analysis screen content (clean, no debug)
@Composable
fun AnalysisScreenContent(
    modifier: Modifier = Modifier,
    data: List<CategorySummary> = sampleData(),
    incomeBreakdown: List<CategorySummary> = emptyList(),
    expenseBreakdown: List<CategorySummary> = emptyList()
) {
    val total = remember(data) { data.sumOf { it.amount.toDouble() }.toFloat() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(text = "账单分析", style = appTitleStyle(), color = MaterialTheme.colors.onBackground)
        Spacer(modifier = Modifier.height(12.dp))

        // Pie + legend row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PieChart(
                data = data,
                total = total,
                modifier = Modifier.size(220.dp)
            )

            Column(modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxHeight()) {
                Text(text = "图例", style = appBodyStyle().copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colors.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                data.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier
                            .size(12.dp)
                            .background(color = item.color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.category, style = appBodyStyle(), color = MaterialTheme.colors.onBackground)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "分类明细 (${data.size} 项)", style = appBodyStyle().copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colors.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        // Table of categories
        Card(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colors.surface, elevation = 6.dp) {
            Column(modifier = Modifier.padding(8.dp)) {
                // header
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "分类", style = appBodyStyle().copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colors.onBackground)
                    Text(text = "金额", style = appBodyStyle().copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colors.onBackground)
                }
                Divider()

                Column(modifier = Modifier.fillMaxWidth()) {
                    data.forEach { item ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.04f))
                            .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = item.category, color = MaterialTheme.colors.onBackground, fontWeight = FontWeight.Medium, style = appBodyStyle())
                            val percent = if (total <= 0f) 0f else item.amount / total * 100f
                            Text(text = "${formatAmount(item.amount)}  (${String.format(Locale.getDefault(), "%.1f", percent)}%)", color = MaterialTheme.colors.onBackground, style = appBodyStyle())
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Income & Expense breakdown cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BreakdownCard(title = "收入汇总", breakdown = incomeBreakdown, modifier = Modifier.weight(1f))
            BreakdownCard(title = "支出汇总", breakdown = expenseBreakdown, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BreakdownCard(modifier: Modifier = Modifier, title: String, breakdown: List<CategorySummary>, topN: Int = 5) {
    val total = breakdown.sumOf { it.amount.toDouble() }.toFloat()
    // sort desc
    val sorted = breakdown.sortedByDescending { it.amount }
    val top = if (sorted.size <= topN) sorted else sorted.subList(0, topN)
    val rest = if (sorted.size <= topN) 0f else sorted.subList(topN, sorted.size).sumOf { it.amount.toDouble() }.toFloat()

    val displayList = mutableListOf<CategorySummary>()
    displayList.addAll(top)
    if (rest > 0f) displayList.add(CategorySummary("其他", rest, Color(0xFFBDBDBD)))

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = appBodyStyle().copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            if (displayList.isEmpty()) {
                Text(text = "暂无数据", style = appBodyStyle())
            } else {
                displayList.forEach { entry ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.onSurface.copy(alpha = 0.04f))
                        .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = entry.category, color = MaterialTheme.colors.onBackground, style = appBodyStyle())
                        val percent = if (total <= 0f) 0f else entry.amount / total * 100f
                        Text(text = "${formatAmount(entry.amount)}  (${String.format(Locale.getDefault(), "%.1f", percent)}%)", color = MaterialTheme.colors.onBackground, style = appBodyStyle())
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun PieChart(data: List<CategorySummary>, total: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (total <= 0f) {
            // draw empty circle
            drawCircle(color = Color.LightGray, radius = min(size.width, size.height) / 2f)
            return@Canvas
        }

        var startAngle = -90f
        data.forEach { item ->
            val sweep = (item.amount / total) * 360f
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }
}

private fun formatAmount(value: Float): String {
    return String.format(Locale.getDefault(), "¥%.2f", value)
}

private fun sampleData(): List<CategorySummary> {
    return listOf(
        CategorySummary("餐饮", 320f, Color(0xFFEF5350)),
        CategorySummary("购物", 200f, Color(0xFFAB47BC)),
        CategorySummary("交通", 120f, Color(0xFF29B6F6)),
        CategorySummary("娱乐", 80f, Color(0xFFFFCA28)),
        CategorySummary("其他", 40f, Color(0xFF66BB6A))
    )
}

// Public wrapper to use ViewModel and fetch real data from Room
@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    // trigger backfill to ensure transaction_items exist before displaying summaries
    LaunchedEffect(Unit) {
        viewModel.ensureTransactionItemsPopulated()
    }

    val totals by viewModel.categoryTotals.observeAsState(initial = emptyList())
    val breakdown by viewModel.categoryTotalsByType.observeAsState(initial = emptyList())

    // Map CategoryTotal to CategorySummary with a simple color palette function
    fun paletteColor(index: Int): Color = when (index % 6) {
        0 -> Color(0xFFEF5350)
        1 -> Color(0xFFAB47BC)
        2 -> Color(0xFF29B6F6)
        3 -> Color(0xFFFFCA28)
        4 -> Color(0xFF66BB6A)
        else -> Color(0xFF78909C)
    }

    val data = totals.mapIndexed { index, ct ->
        CategorySummary(ct.category, ct.total, paletteColor(index))
    }

    // Normalize type matching: accept constants and Chinese labels
    fun isIncomeType(type: String?): Boolean {
        return Constants.normalizeTransactionType(type) == Constants.INCOME_TYPE
    }

    fun isExpenseType(type: String?): Boolean {
        return Constants.normalizeTransactionType(type) == Constants.EXPENSE_TYPE
    }

    val income = breakdown.filter { isIncomeType(it.type) }
    val expense = breakdown.filter { isExpenseType(it.type) }

    val incomeSummary = income.mapIndexed { index, it -> CategorySummary(it.category, it.total, paletteColor(index)) }
    val expenseSummary = expense.mapIndexed { index, it -> CategorySummary(it.category, it.total, paletteColor(index + income.size)) }

    // Delegate to UI-only composable
    AnalysisScreenContent(modifier = modifier, data = data, incomeBreakdown = incomeSummary, expenseBreakdown = expenseSummary)
}
