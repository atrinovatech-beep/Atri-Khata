package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MonthlyLedgerData
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MonthlyIncomeExpenseSection(
    monthlyData: List<MonthlyLedgerData>,
    modifier: Modifier = Modifier
) {
    var selectedMonthIndex by remember { mutableIntStateOf(0) }

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bar Chart Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .testTag("income_expense_barchart_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Monthly Inflow vs. Outflow", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Interactive Double-Entry Breakdown", color = TextSubtle, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(label = "Income", color = Color(0xFF22C55E))
                        LegendDot(label = "Expense", color = Color(0xFFEF4444))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                RechartsBarCanvas(
                    monthlyData = monthlyData,
                    selectedIndex = selectedMonthIndex,
                    onSelectIndex = { selectedMonthIndex = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedMonthIndex in monthlyData.indices) {
                    val sel = monthlyData[selectedMonthIndex]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SkyBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${sel.monthName} (Month ${sel.monthIndex})",
                                    color = SkyBlueBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Net: ${if (sel.netProfit >= 0) "+" else ""}Rs. ${currencyFormatter.format(sel.netProfit)} (${String.format(Locale.US, "%.1f", sel.marginPercent)}%)",
                                    color = if (sel.netProfit >= 0) Color(0xFF22C55E) else Color(0xFFEF4444),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Income", color = TextSubtle, fontSize = 10.sp)
                                    Text("Rs. ${currencyFormatter.format(sel.income)}", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Expense", color = TextSubtle, fontSize = 10.sp)
                                    Text("Rs. ${currencyFormatter.format(sel.expense)}", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Net Margin Trend Curve Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Net Profit Trajectory", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Spline area chart showing operating profit trajectory", color = TextSubtle, fontSize = 11.sp)
                    }

                    Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                RechartsSplineAreaCanvas(monthlyData = monthlyData)
            }
        }

        // Month-by-Month Detailed Table Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "General Ledger Monthly Breakdown",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MONTH", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("INCOME", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    Text("EXPENSE", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    Text("NET PROFIT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                monthlyData.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMonthIndex = index }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.monthName, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f))
                        Text(currencyFormatter.format(item.income), color = Color(0xFF22C55E), fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text(currencyFormatter.format(item.expense), color = Color(0xFFEF4444), fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text(
                            text = "${if (item.netProfit >= 0) "+" else ""}${currencyFormatter.format(item.netProfit)}",
                            color = if (item.netProfit >= 0) SkyBlueBright else Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    if (index < monthlyData.size - 1) {
                        HorizontalDivider(color = CardBorder.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RechartsBarCanvas(
    monthlyData: List<MonthlyLedgerData>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    if (monthlyData.isEmpty()) return

    val maxVal = remember(monthlyData) {
        val peak = monthlyData.maxOfOrNull { maxOf(it.income, it.expense) } ?: 100000.0
        if (peak <= 0.0) 100000.0 else peak * 1.15
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(monthlyData) {
                detectTapGestures { offset ->
                    val chartWidth = size.width
                    val step = chartWidth / monthlyData.size
                    val tappedIdx = (offset.x / step).toInt().coerceIn(0, monthlyData.size - 1)
                    onSelectIndex(tappedIdx)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val bottomPadding = 24.dp.toPx()
            val chartHeight = height - bottomPadding

            val gridPaint = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            for (i in 1..4) {
                val gridY = chartHeight * (1f - (i / 4f))
                drawLine(
                    color = Color(0xFF1E2F4D),
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1f,
                    pathEffect = gridPaint
                )
            }

            drawLine(
                color = Color(0xFF334155),
                start = Offset(0f, chartHeight),
                end = Offset(width, chartHeight),
                strokeWidth = 1.5f
            )

            val groupWidth = width / monthlyData.size
            val barWidth = (groupWidth * 0.34f).coerceAtMost(14.dp.toPx())
            val barSpacing = 3.dp.toPx()

            monthlyData.forEachIndexed { i, data ->
                val groupCenterX = (i * groupWidth) + (groupWidth / 2f)

                if (i == selectedIndex) {
                    drawRoundRect(
                        color = Color(0xFF00A3FF).copy(alpha = 0.08f),
                        topLeft = Offset(i * groupWidth, 0f),
                        size = Size(groupWidth, chartHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                val incomeHeight = (data.income / maxVal * chartHeight).toFloat().coerceAtLeast(3f)
                val incomeLeft = groupCenterX - barWidth - (barSpacing / 2f)
                val incomeTop = chartHeight - incomeHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A)),
                        startY = incomeTop,
                        endY = chartHeight
                    ),
                    topLeft = Offset(incomeLeft, incomeTop),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                val expenseHeight = (data.expense / maxVal * chartHeight).toFloat().coerceAtLeast(3f)
                val expenseLeft = groupCenterX + (barSpacing / 2f)
                val expenseTop = chartHeight - expenseHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF87171), Color(0xFFDC2626)),
                        startY = expenseTop,
                        endY = chartHeight
                    ),
                    topLeft = Offset(expenseLeft, expenseTop),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthlyData.forEachIndexed { index, m ->
                val shortName = m.monthName.take(3)
                Text(
                    text = shortName,
                    color = if (index == selectedIndex) SkyBlueBright else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RechartsSplineAreaCanvas(
    monthlyData: List<MonthlyLedgerData>
) {
    if (monthlyData.isEmpty()) return

    val splineFillColor = SkyBlue.copy(alpha = 0.35f)
    val splineStrokeColor = SkyBlueBright

    val maxAbsNet = remember(monthlyData) {
        val peak = monthlyData.maxOfOrNull { Math.abs(it.netProfit) } ?: 50000.0
        if (peak <= 0.0) 50000.0 else peak * 1.2
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f

            drawLine(
                color = Color(0xFF334155),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            val stepX = width / (monthlyData.size - 1).coerceAtLeast(1)

            val points = monthlyData.mapIndexed { idx, item ->
                val x = idx * stepX
                val normalizedY = (item.netProfit / maxAbsNet).toFloat()
                val y = midY - (normalizedY * (midY * 0.85f))
                Offset(x, y)
            }

            val strokePath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cp1X = prev.x + (curr.x - prev.x) / 2f
                        val cp1Y = prev.y
                        val cp2X = prev.x + (curr.x - prev.x) / 2f
                        val cp2Y = curr.y
                        cubicTo(cp1X, cp1Y, cp2X, cp2Y, curr.x, curr.y)
                    }
                }
            }

            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, midY)
                lineTo(points.first().x, midY)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(splineFillColor, Color.Transparent),
                    startY = 0f,
                    endY = height
                ),
                style = Fill
            )

            drawPath(
                path = strokePath,
                color = splineStrokeColor,
                style = Stroke(width = 2.5f)
            )

            points.forEach { pt ->
                drawCircle(color = Color(0xFF0F172A), radius = 4.dp.toPx(), center = pt)
                drawCircle(color = splineStrokeColor, radius = 2.5.dp.toPx(), center = pt)
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
