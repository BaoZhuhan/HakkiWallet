package com.example.account.ui.main.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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

// Simple data model for analysis summary per category
data class CategorySummary(
    val category: String,
    val amount: Float,
    val color: Color
)

// UI-only composable for Analysis screen content
@Composable
fun AnalysisScreenContent(
    modifier: Modifier = Modifier,
    data: List<CategorySummary> = sampleData()
) {
    val total = remember(data) { data.sumOf { it.amount.toDouble() }.toFloat() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "账单分析", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
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
                Text(text = "图例", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                data.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier
                            .size(12.dp)
                            .background(color = item.color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.category)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "分类明细", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Table of categories
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                // header
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "分类", fontWeight = FontWeight.Bold)
                    Text(text = "金额", fontWeight = FontWeight.Bold)
                }
                Divider()

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(data) { item ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = item.category)
                            val percent = if (total <= 0f) 0f else item.amount / total * 100f
                            Text(text = "${formatAmount(item.amount)}  (${String.format(Locale.getDefault(), "%.1f", percent)}%)")
                        }
                    }
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
    val totals by viewModel.categoryTotals.observeAsState(initial = emptyList())

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

    // Delegate to UI-only composable
    AnalysisScreenContent(modifier = modifier, data = data)
}
