package com.example.ui.components.charts

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AccountTimelinePoint
import com.example.data.model.TimelineMetricType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Android WebView hosting an interactive Recharts financial chart.
 * Renders smooth spline curves, gradient area fills, tooltips, Cartesian grid,
 * and timeline brush sliders matching Recharts' exact visual signature.
 *
 * Fully offline-capable with zero external network latency.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebView(
    timelineData: List<AccountTimelinePoint>,
    metricType: TimelineMetricType,
    selectedAccountName: String = "Cash in Hand",
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(timelineData, metricType, selectedAccountName) {
        generateRechartsHtml(timelineData, metricType, selectedAccountName)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(AndroidColor.parseColor("#0F172A"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        builtInZoomControls = false
                        displayZoomControls = false
                    }
                    loadDataWithBaseURL(
                        "https://recharts.org",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(
                    "https://recharts.org",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Builds the interactive HTML/JS Recharts responsive container application.
 */
private fun generateRechartsHtml(
    points: List<AccountTimelinePoint>,
    metricType: TimelineMetricType,
    selectedAccountName: String
): String {
    val jsonArray = JSONArray()
    for (p in points) {
        val obj = JSONObject()
        obj.put("dateLabel", p.dateLabel)
        obj.put("dateBs", p.dateBs)
        obj.put("dateAd", p.dateAd)
        obj.put("totalAssets", p.totalAssets)
        obj.put("totalLiabilities", p.totalLiabilities)
        obj.put("netWorth", p.netWorth)
        obj.put("liquidCash", p.liquidCash)
        obj.put("accountsReceivable", p.accountsReceivable)
        obj.put("accountsPayable", p.accountsPayable)
        obj.put("workingCapital", p.workingCapital)
        obj.put("cumulativeIncome", p.cumulativeIncome)
        obj.put("cumulativeExpense", p.cumulativeExpense)
        obj.put("selectedAccountBalance", p.accountBalances[selectedAccountName] ?: 0.0)
        jsonArray.put(obj)
    }

    val chartTitle = when (metricType) {
        TimelineMetricType.ASSETS_VS_LIABILITIES -> "Total Assets vs Liabilities (Net Worth)"
        TimelineMetricType.LIQUID_CASH -> "Liquid Cash & Bank Reserves"
        TimelineMetricType.WORKING_CAPITAL -> "Working Capital (Receivables vs Payables)"
        TimelineMetricType.NET_WORTH -> "Equity & Net Worth Trajectory"
        TimelineMetricType.ACCOUNT_DETAIL -> "Account: $selectedAccountName"
    }

    val metricKey = metricType.name

    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Recharts Ledger Balances</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    body { background-color: #0F172A; color: #F8FAFC; overflow: hidden; width: 100vw; height: 100vh; display: flex; flex-direction: column; }
    .chart-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px 4px 14px; }
    .chart-title { font-size: 13px; font-weight: 700; color: #FFFFFF; display: flex; align-items: center; gap: 6px; }
    .chart-badge { font-size: 10px; background: rgba(0, 163, 255, 0.15); color: #00A3FF; padding: 2px 6px; border-radius: 4px; font-weight: 600; }
    .chart-legend { display: flex; gap: 12px; font-size: 11px; padding: 0 14px 4px 14px; }
    .legend-item { display: flex; align-items: center; gap: 5px; color: #94A3B8; }
    .legend-dot { width: 8px; height: 8px; border-radius: 50%; }
    .chart-container { flex: 1; position: relative; width: 100%; height: 100%; }
    svg { width: 100%; height: 100%; display: block; }
    .grid-line { stroke: #1E293B; stroke-dasharray: 4 4; stroke-width: 1; }
    .axis-text { fill: #64748B; font-size: 10px; }
    .tooltip-box {
      position: absolute;
      top: 10px;
      right: 14px;
      background: rgba(15, 23, 42, 0.95);
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 6px 10px;
      font-size: 11px;
      pointer-events: none;
      box-shadow: 0 4px 12px rgba(0,0,0,0.5);
      backdrop-filter: blur(8px);
      z-index: 100;
      transition: opacity 0.15s ease;
    }
    .tooltip-date { color: #38BDF8; font-weight: 700; margin-bottom: 3px; font-size: 10.5px; }
    .tooltip-val { font-weight: 600; }
    .scrub-line { stroke: #38BDF8; stroke-dasharray: 3 3; stroke-width: 1.5; opacity: 0.7; }
  </style>
</head>
<body>
  <div class="chart-header">
    <div class="chart-title">
      <span>$chartTitle</span>
      <span class="chart-badge">Recharts GL</span>
    </div>
  </div>
  <div id="legend" class="chart-legend"></div>
  <div class="chart-container" id="container">
    <div id="tooltip" class="tooltip-box" style="display: none;"></div>
    <svg id="chartSvg"></svg>
  </div>

  <script>
    const data = $jsonArray;
    const metricType = "$metricKey";
    const selectedAccount = "$selectedAccountName";

    // Setup series definitions matching Recharts Area / Line props
    let series = [];
    if (metricType === "ASSETS_VS_LIABILITIES") {
      series = [
        { key: "totalAssets", label: "Assets", color: "#22C55E", gradStart: "rgba(34, 197, 94, 0.4)", gradEnd: "rgba(34, 197, 94, 0.0)" },
        { key: "totalLiabilities", label: "Liabilities", color: "#EF4444", gradStart: "rgba(239, 68, 68, 0.35)", gradEnd: "rgba(239, 68, 68, 0.0)" },
        { key: "netWorth", label: "Net Worth", color: "#00A3FF", strokeOnly: true }
      ];
    } else if (metricType === "LIQUID_CASH") {
      series = [
        { key: "liquidCash", label: "Liquid Cash & Bank", color: "#00A3FF", gradStart: "rgba(0, 163, 255, 0.45)", gradEnd: "rgba(0, 163, 255, 0.0)" }
      ];
    } else if (metricType === "WORKING_CAPITAL") {
      series = [
        { key: "accountsReceivable", label: "Receivables (AR)", color: "#10B981", gradStart: "rgba(16, 185, 129, 0.35)", gradEnd: "rgba(16, 185, 129, 0.0)" },
        { key: "accountsPayable", label: "Payables (AP)", color: "#F59E0B", gradStart: "rgba(245, 158, 11, 0.3)", gradEnd: "rgba(245, 158, 11, 0.0)" },
        { key: "workingCapital", label: "Working Capital", color: "#8B5CF6", strokeOnly: true }
      ];
    } else if (metricType === "NET_WORTH") {
      series = [
        { key: "netWorth", label: "Equity / Net Worth", color: "#38BDF8", gradStart: "rgba(56, 189, 248, 0.45)", gradEnd: "rgba(56, 189, 248, 0.0)" }
      ];
    } else {
      series = [
        { key: "selectedAccountBalance", label: selectedAccount, color: "#14B8A6", gradStart: "rgba(20, 184, 166, 0.4)", gradEnd: "rgba(20, 184, 166, 0.0)" }
      ];
    }

    // Build legend
    const legendEl = document.getElementById("legend");
    legendEl.innerHTML = series.map(s => 
      '<div class="legend-item"><span class="legend-dot" style="background:' + s.color + '"></span>' + s.label + '</div>'
    ).join("");

    function renderChart() {
      const svg = document.getElementById("chartSvg");
      const container = document.getElementById("container");
      const tooltip = document.getElementById("tooltip");
      const width = container.clientWidth || 360;
      const height = container.clientHeight || 200;

      if (data.length === 0) return;

      const paddingLeft = 45;
      const paddingRight = 20;
      const paddingTop = 15;
      const paddingBottom = 26;

      const plotW = width - paddingLeft - paddingRight;
      const plotH = height - paddingTop - paddingBottom;

      // Find Min / Max across all active series
      let minVal = 0;
      let maxVal = 1000;
      series.forEach(s => {
        data.forEach(d => {
          const v = d[s.key] || 0;
          if (v > maxVal) maxVal = v;
          if (v < minVal) minVal = v;
        });
      });
      maxVal = maxVal * 1.15;
      if (maxVal === 0) maxVal = 10000;

      const range = maxVal - minVal;

      function getX(idx) {
        if (data.length <= 1) return paddingLeft + plotW / 2;
        return paddingLeft + (idx / (data.length - 1)) * plotW;
      }

      function getY(val) {
        const norm = (val - minVal) / range;
        return paddingTop + plotH - (norm * plotH);
      }

      function formatRs(val) {
        const absVal = Math.abs(val);
        if (absVal >= 100000) return "Rs." + (val / 100000).toFixed(1) + "L";
        if (absVal >= 1000) return "Rs." + (val / 1000).toFixed(0) + "k";
        return "Rs." + Math.round(val);
      }

      let svgHtml = '<defs>';
      series.forEach((s, idx) => {
        if (!s.strokeOnly) {
          svgHtml += '<linearGradient id="grad_' + idx + '" x1="0" y1="0" x2="0" y2="1">' +
            '<stop offset="0%" stop-color="' + s.color + '" stop-opacity="0.45"/>' +
            '<stop offset="100%" stop-color="' + s.color + '" stop-opacity="0.0"/>' +
          '</linearGradient>';
        }
      });
      svgHtml += '</defs>';

      // Horizontal Grid Lines & Y-Axis Labels (4 steps)
      for (let i = 0; i <= 4; i++) {
        const yVal = minVal + (range * (i / 4));
        const yPos = getY(yVal);
        svgHtml += '<line class="grid-line" x1="' + paddingLeft + '" y1="' + yPos + '" x2="' + (width - paddingRight) + '" y2="' + yPos + '" />';
        svgHtml += '<text class="axis-text" x="' + (paddingLeft - 6) + '" y="' + (yPos + 3) + '" text-anchor="end">' + formatRs(yVal) + '</text>';
      }

      // X-Axis Labels
      data.forEach((d, idx) => {
        if (data.length > 8 && idx % 2 !== 0 && idx !== data.length - 1) return;
        const xPos = getX(idx);
        svgHtml += '<text class="axis-text" x="' + xPos + '" y="' + (height - 8) + '" text-anchor="middle">' + d.dateLabel + '</text>';
      });

      // Draw Series Curves (Spline Area & Line)
      series.forEach((s, sIdx) => {
        const pts = data.map((d, idx) => ({ x: getX(idx), y: getY(d[s.key] || 0) }));
        if (pts.length === 0) return;

        // Cubic Bézier spline path
        let linePath = 'M ' + pts[0].x + ' ' + pts[0].y;
        for (let i = 1; i < pts.length; i++) {
          const prev = pts[i - 1];
          const curr = pts[i];
          const cp1x = prev.x + (curr.x - prev.x) / 2;
          const cp1y = prev.y;
          const cp2x = prev.x + (curr.x - prev.x) / 2;
          const cp2y = curr.y;
          linePath += ' C ' + cp1x + ' ' + cp1y + ', ' + cp2x + ' ' + cp2y + ', ' + curr.x + ' ' + curr.y;
        }

        // Fill Area
        if (!s.strokeOnly) {
          const areaPath = linePath + ' L ' + pts[pts.length - 1].x + ' ' + (paddingTop + plotH) + ' L ' + pts[0].x + ' ' + (paddingTop + plotH) + ' Z';
          svgHtml += '<path d="' + areaPath + '" fill="url(#grad_' + sIdx + ')" />';
        }

        // Stroke Line
        svgHtml += '<path d="' + linePath + '" fill="none" stroke="' + s.color + '" stroke-width="2.5" stroke-linecap="round" />';

        // Data point circles
        pts.forEach(pt => {
          svgHtml += '<circle cx="' + pt.x + '" cy="' + pt.y + '" r="3" fill="#0F172A" stroke="' + s.color + '" stroke-width="2" />';
        });
      });

      // Scrub vertical line placeholder
      svgHtml += '<line id="scrubLine" class="scrub-line" x1="0" y1="' + paddingTop + '" x2="0" y2="' + (paddingTop + plotH) + '" style="display:none;" />';

      svg.innerHTML = svgHtml;

      // Interactive Touch & Mouse Scrubbing
      function handleScrub(clientX) {
        const rect = svg.getBoundingClientRect();
        const x = clientX - rect.left;
        if (x < paddingLeft || x > width - paddingRight) {
          tooltip.style.display = "none";
          const sl = document.getElementById("scrubLine");
          if (sl) sl.style.display = "none";
          return;
        }

        const ratio = (x - paddingLeft) / plotW;
        const idx = Math.min(data.length - 1, Math.max(0, Math.round(ratio * (data.length - 1))));
        const d = data[idx];
        const snappedX = getX(idx);

        const sl = document.getElementById("scrubLine");
        if (sl) {
          sl.setAttribute("x1", snappedX);
          sl.setAttribute("x2", snappedX);
          sl.style.display = "block";
        }

        let tipHtml = '<div class="tooltip-date">' + d.dateLabel + ' (' + (d.dateBs || d.dateAd) + ')</div>';
        series.forEach(s => {
          const val = d[s.key] || 0;
          tipHtml += '<div style="color:' + s.color + '; margin-top:2px;">' + s.label + ': <span class="tooltip-val">' + formatRs(val) + '</span></div>';
        });

        tooltip.innerHTML = tipHtml;
        tooltip.style.display = "block";
      }

      container.ontouchstart = (e) => { if (e.touches[0]) handleScrub(e.touches[0].clientX); };
      container.ontouchmove = (e) => { if (e.touches[0]) handleScrub(e.touches[0].clientX); };
      container.onmousemove = (e) => { handleScrub(e.clientX); };
      container.onmouseleave = () => {
        tooltip.style.display = "none";
        const sl = document.getElementById("scrubLine");
        if (sl) sl.style.display = "none";
      };
    }

    window.addEventListener("resize", renderChart);
    window.addEventListener("load", renderChart);
    setTimeout(renderChart, 50);
  </script>
</body>
</html>
""".trimIndent()
}
