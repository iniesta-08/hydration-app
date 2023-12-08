package com.cse535.hydrofit.network

data class HydrationRequest(
    val timestamp: String,
    val lastdrinkTime: String,
    val wateramount: Int
)
