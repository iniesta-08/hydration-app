package com.cse535.hydrofit.network

data class FitnessRequest(
    val stepCount: Int,
    val heartRate: Int,
    val exerciseDuration: Int
)
