package com.example.myapplication.data

data class Budget(
    val monthlyLimit: Double = 1000.0,
    val currency: String = "€"
)

data class AppSettings(
    val isDarkMode: Boolean = false,
    val budget: Budget = Budget()
)
