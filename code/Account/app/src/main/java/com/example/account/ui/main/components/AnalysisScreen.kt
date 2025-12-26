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
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp

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
    expenseBreakdown: List<CategorySummary> = emptyList(),
    // new: predicted data passed from parent composable
    predicted: List<Pair<String, Double>> = emptyList(),
    selectedMonths: Int = 3,
    onSelectMonths: (Int) -> Unit = {}
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

            // Legend: make scrollable with a small scrollbar when long
            // replaced the static Column with a Box containing a scrollable Column + scrollbar
            val legendScroll = rememberScrollState()
            var legendContainerHeight by remember { mutableStateOf(0) }

            Box(modifier = Modifier
                .padding(start = 12.dp)
                .heightIn(max = 220.dp)
                .onSizeChanged { legendContainerHeight = it.height }
            ) {
                Column(modifier = Modifier
                    .verticalScroll(legendScroll)
                    .padding(end = 6.dp)
                ) {
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

                if (legendContainerHeight > 0) {
                    VerticalScrollbar(scrollState = legendScroll, containerHeightPx = legendContainerHeight, modifier = Modifier.align(Alignment.TopEnd).padding(end = 2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "分类明细 (${data.size} 项)", style = appBodyStyle().copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colors.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        // Table of categories -- make this list internally scrollable with a max height and scrollbar
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

                // internal scroll area
                val listScroll = rememberScrollState()
                var listContainerHeight by remember { mutableStateOf(0) }

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .onSizeChanged { listContainerHeight = it.height }
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(listScroll)
                        .padding(end = 6.dp)
                    ) {
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

                    if (listContainerHeight > 0) {
                        VerticalScrollbar(scrollState = listScroll, containerHeightPx = listContainerHeight, modifier = Modifier.align(Alignment.TopEnd).padding(end = 2.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // Predicted expenses card
        Card(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "未来 $selectedMonths 个月花费预测", style = appBodyStyle().copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))

                // month selection buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(1, 3, 6)
                    options.forEach { opt ->
                        Button(onClick = { onSelectMonths(opt) }, modifier = Modifier) {
                            Text(text = if (opt == selectedMonths) "$opt 月 (当前)" else "$opt 月")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (predicted.isEmpty()) {
                    Text(text = "暂无预测数据", style = appBodyStyle())
                } else {
                    predicted.forEach { (ym, value) ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = ym, style = appBodyStyle())
                            Text(text = formatAmountDouble(value), style = appBodyStyle())
                        }
                    }
                }
            }
        }
    }
}

// Reusable vertical scrollbar composable that ties to a ScrollState and measured container height
@Composable
private fun VerticalScrollbar(
    scrollState: androidx.compose.foundation.ScrollState,
    containerHeightPx: Int,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
    minThumbHeight: Dp = 24.dp,
    trackColor: Color = Color.LightGray.copy(alpha = 0.25f),
    thumbColor: Color = Color.Gray
) {
    val density = LocalDensity.current
    val minThumbPx = with(density) { minThumbHeight.toPx() }
    val visible = containerHeightPx.toFloat()
    val maxValue = scrollState.maxValue.toFloat()
    val contentHeight = if (maxValue <= 0f) visible else visible + maxValue

    val thumbHeight = if (contentHeight <= 0f) visible else (visible * visible / contentHeight).coerceAtLeast(minThumbPx)
    val trackTravel = (visible - thumbHeight).coerceAtLeast(0f)
    val thumbOffset = if (maxValue <= 0f) 0f else (scrollState.value.toFloat() / maxValue) * trackTravel

    Canvas(modifier = modifier.width(thickness).fillMaxHeight()) {
        // draw track
        drawRoundRect(color = trackColor, cornerRadius = CornerRadius(size.width / 2f, size.width / 2f))
        // draw thumb
        drawRoundRect(color = thumbColor, topLeft = Offset(0f, thumbOffset), size = Size(size.width, thumbHeight), cornerRadius = CornerRadius(size.width / 2f, size.width / 2f))
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
                // internal scroll area for breakdown entries
                val brScroll = rememberScrollState()
                var brContainerHeight by remember { mutableStateOf(0) }

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .onSizeChanged { brContainerHeight = it.height }
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(brScroll)
                    ) {
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

                    if (brContainerHeight > 0) {
                        VerticalScrollbar(scrollState = brScroll, containerHeightPx = brContainerHeight, modifier = Modifier.align(Alignment.TopEnd).padding(end = 2.dp))
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

private fun formatAmountDouble(value: Double): String {
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
    // Replace fixed palette (which repeats every 6) with a stable per-category color derived from the category string
    fun colorFromCategory(category: String): Color {
        // create a stable hue in [0,360) from the category hash
        val h = (kotlin.math.abs(category.hashCode()) % 360).toFloat()
        val s = 0.58f // saturation
        val l = 0.55f // lightness
        return hslToColor(h, s, l)
    }

    val data = totals.map { ct ->
        CategorySummary(ct.category, ct.total, colorFromCategory(ct.category))
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

    val incomeSummary = income.map { CategorySummary(it.category, it.total, colorFromCategory(it.category)) }
    val expenseSummary = expense.map { CategorySummary(it.category, it.total, colorFromCategory(it.category)) }

    // predicted LiveData
    var selectedMonths by remember { mutableStateOf(3) }
    val predicted by viewModel.predictedMonthlyExpenses.observeAsState(initial = emptyList())

    // load predictions whenever selectedMonths changes
    LaunchedEffect(selectedMonths) {
        viewModel.loadPredictedMonthlyExpenses(selectedMonths, startFromNextMonth = true)
    }

    // Delegate to UI-only composable with predicted data
    AnalysisScreenContent(
        modifier = modifier,
        data = data,
        incomeBreakdown = incomeSummary,
        expenseBreakdown = expenseSummary,
        predicted = predicted,
        selectedMonths = selectedMonths,
        onSelectMonths = { selectedMonths = it }
    )
}

// HSL (h in degrees, s & l in 0..1) -> Color (top-level helper so it's accessible everywhere)
private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val hh = (h / 60f) % 6f
    val x = c * (1f - kotlin.math.abs(hh % 2f - 1f))
    var r1 = 0f
    var g1 = 0f
    var b1 = 0f
    when {
        hh < 1f -> { r1 = c; g1 = x; b1 = 0f }
        hh < 2f -> { r1 = x; g1 = c; b1 = 0f }
        hh < 3f -> { r1 = 0f; g1 = c; b1 = x }
        hh < 4f -> { r1 = 0f; g1 = x; b1 = c }
        hh < 5f -> { r1 = x; g1 = 0f; b1 = c }
        else    -> { r1 = c; g1 = 0f; b1 = x }
    }
    val m = l - c / 2f
    val r = (r1 + m).coerceIn(0f, 1f)
    val g = (g1 + m).coerceIn(0f, 1f)
    val b = (b1 + m).coerceIn(0f, 1f)
    return Color(r, g, b, 1f)
}
