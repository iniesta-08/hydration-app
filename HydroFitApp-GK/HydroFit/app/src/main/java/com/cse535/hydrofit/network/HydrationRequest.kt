package com.cse535.hydrofit.network

data class HydrationRequest(
    val timeSinceLastDrink: Int,
    val lastDrinkAmount: Int,
    val fitnessLevels: Int
)
