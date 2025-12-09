package com.example.gozizigo.network

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleDirectionsService {

    @GET("directions/json")
    suspend fun getWalkingRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "walking",
        @Query("key") apiKey: String
    ): DirectionsResponse
}
