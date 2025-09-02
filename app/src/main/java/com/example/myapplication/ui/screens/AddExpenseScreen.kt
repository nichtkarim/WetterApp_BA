package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ExpenseCategory
import com.example.myapplication.viewmodel.BudgetViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(viewModel: BudgetViewModel) {
    val amount by viewModel.addExpenseAmount.collectAsState()
    val category by viewModel.addExpenseCategory.collectAsState()
    val note by viewModel.addExpenseNote.collectAsState()
    val date by viewModel.addExpenseDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ausgabe hinzufügen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::updateAddExpenseAmount,
                    label = { Text("Betrag") },
                    suffix = { Text("€") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${category.emoji} ${category.displayName}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Kategorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        ExpenseCategory.values().forEach { categoryOption ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = categoryOption.emoji,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(text = categoryOption.displayName)
                                    }
                                },
                                onClick = {
                                    viewModel.updateAddExpenseCategory(categoryOption)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Date picker
                OutlinedTextField(
                    value = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    onValueChange = { },
                    label = { Text("Datum") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Datum auswählen")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = viewModel::updateAddExpenseNote,
                    label = { Text("Notiz (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Add button
                Button(
                    onClick = {
                        viewModel.addExpense()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
                ) {
                    Text("Ausgabe hinzufügen")
                }
            }
        }

        // Recent categories card for quick selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Schnellauswahl Kategorien",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Erste Zeile mit 2 Kategorien
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ExpenseCategory.FOOD,
                        ExpenseCategory.TRANSPORT
                    ).forEach { quickCategory ->
                        FilterChip(
                            onClick = { viewModel.updateAddExpenseCategory(quickCategory) },
                            label = {
                                Text(
                                    text = "${quickCategory.emoji} ${quickCategory.displayName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            selected = category == quickCategory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Zweite Zeile mit 2 Kategorien
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ExpenseCategory.ENTERTAINMENT,
                        ExpenseCategory.SHOPPING
                    ).forEach { quickCategory ->
                        FilterChip(
                            onClick = { viewModel.updateAddExpenseCategory(quickCategory) },
                            label = {
                                Text(
                                    text = "${quickCategory.emoji} ${quickCategory.displayName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            selected = category == quickCategory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = java.time.ZoneOffset.UTC.let {
                date.atStartOfDay().toInstant(it).toEpochMilli()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateAddExpenseDate(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
