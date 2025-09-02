package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class BudgetRepository(private val context: Context) {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val BUDGET_LIMIT_KEY = doublePreferencesKey("budget_limit")
        private val CURRENCY_KEY = stringPreferencesKey("currency")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            isDarkMode = preferences[DARK_MODE_KEY] ?: false,
            budget = Budget(
                monthlyLimit = preferences[BUDGET_LIMIT_KEY] ?: 1000.0,
                currency = preferences[CURRENCY_KEY] ?: "€"
            )
        )
    }

    suspend fun updateDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    suspend fun updateBudget(monthlyLimit: Double, currency: String) {
        context.dataStore.edit { preferences ->
            preferences[BUDGET_LIMIT_KEY] = monthlyLimit
            preferences[CURRENCY_KEY] = currency
        }
    }

    fun addExpense(expense: Expense) {
        _expenses.value = _expenses.value + expense
    }

    fun deleteExpense(expenseId: String) {
        _expenses.value = _expenses.value.filter { it.id != expenseId }
    }

    fun getExpensesForCurrentMonth(): List<Expense> {
        val currentMonth = LocalDate.now().monthValue
        val currentYear = LocalDate.now().year
        return _expenses.value.filter {
            it.date.monthValue == currentMonth && it.date.year == currentYear
        }
    }

    fun getTotalExpensesForCurrentMonth(): Double {
        return getExpensesForCurrentMonth().sumOf { it.amount }
    }

    fun getExpensesByCategory(category: ExpenseCategory): List<Expense> {
        return _expenses.value.filter { it.category == category }
    }

    fun getExpensesBetweenDates(startDate: LocalDate, endDate: LocalDate): List<Expense> {
        return _expenses.value.filter { expense ->
            (expense.date.isAfter(startDate) || expense.date.isEqual(startDate)) &&
            (expense.date.isBefore(endDate) || expense.date.isEqual(endDate))
        }
    }
}
