package com.example.livekitdemo

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class LiveKitAPIService {
    private val api: LiveKitAPI

    init {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("LiveKitAPIService", "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create OkHttpClient with timeout and logging
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(UrlFactory.LIVEKIT_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(LiveKitAPI::class.java)
    }

    suspend fun joinRoom(roomName: String, participantName: String): ConnectionDetailsResponse {
        Log.d("LiveKitAPIService", "Making API call to join room: $roomName, participant: $participantName")
        
        return try {
            val response = api.joinRoom(
                sandboxId = UrlFactory.DEFAULT_SANDBOX_ID,
                participantName = participantName,
                roomName = roomName
            )
            Log.d("LiveKitAPIService", "API call successful: ${response.serverUrl}")
            response
        } catch (e: Exception) {
            Log.e("LiveKitAPIService", "API call failed: ${e.message}")
            throw e
        }
    }
}