package com.example.nezhadashboard.data

import retrofit2.http.GET
import retrofit2.http.Url

interface NezhaApiService {
    @GET
    suspend fun getServerDetails(@Url fullUrl: String): NezhaResponse
}
