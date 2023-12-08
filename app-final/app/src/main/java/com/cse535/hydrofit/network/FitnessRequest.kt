package com.cse535.hydrofit.network

data class FitnessRequest(
    val timestamp: String,
    val stepcount: Int,
    val heartrate: Int,
    val exercise: Int
)
