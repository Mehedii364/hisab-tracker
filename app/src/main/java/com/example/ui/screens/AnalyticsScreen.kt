package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HisabViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: HisabViewModel,
    onNavigateToChat: () -> Unit
) {
    val summary by viewModel.financialSummary.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsState()

    Scaffold(
        containerColor = NeonBlackBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ANALYTICS & REPORTS",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonTextPrimary,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBlackBg)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expense Breakdown Donut Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expense Breakdown",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NeonSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                            ) {
                                Text(
                                    text = "THIS MONTH",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonTextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Donut Canvas Chart
                        Box(
                            modifier = Modifier.size(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(150.dp)) {
                                val strokeWidth = 22.dp.toPx()
                                val slices: List<Triple<Float, Color, String>> = listOf(
                                    Triple(0.45f, ChartWhite, "Food"),
                                    Triple(0.25f, ChartGrayLight, "Utilities"),
                                    Triple(0.18f, ChartAccent, "Shopping"),
                                    Triple(0.12f, ChartGrayDark, "Others")
                                )

                                var currentAngle = -90f
                                slices.forEach { (percent, color, _) ->
                                    val sweep = percent * 360f
                                    drawArc(
                                        color = color,
                                        startAngle = currentAngle,
                                        sweepAngle = sweep - 4f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    currentAngle += sweep
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val displayExpense = if (isPrivacyMode) "৳ •••" else "৳${String.format("%,d", summary.monthlyExpense.toInt())}"
                                Text(
                                    text = displayExpense,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonTextPrimary
                                )
                                Text("TOTAL SPENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Chart Legend Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendItem(color = ChartWhite, label = "Food (45%)")
                            LegendItem(color = ChartGrayLight, label = "Bills (25%)")
                            LegendItem(color = ChartAccent, label = "Shop (18%)")
                            LegendItem(color = ChartGrayDark, label = "Other (12%)")
                        }
                    }
                }
            }

            // Savings & Health Rate Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = NeonSurfaceHover,
                                modifier = Modifier.size(40.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = NeonTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Savings & Financial Health Rate",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonTextPrimary
                                )
                                Text(
                                    text = "TARGET: 20%+ MONTHLY SAVINGS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${summary.monthlySavingsRate.toInt()}%",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald500
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NeonSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                            ) {
                                Text(
                                    text = if (summary.monthlySavingsRate >= 20) "HEALTHY RATE" else "REBALANCING NEEDED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald500,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (summary.monthlySavingsRate / 100f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Emerald500,
                            trackColor = NeonSurfaceHover
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (summary.monthlySavingsRate >= 20) "Great work! You are maintaining a healthy savings buffer over 20%."
                            else "Consider optimizing recurring food and utilities expenses to increase savings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonTextMuted
                        )
                    }
                }
            }

            // Month Comparison Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Month-over-Month Comparison",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonTextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("This Month", style = MaterialTheme.typography.labelSmall, color = NeonTextMuted)
                                val displayVal = if (isPrivacyMode) "৳ •••" else "৳ ${String.format("%,d", summary.monthlyExpense.toInt())}"
                                Text(displayVal, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = NeonTextPrimary)
                            }
                            Column {
                                Text("Previous Month", style = MaterialTheme.typography.labelSmall, color = NeonTextMuted)
                                val displayPrev = if (isPrivacyMode) "৳ •••" else "৳ ${String.format("%,d", summary.prevMonthExpense.toInt())}"
                                Text(displayPrev, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = NeonTextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonSurfaceHover,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (summary.expenseChangePercent > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (summary.expenseChangePercent > 0) Red500 else Emerald500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${if (summary.expenseChangePercent > 0) "+" else ""}${String.format("%.1f", summary.expenseChangePercent)}% spending vs last month",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (summary.expenseChangePercent > 0) Red500 else Emerald500
                                )
                            }
                        }
                    }
                }
            }

            // Highest Spending Category Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Top Spending Category",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonTextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Red500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = Red500, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = summary.highestCategoryName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonTextPrimary
                                )
                                val displayAmt = if (isPrivacyMode) "৳ •••" else "৳ ${String.format("%,d", summary.highestCategoryAmount.toInt())} spent"
                                Text(
                                    text = displayAmt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Red500,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // AI Report Action
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonTextPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Full Report & Optimization",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ask AI Assistant to analyze full financial history, detect recurring subscriptions, and generate budget tips.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonTextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToChat,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonWhiteSolid, contentColor = NeonBlackBg)
                        ) {
                            Text("Ask AI for Financial Insights", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTextMuted)
    }
}
