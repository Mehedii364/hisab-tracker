package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionEntity
import com.example.ui.HisabViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: HisabViewModel
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("ALL") } // "ALL", "INCOME", "EXPENSE"

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = remember(allTransactions, searchQuery, selectedFilterTab) {
        allTransactions.filter { tx ->
            val matchesTab = when (selectedFilterTab) {
                "INCOME" -> tx.type == "INCOME"
                "EXPENSE" -> tx.type == "EXPENSE"
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.description.contains(searchQuery, ignoreCase = true) ||
                    tx.date.contains(searchQuery)
            matchesTab && matchesSearch
        }
    }

    Scaffold(
        containerColor = NeonBlackBg,
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.Bold, color = NeonTextPrimary) },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_transaction_top_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = NeonTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBlackBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_transaction_fab"),
                containerColor = NeonWhiteSolid,
                contentColor = NeonBlackBg
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transactions_input"),
                placeholder = { Text("Search transactions...", color = NeonTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonTextPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = NeonTextPrimary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NeonTextPrimary,
                    unfocusedTextColor = NeonTextPrimary,
                    focusedBorderColor = NeonTextPrimary,
                    unfocusedBorderColor = NeonBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedFilterTab == "ALL",
                    onClick = { selectedFilterTab = "ALL" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = NeonWhiteSolid,
                        activeContentColor = NeonBlackBg,
                        inactiveContainerColor = NeonSurfaceCard,
                        inactiveContentColor = NeonTextPrimary
                    )
                ) {
                    Text("All (${allTransactions.size})")
                }
                SegmentedButton(
                    selected = selectedFilterTab == "EXPENSE",
                    onClick = { selectedFilterTab = "EXPENSE" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = NeonWhiteSolid,
                        activeContentColor = NeonBlackBg,
                        inactiveContainerColor = NeonSurfaceCard,
                        inactiveContentColor = NeonTextPrimary
                    )
                ) {
                    Text("Expense")
                }
                SegmentedButton(
                    selected = selectedFilterTab == "INCOME",
                    onClick = { selectedFilterTab = "INCOME" },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = NeonWhiteSolid,
                        activeContentColor = NeonBlackBg,
                        inactiveContainerColor = NeonSurfaceCard,
                        inactiveContentColor = NeonTextPrimary
                    )
                ) {
                    Text("Income")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = NeonTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            onEdit = { editingTransaction = tx },
                            onDelete = { viewModel.requestDeleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditTransactionDialog(
            transaction = null,
            categories = categories.map { it.name },
            onDismiss = { showAddDialog = false },
            onSave = { type, amount, category, description, date ->
                viewModel.addTransaction(type, amount, category, description, date)
                showAddDialog = false
            }
        )
    }

    if (editingTransaction != null) {
        AddEditTransactionDialog(
            transaction = editingTransaction,
            categories = categories.map { it.name },
            onDismiss = { editingTransaction = null },
            onSave = { type, amount, category, description, date ->
                val updated = editingTransaction!!.copy(
                    type = type,
                    amount = amount,
                    category = category,
                    description = description,
                    date = date
                )
                viewModel.updateTransaction(updated)
                editingTransaction = null
            }
        )
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.type == "INCOME") Emerald500.copy(alpha = 0.15f)
                        else Red500.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "INCOME") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (transaction.type == "INCOME") Emerald500 else Red500,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonTextMuted
                    )
                }
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonTextMuted.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == "INCOME") "+" else "-"} ৳${String.format("%.2f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.type == "INCOME") Emerald500 else Red500
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonTextPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Red500, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditTransactionDialog(
    transaction: TransactionEntity?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, category: String, description: String, date: String) -> Unit
) {
    var type by remember { mutableStateOf(transaction?.type ?: "EXPENSE") }
    var amountText by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(transaction?.category ?: if (categories.isNotEmpty()) categories[0] else "Food & Dining") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var dateText by remember {
        mutableStateOf(
            transaction?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (transaction == null) "Add Transaction" else "Edit Transaction", color = NeonTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type Selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("Expense", color = NeonTextPrimary) },
                        leadingIcon = { Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = Red500) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceHover)
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("Income", color = NeonTextPrimary) },
                        leadingIcon = { Icon(Icons.Default.AddCircle, contentDescription = null, tint = Emerald500) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceHover)
                    )
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (৳)", color = NeonTextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                )

                // Category Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category", color = NeonTextMuted) },
                        trailingIcon = { IconButton(onClick = { categoryDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null, tint = NeonTextPrimary) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = NeonTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                )

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)", color = NeonTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onSave(type, amount, selectedCategory, description, dateText)
                    }
                },
                modifier = Modifier.testTag("save_transaction_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonWhiteSolid, contentColor = NeonBlackBg)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeonTextPrimary)
            }
        },
        containerColor = NeonSurfaceCard
    )
}
