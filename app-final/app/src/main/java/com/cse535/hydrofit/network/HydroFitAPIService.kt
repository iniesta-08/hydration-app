package com.cse535.hydrofit.network

import com.cse535.hydrofit.User
import com.cse535.hydrofit.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * PLACEHOLDER API SERVICE CLASS
 */
interface HydroFitAPIService {

    @POST("/data/{username}/fitness")
    suspend fun getFitness(
        @Path("username") userName: String?,
        @Body body: FitnessRequest
    ): FitnessResponse

    @POST("/data/{username}/hydration")
    suspend fun getHydration(
        @Path("username") userName: String?,
        @Body body: HydrationRequest
    ): HydrationResponse

    @POST("/users")
    suspend fun createUser(@Body user: User?): UserResponse

    @POST("/data/{username}/deviceToken")
    suspend fun saveDeviceToken(
        @Path("username") userName: String?,
        @Body body: SaveDeviceTokenRequest
    )
}