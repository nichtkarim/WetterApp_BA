package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppSettings
import com.example.myapplication.data.Budget
import com.example.myapplication.data.BudgetRepository
import com.example.myapplication.data.Expense
import com.example.myapplication.data.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    val expenses = repository.expenses
    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    val dashboardState = combine(
        expenses,
        settings
    ) { expenseList, appSettings ->
        val monthlyExpenses = repository.getExpensesForCurrentMonth()
        val totalSpent = monthlyExpenses.sumOf { it.amount }
        val budget = appSettings.budget.monthlyLimit
        val remainingBudget = budget - totalSpent
        val spentPercentage = if (budget > 0) (totalSpent / budget).coerceAtMost(1.0) else 0.0

        DashboardState(
            totalBudget = budget,
            totalSpent = totalSpent,
            remainingBudget = remainingBudget,
            spentPercentage = spentPercentage,
            monthlyExpenses = monthlyExpenses,
            currency = appSettings.budget.currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    // Add Expense State
    private val _addExpenseAmount = MutableStateFlow("")
    val addExpenseAmount = _addExpenseAmount.asStateFlow()

    private val _addExpenseCategory = MutableStateFlow(ExpenseCategory.OTHER)
    val addExpenseCategory = _addExpenseCategory.asStateFlow()

    private val _addExpenseNote = MutableStateFlow("")
    val addExpenseNote = _addExpenseNote.asStateFlow()

    private val _addExpenseDate = MutableStateFlow(LocalDate.now())
    val addExpenseDate = _addExpenseDate.asStateFlow()

    // Settings State
    private val _settingsBudget = MutableStateFlow("")
    val settingsBudget = _settingsBudget.asStateFlow()

    private val _settingsCurrency = MutableStateFlow("€")
    val settingsCurrency = _settingsCurrency.asStateFlow()

    // Filter State
    private val _selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    fun addExpense() {
        val amount = _addExpenseAmount.value.toDoubleOrNull()
        if (amount != null && amount > 0) {
            val expense = Expense(
                amount = amount,
                category = _addExpenseCategory.value,
                date = _addExpenseDate.value,
                note = _addExpenseNote.value
            )
            repository.addExpense(expense)
            clearAddExpenseForm()
        }
    }

    fun updateAddExpenseAmount(amount: String) {
        _addExpenseAmount.value = amount
    }

    fun updateAddExpenseCategory(category: ExpenseCategory) {
        _addExpenseCategory.value = category
    }

    fun updateAddExpenseNote(note: String) {
        _addExpenseNote.value = note
    }

    fun updateAddExpenseDate(date: LocalDate) {
        _addExpenseDate.value = date
    }

    private fun clearAddExpenseForm() {
        _addExpenseAmount.value = ""
        _addExpenseCategory.value = ExpenseCategory.OTHER
        _addExpenseNote.value = ""
        _addExpenseDate.value = LocalDate.now()
    }

    fun deleteExpense(expenseId: String) {
        repository.deleteExpense(expenseId)
    }

    fun updateDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            repository.updateDarkMode(isDarkMode)
        }
    }

    fun updateBudget(monthlyLimit: Double, currency: String) {
        viewModelScope.launch {
            repository.updateBudget(monthlyLimit, currency)
        }
    }

    fun updateSettingsBudget(budget: String) {
        _settingsBudget.value = budget
    }

    fun updateSettingsCurrency(currency: String) {
        _settingsCurrency.value = currency
    }

    fun saveSettings() {
        val budget = _settingsBudget.value.toDoubleOrNull()
        if (budget != null && budget > 0) {
            updateBudget(budget, _settingsCurrency.value)
        }
    }

    fun loadCurrentSettings() {
        val currentSettings = settings.value
        _settingsBudget.value = currentSettings.budget.monthlyLimit.toString()
        _settingsCurrency.value = currentSettings.budget.currency
    }

    fun filterByCategory(category: ExpenseCategory?) {
        _selectedCategory.value = category
    }

    fun getFilteredExpenses(): List<Expense> {
        val allExpenses = expenses.value
        return _selectedCategory.value?.let { category ->
            allExpenses.filter { it.category == category }
        } ?: allExpenses
    }
}

data class BudgetUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DashboardState(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val spentPercentage: Double = 0.0,
    val monthlyExpenses: List<Expense> = emptyList(),
    val currency: String = "€"
)

class BudgetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            return BudgetViewModel(BudgetRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
