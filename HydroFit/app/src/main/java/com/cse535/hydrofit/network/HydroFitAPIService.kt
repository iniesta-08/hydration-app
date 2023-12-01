package com.cse535.hydrofit.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * PLACEHOLDER API SERVICE CLASS
 */
interface HydroFitAPIService {

    @POST("hydrofit/fitness")
    suspend fun getFitness(
        @Body body: FitnessRequest
    ): Float

    @POST("hydrofit/hydration")
    suspend fun getHydration(
        @Body body: HydrationRequest
    ): Float

    @GET("all.json")
    suspend fun getAllHeroes()
}