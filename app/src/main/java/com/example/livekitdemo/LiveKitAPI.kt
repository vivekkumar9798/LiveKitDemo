package com.example.livekitdemo

import retrofit2.http.*

interface LiveKitAPI {
    @GET(UrlFactory.CONNECTION_DETAILS_ENDPOINT)
    suspend fun joinRoom(
        @Header(UrlFactory.SANDBOX_ID_HEADER) sandboxId: String = UrlFactory.DEFAULT_SANDBOX_ID,
        @Query(UrlFactory.PARTICIPANT_NAME_PARAM) participantName: String,
        @Query(UrlFactory.ROOM_NAME_PARAM) roomName: String
    ): ConnectionDetailsResponse
}

data class ConnectionDetailsResponse(
    val serverUrl: String,
    val roomName: String,
    val participantName: String,
    val participantToken: String
)