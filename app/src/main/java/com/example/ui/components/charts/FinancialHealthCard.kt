package com.example.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialHealthAudit
import com.example.data.model.InsightType
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

/**
 * Visual scorecard for assessing the Financial Health of the General Ledger,
 * showing overall health score, liquidity/solvency ratios, double-entry verification,
 * and automated financial insights.
 */
@Composable
fun FinancialHealthCard(
    audit: FinancialHealthAudit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    val scoreColor = when {
        audit.overallScore >= 80 -> Color(0xFF22C55E) // Green
        audit.overallScore >= 60 -> Color(0xFF00A3FF) // Blue
        audit.overallScore >= 45 -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444)                     // Red
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .testTag("financial_health_audit_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Double-Entry Integrity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SkyBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HealthAndSafety,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Financial Health of Ledger",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automated Double-Entry GL Audit",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    }
                }

                // Balanced GL status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (audit.isLedgerBalanced) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (audit.isLedgerBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (audit.isLedgerBalanced) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (audit.isLedgerBalanced) "GL Balanced" else "GL Variance",
                            color = if (audit.isLedgerBalanced) Color(0xFF22C55E) else Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Gauge and Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Score Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(68.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1.0f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1E293B),
                        strokeWidth = 6.dp,
                    )
                    CircularProgressIndicator(
                        progress = { (audit.overallScore / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = scoreColor,
                        strokeWidth = 6.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${audit.overallScore}",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "/100",
                            color = TextSubtle,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${audit.rating} Health",
                            color = scoreColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(scoreColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Working capital Rs. ${currencyFormatter.format(audit.workingCapital)} with ${audit.liquidCashRunwayDays} days estimated cash runway.",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Financial Ratios Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "CURRENT RATIO",
                    value = "${String.format(Locale.US, "%.1f", audit.currentRatio)}x",
                    caption = "Ideal: > 1.5x",
                    valueColor = if (audit.currentRatio >= 1.5) Color(0xFF22C55E) else Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "QUICK RATIO",
                    value = "${String.format(Locale.US, "%.1f", audit.quickRatio)}x",
                    caption = "Liquid Coverage",
                    valueColor = SkyBlueBright,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "DEBT / ASSET",
                    value = "${String.format(Locale.US, "%.0f", audit.debtToAssetRatio * 100)}%",
                    caption = "Solvency Risk",
                    valueColor = if (audit.debtToAssetRatio <= 0.4) Color(0xFF22C55E) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "CASH RUNWAY",
                    value = "${audit.liquidCashRunwayDays} Days",
                    caption = "Operating Reserve",
                    valueColor = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f)
                )
            }

            // Automated Ledger Insights
            if (audit.insights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Automated Ledger Insights",
                    color = SkyBlueBright,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                audit.insights.take(3).forEach { insight ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        val iconColor = when (insight.type) {
                            InsightType.POSITIVE -> Color(0xFF22C55E)
                            InsightType.WARNING -> Color(0xFFF59E0B)
                            InsightType.NEUTRAL -> SkyBlueBright
                        }
                        Icon(
                            imageVector = when (insight.type) {
                                InsightType.POSITIVE -> Icons.Default.CheckCircle
                                InsightType.WARNING -> Icons.Default.Warning
                                InsightType.NEUTRAL -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = insight.title,
                                    color = TextWhite,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                insight.metricValue?.let { mv ->
                                    Text(
                                        text = mv,
                                        color = iconColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = insight.description,
                                color = TextSubtle,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    caption: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = TextSubtle, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = caption, color = TextMuted, fontSize = 9.sp)
        }
    }
}
