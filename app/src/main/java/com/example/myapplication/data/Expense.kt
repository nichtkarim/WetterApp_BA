package com.example.myapplication.data

import java.time.LocalDate
import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: ExpenseCategory,
    val date: LocalDate = LocalDate.now(),
    val note: String = ""
)

enum class ExpenseCategory(val displayName: String, val emoji: String) {
    FOOD("Essen", "🍽️"),
    TRANSPORT("Transport", "🚗"),
    ENTERTAINMENT("Unterhaltung", "🎬"),
    SHOPPING("Einkaufen", "🛍️"),
    HEALTH("Gesundheit", "🏥"),
    BILLS("Rechnungen", "📄"),
    OTHER("Sonstiges", "📦")
}
