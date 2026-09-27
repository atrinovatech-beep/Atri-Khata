package com.example.ui.components.charts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.data.model.AccountTimelinePoint
import com.example.data.model.TimelineMetricType
import com.example.ui.theme.CardBorder
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale

data class ChartSeriesDefinition(
    val name: String,
    val color: Color,
    val valueSelector: (AccountTimelinePoint) -> Double,
    val hasAreaFill: Boolean = true
)

/**
 * Pure Jetpack Compose custom chart with Recharts' signature visual design:
 * smooth Bézier spline curves, translucent vertical gradients, interactive crosshair
 * scrubbing, and responsive tooltips for tracking account balances over time.
 */
@Composable
fun RechartsComposeChart(
    timelineData: List<AccountTimelinePoint>,
    metricType: TimelineMetricType,
    selectedAccountName: String = "Cash in Hand",
    modifier: Modifier = Modifier
) {
    if (timelineData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No ledger balance entries for selected period", color = TextSubtle, fontSize = 13.sp)
        }
        return
    }

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    val netWorthColor = Color(0xFF00A3FF)
    val scrubColor = Color(0xFF38BDF8).copy(alpha = 0.8f)

    // Configure series definitions according to the active metric type
    val seriesList = remember(metricType, selectedAccountName) {
        when (metricType) {
            TimelineMetricType.ASSETS_VS_LIABILITIES -> listOf(
                ChartSeriesDefinition("Assets", Color(0xFF22C55E), { it.totalAssets }, true),
                ChartSeriesDefinition("Liabilities", Color(0xFFEF4444), { it.totalLiabilities }, true),
                ChartSeriesDefinition("Net Worth", netWorthColor, { it.netWorth }, false)
            )
            TimelineMetricType.LIQUID_CASH -> listOf(
                ChartSeriesDefinition("Cash & Bank", Color(0xFF00A3FF), { it.liquidCash }, true)
            )
            TimelineMetricType.WORKING_CAPITAL -> listOf(
                ChartSeriesDefinition("Receivables (AR)", Color(0xFF10B981), { it.accountsReceivable }, true),
                ChartSeriesDefinition("Payables (AP)", Color(0xFFF59E0B), { it.accountsPayable }, true),
                ChartSeriesDefinition("Working Capital", Color(0xFFA855F7), { it.workingCapital }, false)
            )
            TimelineMetricType.NET_WORTH -> listOf(
                ChartSeriesDefinition("Net Worth (Equity)", Color(0xFF38BDF8), { it.netWorth }, true)
            )
            TimelineMetricType.ACCOUNT_DETAIL -> listOf(
                ChartSeriesDefinition(
                    selectedAccountName,
                    Color(0xFF14B8A6),
                    { it.accountBalances[selectedAccountName] ?: 0.0 },
                    true
                )
            )
        }
    }

    var selectedIndex by remember(timelineData) { mutableIntStateOf(timelineData.size - 1) }
    var isScrubbing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_compose_chart_root")
    ) {
        // Chart Header & Interactive Legends
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Legends
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                seriesList.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(s.color)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(s.name, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Live Scrub Indicator / Info
            Text(
                text = if (isScrubbing) "Tracking Balance" else "Touch to Inspect",
                color = if (isScrubbing) SkyBlueBright else TextSubtle,
                fontSize = 11.sp,
                fontWeight = if (isScrubbing) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Selected Data Point Tooltip Card
        val currentPoint = timelineData.getOrNull(selectedIndex)
        if (currentPoint != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${currentPoint.dateLabel} (${currentPoint.dateBs.ifBlank { currentPoint.dateAd }})",
                            color = SkyBlueBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Net: Rs. ${currencyFormatter.format(currentPoint.netWorth)}",
                            color = if (currentPoint.netWorth >= 0) Color(0xFF22C55E) else Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        seriesList.forEach { s ->
                            val v = s.valueSelector(currentPoint)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(s.name, color = TextSubtle, fontSize = 10.sp)
                                Text(
                                    text = "Rs. ${currencyFormatter.format(v)}",
                                    color = s.color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Canvas Recharts Area/Line View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(timelineData) {
                    detectTapGestures(
                        onPress = { offset ->
                            isScrubbing = true
                            val chartW = size.width
                            val step = chartW / timelineData.size.coerceAtLeast(1)
                            val idx = (offset.x / step).toInt().coerceIn(0, timelineData.size - 1)
                            selectedIndex = idx
                            tryAwaitRelease()
                            isScrubbing = false
                        }
                    )
                }
                .pointerInput(timelineData) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isScrubbing = true
                            val chartW = size.width
                            val step = chartW / timelineData.size.coerceAtLeast(1)
                            val idx = (offset.x / step).toInt().coerceIn(0, timelineData.size - 1)
                            selectedIndex = idx
                        },
                        onDragEnd = { isScrubbing = false },
                        onDragCancel = { isScrubbing = false },
                        onDrag = { change, _ ->
                            val chartW = size.width
                            val step = chartW / timelineData.size.coerceAtLeast(1)
                            val idx = (change.position.x / step).toInt().coerceIn(0, timelineData.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val bottomPadding = 24.dp.toPx()
                val chartH = height - bottomPadding

                if (timelineData.isEmpty()) return@Canvas

                // Compute overall peak value for dynamic scale
                var maxVal = 10000.0
                var minVal = 0.0
                seriesList.forEach { s ->
                    timelineData.forEach { pt ->
                        val v = s.valueSelector(pt)
                        if (v > maxVal) maxVal = v
                        if (v < minVal) minVal = v
                    }
                }
                maxVal *= 1.15
                val range = (maxVal - minVal).coerceAtLeast(1.0)

                // 1. Cartesian Grid Lines (4 dashed lines)
                val gridPaint = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                for (i in 0..4) {
                    val y = chartH * (1f - (i / 4f))
                    drawLine(
                        color = Color(0xFF1E2F4D),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f,
                        pathEffect = gridPaint
                    )
                }

                val stepX = if (timelineData.size > 1) width / (timelineData.size - 1) else width / 2f

                // 2. Draw each series
                seriesList.forEach { s ->
                    val points = timelineData.mapIndexed { i, pt ->
                        val x = if (timelineData.size > 1) i * stepX else width / 2f
                        val norm = (s.valueSelector(pt) - minVal) / range
                        val y = chartH - (norm * chartH).toFloat()
                        Offset(x, y.coerceIn(0f, chartH))
                    }

                    if (points.isNotEmpty()) {
                        // Smooth cubic Bézier spline curve
                        val splinePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
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

                        // Gradient Area Fill under curve
                        if (s.hasAreaFill) {
                            val areaPath = Path().apply {
                                addPath(splinePath)
                                lineTo(points.last().x, chartH)
                                lineTo(points.first().x, chartH)
                                close()
                            }

                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(s.color.copy(alpha = 0.35f), Color.Transparent),
                                    startY = 0f,
                                    endY = chartH
                                ),
                                style = Fill
                            )
                        }

                        // Spline Line Stroke
                        drawPath(
                            path = splinePath,
                            color = s.color,
                            style = Stroke(width = 2.5f)
                        )

                        // Data point dots
                        points.forEach { pt ->
                            drawCircle(color = Color(0xFF0F172A), radius = 3.5.dp.toPx(), center = pt)
                            drawCircle(color = s.color, radius = 2.dp.toPx(), center = pt)
                        }
                    }
                }

                // 3. Interactive Crosshair Scrub Line
                if (selectedIndex in timelineData.indices) {
                    val activeX = if (timelineData.size > 1) selectedIndex * stepX else width / 2f
                    drawLine(
                        color = scrubColor,
                        start = Offset(activeX, 0f),
                        end = Offset(activeX, chartH),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )

                    // Draw glowing highlight dot at active points
                    seriesList.forEach { s ->
                        val v = s.valueSelector(timelineData[selectedIndex])
                        val norm = (v - minVal) / range
                        val activeY = chartH - (norm * chartH).toFloat()
                        drawCircle(
                            color = s.color.copy(alpha = 0.3f),
                            radius = 7.dp.toPx(),
                            center = Offset(activeX, activeY)
                        )
                        drawCircle(
                            color = s.color,
                            radius = 4.dp.toPx(),
                            center = Offset(activeX, activeY)
                        )
                    }
                }
            }

            // X-Axis Date Labels Row under canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                timelineData.forEachIndexed { index, pt ->
                    val shouldShow = timelineData.size <= 7 || (index % 2 == 0) || index == timelineData.size - 1
                    if (shouldShow) {
                        Text(
                            text = pt.dateLabel,
                            color = if (index == selectedIndex) SkyBlueBright else TextMuted,
                            fontSize = 9.5.sp,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
