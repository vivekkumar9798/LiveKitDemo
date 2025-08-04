package com.example.livekitdemo

object UrlFactory {
    // Base URLs
    const val LIVEKIT_BASE_URL = "https://cloud-api.livekit.io/api/sandbox/"
    
    // API Endpoints
    const val CONNECTION_DETAILS_ENDPOINT = "connection-details/"
    
    // Sandbox Configuration
    const val DEFAULT_SANDBOX_ID = "viveklivekit-1rg10o"
    
    // API Headers
    const val SANDBOX_ID_HEADER = "X-Sandbox-ID"
    
    // Query Parameters
    const val PARTICIPANT_NAME_PARAM = "participantName"
    const val ROOM_NAME_PARAM = "roomName"
    
    // Test URLs
    const val SANDBOX_TEST_URL = "https://cloud-api.livekit.io/api/sandbox/"
    
    // Default Values
    const val DEFAULT_ROOM_NAME = "test"
    const val DEFAULT_PARTICIPANT_NAME = "test"
} 