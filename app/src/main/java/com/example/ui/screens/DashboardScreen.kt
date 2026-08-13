package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.ui.HisabViewModel
import com.example.ui.LocaleStrings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HisabViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit
) {
    val summary by viewModel.financialSummary.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsState()
    val isBangla = userProfile.appLanguage == "BN"
    val currency = userProfile.currencySymbol

    var quickCommandText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = NeonBlackBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonSurfaceHover,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = NeonTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HISAB TRACKER",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonTextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "NEON FINTECH CANVAS 2026",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonTextMuted,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePrivacyMode() }) {
                        Icon(
                            imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Privacy Mode",
                            tint = NeonTextPrimary
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = NeonSurfaceHover,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("ai_assistant_top_btn")
                            .clickable { onNavigateToChat() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Assistant",
                                tint = NeonTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✦ AI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonTextPrimary
                            )
                        }
                    }
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
            // 🇧🇩 1. NEON HERO BLACK & WHITE BALANCE CARD
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = NeonSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorderHover)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = NeonSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                            ) {
                                Text(
                                    text = "NET MONTHLY BALANCE",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = NeonSurfaceHover,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = NeonTextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${summary.monthlySavingsRate.toInt()}% Saved",
                                            fontSize = 11.sp,
                                            color = NeonTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Balance Big White Neon Display
                        val displayBalance = if (isPrivacyMode) "৳ ••••••" else "৳ ${String.format("%,.2f", summary.monthlyBalance)}"
                        Text(
                            text = displayBalance,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonTextPrimary,
                            letterSpacing = (-1).sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Monochrome Income & Expense Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Income Pill
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = NeonSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Emerald500.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Income", fontSize = 11.sp, color = NeonTextMuted)
                                        val displayIncome = if (isPrivacyMode) "৳ ••••" else "৳ ${String.format("%,d", summary.monthlyIncome.toInt())}"
                                        Text(
                                            displayIncome,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NeonTextPrimary
                                        )
                                    }
                                }
                            }

                            // Expense Pill
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = NeonSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Red500.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Red500, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Expense", fontSize = 11.sp, color = NeonTextMuted)
                                        val displayExpense = if (isPrivacyMode) "৳ ••••" else "৳ ${String.format("%,d", summary.monthlyExpense.toInt())}"
                                        Text(
                                            displayExpense,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NeonTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🤖 2. DIRECT QUICK VOICE & COMMAND AI INPUT BOX
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = NeonSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = quickCommandText,
                            onValueChange = { quickCommandText = it },
                            placeholder = { Text(if (isBangla) "বলুন বা লিখুন: 'আজ বাজারে ৮৫০ টাকা খরচ'..." else "Type or speak: 'Spent 850 in market'...", fontSize = 12.sp, color = NeonTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = NeonTextPrimary,
                                unfocusedTextColor = NeonTextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (quickCommandText.isNotBlank()) {
                                        viewModel.sendChatMessage(quickCommandText)
                                        quickCommandText = ""
                                        onNavigateToChat()
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (quickCommandText.isNotBlank()) {
                                    viewModel.sendChatMessage(quickCommandText)
                                    quickCommandText = ""
                                    onNavigateToChat()
                                } else {
                                    onNavigateToChat()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = NeonTextPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ⚡ 3. QUICK AI CHIP SHORTCUTS
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK AI COMMANDS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonTextMuted,
                            letterSpacing = 1.sp
                        )
                        TextButton(onClick = onNavigateToChat) {
                            Text("Open Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonTextPrimary)
                        }
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.sendChatMessage("আজ রেস্টুরেন্টে ৪৫০ টাকা খরচ হয়েছে")
                                    onNavigateToChat()
                                },
                                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonTextPrimary) },
                                label = { Text("৳৪৫০ রেস্টুরেন্ট খরচ", fontSize = 12.sp, color = NeonTextPrimary) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceCard)
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.sendChatMessage("আজ বিকাশে ২০০০ টাকা ক্যাশ ইন পেয়েছি")
                                    onNavigateToChat()
                                },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonTextPrimary) },
                                label = { Text("আজকের আয় ৳২০০০", fontSize = 12.sp, color = NeonTextPrimary) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceCard)
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.sendChatMessage("বিকাশ থেকে ব্যাংকে ৫০০০ টাকা পাঠালাম")
                                    onNavigateToChat()
                                },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonTextPrimary) },
                                label = { Text("bKash → Bank ৳৫০০০", fontSize = 12.sp, color = NeonTextPrimary) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceCard)
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.sendChatMessage("আমার এই মাসের আর্থিক রিপোর্ট ও বিশ্লেষণ দাও")
                                    onNavigateToChat()
                                },
                                leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonTextPrimary) },
                                label = { Text("AI Financial Report", fontSize = 12.sp, color = NeonTextPrimary) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceCard)
                            )
                        }
                    }
                }
            }

            // 🎯 4. BUDGET PROGRESS RING & HEALTH DIAL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val progress = (summary.budgetSpentPercent.toFloat() / 100f).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(76.dp)) {
                                drawArc(
                                    color = NeonSurfaceHover,
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = NeonTextPrimary,
                                    startAngle = 135f,
                                    sweepAngle = 270f * progress,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${summary.budgetSpentPercent.toInt()}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonTextPrimary
                                )
                                Text("BUDGET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = NeonSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                            ) {
                                Text(
                                    text = if (summary.budgetSpentPercent > 90) "Limit Exceeded" else if (summary.budgetSpentPercent > 70) "High Spending" else "Healthy Budget",
                                    color = NeonTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Today's Expense: ৳${summary.todayExpense.toInt()}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (summary.highestCategoryName != "None") "Top category: ${summary.highestCategoryName}" else "Add expenses to track limits",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonTextMuted
                            )
                        }

                        IconButton(onClick = onNavigateToBudgets) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Budget details", tint = NeonTextPrimary)
                        }
                    }
                }
            }

            // 📋 5. RECENT TRANSACTIONS LIST
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT TRANSACTIONS",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("See All (${transactions.size})", fontWeight = FontWeight.Bold, color = NeonTextPrimary)
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = NeonTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions recorded yet",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = NeonTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask AI Assistant 'আজ ৫০০ টাকা খাবার খরচ' to start tracking instantly!",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonTextMuted,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                items(transactions.take(6)) { tx ->
                    NeonTransactionRow(transaction = tx, isPrivacyMode = isPrivacyMode)
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun NeonTransactionRow(transaction: TransactionEntity, isPrivacyMode: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isIncome = transaction.type == "INCOME"

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NeonSurfaceHover)
                    .border(1.dp, NeonBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isIncome) "↓" else "↑",
                    color = if (isIncome) Emerald500 else Red500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonTextPrimary
                )
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonTextMuted
                    )
                }
                Text(
                    text = "${transaction.date} • ${transaction.accountName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonTextMuted
                )
            }
            val displayAmount = if (isPrivacyMode) "৳ •••" else "${if (isIncome) "+" else "-"} ৳${String.format("%,d", transaction.amount.toInt())}"
            Text(
                text = displayAmount,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
                color = if (isIncome) Emerald500 else Red500
            )
        }
    }
}
