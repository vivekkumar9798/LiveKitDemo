package com.example.livekitdemo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.livekitdemo.databinding.ActivityCallBinding
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.launch

class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallBinding
    private lateinit var apiService: LiveKitAPIService
    private var room: Room? = null
    private var isVideo = true
    private var isMuted = false
    private var isVideoEnabled = true
    private var callStartTime = 0L
    private var callDurationHandler = Handler(Looper.getMainLooper())
    private var callDurationRunnable: Runnable? = null
    private var isDemoMode = false
    private var remoteParticipant: RemoteParticipant? = null
    private var isLocalVideoAttached = false
    private var isLocalVideoInitialized = false
    private var isRemoteVideoInitialized = false
    private var isCallEnding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on during call
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupFromIntent()
        setupUI()
        initializeVideoViews()
        initializeCall()
    }

    private fun setupFromIntent() {
        isVideo = intent.getBooleanExtra("IS_VIDEO", true)
        val isInitiator = intent.getBooleanExtra("IS_INITIATOR", false)

        apiService = LiveKitAPIService()

        if (!isVideo) {
            binding.localVideoView.visibility = View.GONE
            binding.remoteVideoView.visibility = View.GONE
            binding.audioCallLayout.visibility = View.VISIBLE
        }
    }

    private fun setupUI() {
        binding.apply {
            btnEndCall.setOnClickListener {
                showEndCallConfirmation()
            }
            btnMute.setOnClickListener {
                toggleMute()
            }
            btnVideo.setOnClickListener {
                toggleVideo()
            }
            btnSwitchCamera.setOnClickListener {
                switchCamera()
            }
            btnSpeaker.setOnClickListener {
                toggleSpeaker()
            }

            // Hide controls after 5 seconds
            hideControlsAfterDelay()

            // Show controls when tapping screen
            callContainer.setOnClickListener {
                showControls()
                hideControlsAfterDelay()
            }
        }

        updateCallTypeUI()
    }

    private fun initializeVideoViews() {
        try {
            // Initialize local video view
            binding.localVideoView.init(null, null)
            binding.localVideoView.setEnableHardwareScaler(true)
            binding.localVideoView.setMirror(true)
            isLocalVideoInitialized = true

            // Initialize remote video view
            binding.remoteVideoView.init(null, null)
            binding.remoteVideoView.setEnableHardwareScaler(true)
            binding.remoteVideoView.setMirror(false)
            isRemoteVideoInitialized = true

            Log.d("CallActivity", "Video views initialized successfully")
        } catch (e: Exception) {
            Log.e("CallActivity", "Failed to initialize video views: ${e.message}")
        }
    }

    private fun initializeCall() {
        lifecycleScope.launch {
            try {
                val isInitiator = intent.getBooleanExtra("IS_INITIATOR", false)

                if (isInitiator) {
                    val callerId = intent.getStringExtra("CALLER_ID") ?: ""
                    val calleeId = intent.getStringExtra("CALLEE_ID") ?: ""
                    initiateCall(callerId, calleeId)
                } else {
                    val roomName = intent.getStringExtra("ROOM_NAME") ?: ""
                    val participantName = intent.getStringExtra("PARTICIPANT_NAME") ?: ""
                    joinRoom(roomName, participantName)
                }
            } catch (e: Exception) {
                Log.e("CallActivity", "Failed to initialize call: ${e.message}")
                showError("Failed to connect: ${e.message}")

                // Activate demo mode immediately
                activateDemoMode()
            }
        }
    }

    private suspend fun initiateCall(callerId: String, calleeId: String) {
        try {
            Log.d("CallActivity", "Attempting to get connection details from API...")
            val connectionDetails = apiService.joinRoom(UrlFactory.DEFAULT_ROOM_NAME, callerId)

            Log.d("CallActivity", "API call successful, connecting to room...")
            connectToRoom(connectionDetails.participantToken, connectionDetails.serverUrl)
            binding.tvParticipantName.text = calleeId
            binding.tvCallStatus.text = "Calling..."

        } catch (e: Exception) {
            Log.e("CallActivity", "API call failed: ${e.message}")

            // Show error and activate demo mode
            showError("API unavailable, using demo mode")
            activateDemoMode()
            binding.tvParticipantName.text = calleeId
            binding.tvCallStatus.text = "Calling (Demo Mode)..."
        }
    }

    private suspend fun joinRoom(roomName: String, participantName: String) {
        try {
            Log.d("CallActivity", "Attempting to get connection details from API...")
            val connectionDetails = apiService.joinRoom(roomName, participantName)

            Log.d("CallActivity", "API call successful, connecting to room...")
            connectToRoom(connectionDetails.participantToken, connectionDetails.serverUrl)
            binding.tvParticipantName.text = roomName
            binding.tvCallStatus.text = "Joining..."

        } catch (e: Exception) {
            Log.e("CallActivity", "API call failed: ${e.message}")

            // Show error and activate demo mode
            showError("API unavailable, using demo mode")
            activateDemoMode()
            binding.tvParticipantName.text = roomName
            binding.tvCallStatus.text = "Joining (Demo Mode)..."
        }
    }

    private fun activateDemoMode() {
        isDemoMode = true
        Log.d("CallActivity", "Demo mode activated")

        // Simulate successful connection after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvCallStatus.text = "Demo Mode - Connected"
            binding.tvCallStatus.setTextColor(android.graphics.Color.GREEN)
            startCallTimer()

            if (isVideo) {
                binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                binding.remoteVideoView.setBackgroundColor(android.graphics.Color.DKGRAY)
            }

            // Show success animation
            binding.tvCallStatus.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(300)
                .withEndAction {
                    binding.tvCallStatus.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()

            // Simulate remote participant for demo mode
            simulateRemoteParticipant()

            Log.d("CallActivity", "Demo mode connection simulation completed")
        }, 2000)
    }

    private fun simulateRemoteParticipant() {
        // Simulate remote participant behavior in demo mode
        Handler(Looper.getMainLooper()).postDelayed({
            if (isDemoMode) {
                // Simulate participant leaving after 30 seconds in demo mode
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isDemoMode) {
                        binding.tvCallStatus.text = "Demo Mode - Participant Left"
                        binding.tvCallStatus.setTextColor(android.graphics.Color.RED)
                        startCallEndCountdown()
                    }
                }, 30000) // 30 seconds
            }
        }, 5000) // Wait 5 seconds before starting simulation
    }

    private suspend fun connectToRoom(token: String, wsUrl: String) {
        try {
            Log.d("CallActivity", "Starting connection process")
            Log.d("CallActivity", "Token length = ${token.length}")
            Log.d("CallActivity", "WebSocket URL = $wsUrl")

            Log.d("CallActivity", "Creating LiveKit instance...")
            room = LiveKit.create(applicationContext)

            // Setup room event listeners for automatic call ending
            setupRoomEventListeners()
            Log.d("CallActivity", "Room listener setup completed")

            Log.d("CallActivity", "Connecting to: $wsUrl")
            Log.d("CallActivity", "LiveKit instance created successfully")

            Log.d("CallActivity", "Attempting to connect...")
            room?.connect(wsUrl, token)
            Log.d("CallActivity", "Connection request sent to LiveKit server")

            // Add connection status check with dynamic video setup
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("CallActivity", "Connection status check after 2 seconds")
                Log.d("CallActivity", "Room connected = ${room != null}")
                Log.d("CallActivity", "Local participant = ${room?.localParticipant?.identity}")

                // Setup local video if available
                if (isVideo) {
                    setupLocalVideo()
                }

                // Check for remote participants
                room?.remoteParticipants?.values?.forEach { participant ->
                    Log.d("CallActivity", "Found remote participant: ${participant.identity}")
                    handleRemoteParticipantConnected(participant)
                }
            }, 2000)

            // Setup local tracks with error handling
            lifecycleScope.launch {
                try {
                    room?.localParticipant?.setMicrophoneEnabled(true)
                    Log.d("CallActivity", "Microphone enabled successfully")
                } catch (e: Exception) {
                    Log.e("CallActivity", "Failed to enable microphone - ${e.message}")
                }

                if (isVideo) {
                    try {
                        room?.localParticipant?.setCameraEnabled(true)
                        Log.d("CallActivity", "Camera enabled successfully")

                        // Wait a bit for the video track to be created
                        Handler(Looper.getMainLooper()).postDelayed({
                            setupLocalVideo()
                        }, 1000)

                    } catch (e: Exception) {
                        Log.e("CallActivity", "Failed to enable camera - ${e.message}")
                        // Fallback to demo mode for video
                        binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                        Log.d("CallActivity", "Using demo mode for video")
                    }
                }
            }

            startCallTimer()
            binding.tvCallStatus.text = "Connected"

            // Beautiful connection success animation
            binding.tvCallStatus.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .withEndAction {
                    binding.tvCallStatus.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()

            // Start periodic video track checking for dynamic updates
            startVideoTrackMonitoring()

            Log.d("CallActivity", "Room connection completed successfully!")

        } catch (e: Exception) {
            Log.e("CallActivity", "Connection failed - ${e.message}")
            Log.e("CallActivity", "Exception type = ${e.javaClass.simpleName}")
            Log.e("CallActivity", "Full error = $e")

            // Check if it's an authentication error
            val errorMessage = when {
                e.message?.contains("401") == true -> "Token expired or invalid"
                e.message?.contains("Could not fetch region settings") == true -> "Authentication failed"
                else -> "Connection failed: ${e.message}"
            }

            showError(errorMessage)

            // Activate demo mode as fallback
            activateDemoMode()
        }
    }

    private fun handleRemoteParticipantConnected(participant: RemoteParticipant?) {
        logStep("handleRemoteParticipantConnected", "Remote participant connected: ${participant?.identity ?: "Unknown"}")
        remoteParticipant = participant
        runOnUiThread {
            binding.tvParticipantName.text = participant?.identity?.toString() ?: "Remote Participant"
            binding.tvCallStatus.text = "Connected"

            // Setup remote video
            if (isVideo && participant != null) {
                // Try to setup remote video with retry
                setupRemoteVideo(participant)
                logStep("handleRemoteParticipantConnected", "Remote video setup initiated")
            } else if (!isVideo) {
                // Show participant avatar for audio call
                binding.participantAvatar.visibility = View.VISIBLE
                binding.participantInitials.text = (participant?.identity?.toString() ?: "RP").take(2).uppercase()
                logStep("handleRemoteParticipantConnected", "Audio call avatar set")
            }

            Log.d("CallActivity", "Remote participant UI updated")
        }
    }

    private fun setupRoomEventListeners() {
        room?.let { roomInstance ->
            Log.d("CallActivity", "Setting up room event listeners")

            // Setup event handling for track subscriptions
            lifecycleScope.launch {
                roomInstance.events.collect { event ->
                    when (event) {
                        is RoomEvent.TrackSubscribed -> onTrackSubscribed(event)
                        is RoomEvent.TrackUnsubscribed -> onTrackUnsubscribed(event)
                        else -> {}
                    }
                }
            }

            // Monitor remote participants periodically
            startRemoteParticipantMonitoring()

            // Monitor connection state
            startConnectionStateMonitoring()
        }
    }

    private fun startRemoteParticipantMonitoring() {
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                try {
                    room?.let { roomInstance ->
                        val currentParticipants = roomInstance.remoteParticipants.values.toList()

                        // Check if remote participant left
                        if (remoteParticipant != null && currentParticipants.isEmpty()) {
                            Log.d("CallActivity", "Remote participant left - no participants found")
                            handleRemoteParticipantDisconnected(remoteParticipant)
                            return@let
                        }

                        // Check if our remote participant is no longer in the list
                        if (remoteParticipant != null) {
                            val participantStillExists = currentParticipants.any { 
                                it.identity == remoteParticipant?.identity 
                            }
                            if (!participantStillExists) {
                                Log.d("CallActivity", "Remote participant left - participant no longer in list")
                                handleRemoteParticipantDisconnected(remoteParticipant)
                                return@let
                            }
                        }

                        // Check for new participants
                        currentParticipants.firstOrNull()?.let { participant ->
                            if (remoteParticipant?.identity != participant.identity) {
                                Log.d("CallActivity", "New remote participant found: ${participant.identity}")
                                handleRemoteParticipantConnected(participant)
                            }
                        }
                    }

                    // Continue monitoring
                    Handler(Looper.getMainLooper()).postDelayed(this, 2000)
                } catch (e: Exception) {
                    Log.e("CallActivity", "Error in remote participant monitoring: ${e.message}")
                }
            }
        }, 2000)
    }

    private fun startConnectionStateMonitoring() {
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                try {
                    room?.let { roomInstance ->
                        // Check if room is still connected by trying to access local participant
                        val localParticipant = roomInstance.localParticipant
                        if (localParticipant == null) {
                            Log.d("CallActivity", "Local participant is null - connection may be lost")
                            binding.tvCallStatus.text = "Connection Lost"
                            binding.tvCallStatus.setTextColor(android.graphics.Color.RED)
                            startCallEndCountdown()
                            return@let
                        }

                        // Check if we can access remote participants (indicates connection is alive)
                        val remoteParticipants = roomInstance.remoteParticipants
                        Log.d("CallActivity", "Connection check - Local participant: ${localParticipant.identity}, Remote participants: ${remoteParticipants.size}")

                        // Update status based on connection health
                        if (remoteParticipants.isNotEmpty()) {
                            binding.tvCallStatus.text = "Connected"
                            binding.tvCallStatus.setTextColor(android.graphics.Color.GREEN)
                        } else {
                            // No remote participants but local participant exists - still connected
                            binding.tvCallStatus.text = "Waiting for participants..."
                        }
                    }

                    // Continue monitoring
                    Handler(Looper.getMainLooper()).postDelayed(this, 3000)
                } catch (e: Exception) {
                    Log.e("CallActivity", "Error in connection state monitoring: ${e.message}")
                    // If we can't access room properties, connection might be lost
                    binding.tvCallStatus.text = "Connection Error"
                    binding.tvCallStatus.setTextColor(android.graphics.Color.RED)
                    startCallEndCountdown()
                }
            }
        }, 3000)
    }

    private fun handleRemoteParticipantDisconnected(participant: RemoteParticipant?) {
        runOnUiThread {
            binding.tvCallStatus.text = "Participant left"
            binding.tvCallStatus.setTextColor(android.graphics.Color.RED)

            // Show participant left animation
            binding.tvCallStatus.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction {
                    binding.tvCallStatus.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()

            // Clear remote participant reference
            remoteParticipant = null

            // Show option to end call immediately or wait for auto-end
            showParticipantLeftDialog()
        }
    }



    private fun startCallEndCountdown() {
        if (isCallEnding) {
            Log.d("CallActivity", "Call ending countdown already in progress")
            return
        }

        Log.d("CallActivity", "Starting call end countdown")
        isCallEnding = true
        var countdown = 3
        val countdownRunnable = object : Runnable {
            override fun run() {
                if (countdown > 0) {
                    binding.tvCallStatus.text = "Call ending in $countdown..."
                    countdown--
                    Handler(Looper.getMainLooper()).postDelayed(this, 1000)
                } else {
                    Log.d("CallActivity", "Countdown finished, ending call")
                    endCall()
                }
            }
        }
        Handler(Looper.getMainLooper()).post(countdownRunnable)
    }

    private fun toggleMute() {
        isMuted = !isMuted

        if (!isDemoMode) {
            lifecycleScope.launch {
                try {
                    room?.localParticipant?.setMicrophoneEnabled(!isMuted)
                    Log.d("CallActivity", "Microphone toggled successfully")
                } catch (e: Exception) {
                    Log.e("CallActivity", "Failed to toggle microphone: ${e.message}")
                    // Fallback to demo mode
                    runOnUiThread {
                        Toast.makeText(this@CallActivity, "Demo mode - ${if (isMuted) "Muted" else "Unmuted"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Demo mode - ${if (isMuted) "Muted" else "Unmuted"}", Toast.LENGTH_SHORT).show()
        }

        binding.btnMute.apply {
            setImageResource(if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic)
            animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .rotationBy(360f)
                .setDuration(200)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .withEndAction {
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        Log.d("CallActivity", "Mute state changed to: $isMuted")
    }

    private fun toggleVideo() {
        if (!isVideo) return

        isVideoEnabled = !isVideoEnabled

        if (!isDemoMode) {
            lifecycleScope.launch {
                try {
                    room?.localParticipant?.setCameraEnabled(isVideoEnabled)
                    Log.d("CallActivity", "Camera toggled successfully")

                    if (isVideoEnabled) {
                        // Re-attach video track when enabling
                        isLocalVideoAttached = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            setupLocalVideo()
                        }, 500)
                    } else {
                        // Clear local video when disabling
                        runOnUiThread {
                            binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                            isLocalVideoAttached = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CallActivity", "Failed to toggle camera: ${e.message}")
                    // Fallback to demo mode
                    runOnUiThread {
                        Toast.makeText(this@CallActivity, "Demo mode - Video ${if (isVideoEnabled) "On" else "Off"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Demo mode - Video ${if (isVideoEnabled) "On" else "Off"}", Toast.LENGTH_SHORT).show()
        }

        binding.btnVideo.apply {
            setImageResource(if (isVideoEnabled) R.drawable.ic_videocam else R.drawable.ic_videocam_off)
            animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
                .withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
        }
        Log.d("CallActivity", "Video state changed to: $isVideoEnabled")
    }

    private fun switchCamera() {
        try {
            // For demo purposes, just show the animation
            binding.btnSwitchCamera.animate()
                .rotationBy(180f)
                .setDuration(300)
                .start()
            Toast.makeText(this, "Camera Switched", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to switch camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSpeaker() {
        // Implementation for speaker toggle
        binding.btnSpeaker.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
            .withEndAction {
                binding.btnSpeaker.animate().scaleX(1f).scaleY(1f).setDuration(100)
            }
        Toast.makeText(this, "Speaker Toggled", Toast.LENGTH_SHORT).show()
    }

    private fun endCall() {
        // Prevent multiple calls to endCall
        if (isCallEnding) {
            Log.d("CallActivity", "Call ending already in progress, skipping")
            return
        }

        Log.d("CallActivity", "Ending call")
        isCallEnding = true

        // Stop call timer
        callDurationRunnable?.let { callDurationHandler.removeCallbacks(it) }

        // Show ending animation
        binding.tvCallStatus.text = "Call Ended"
        binding.tvCallStatus.setTextColor(android.graphics.Color.RED)
        binding.tvCallStatus.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction {
                binding.tvCallStatus.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()

        // Clean up video resources
        try {
            // Clear video views
            binding.localVideoView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.remoteVideoView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            
            // Reset initialization flags
            isLocalVideoInitialized = false
            isRemoteVideoInitialized = false
            isLocalVideoAttached = false
            
            Log.d("CallActivity", "Video resources cleared")
        } catch (e: Exception) {
            Log.e("CallActivity", "Error clearing video resources: ${e.message}")
        }

        // Disconnect from room
        try {
            room?.disconnect()
            room = null
            Log.d("CallActivity", "Room disconnected successfully")
        } catch (e: Exception) {
            Log.e("CallActivity", "Error disconnecting from room: ${e.message}")
        }

        // Show ending toast only if not already showing
        if (!isFinishing) {
            Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show()
        }

        // Finish activity after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("CallActivity", "Call ended successfully")
            if (!isFinishing) {
                finish()
            }
        }, 1000)
    }

    private fun showEndCallConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("End Call")
            .setMessage("Are you sure you want to end this call?")
            .setPositiveButton("End Call") { _, _ ->
                endCall()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showParticipantLeftDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Participant Left")
            .setMessage("The other participant has left the call. Would you like to end the call now or wait for it to end automatically?")
            .setPositiveButton("End Now") { _, _ ->
                endCall()
            }
            .setNegativeButton("Wait") { _, _ ->
                // Start auto countdown
                startCallEndCountdown()
            }
            .setCancelable(false)
            .show()
    }

    private fun startCallTimer() {
        callStartTime = System.currentTimeMillis()
        callDurationRunnable = object : Runnable {
            override fun run() {
                val duration = (System.currentTimeMillis() - callStartTime) / 1000
                val minutes = duration / 60
                val seconds = duration % 60
                binding.tvCallDuration.text = String.format("%02d:%02d", minutes, seconds)
                callDurationHandler.postDelayed(this, 1000)
            }
        }
        callDurationHandler.post(callDurationRunnable!!)
    }

    private fun updateCallTypeUI() {
        if (!isVideo) {
            binding.btnVideo.visibility = View.GONE
            binding.btnSwitchCamera.visibility = View.GONE
        }
    }

    private fun showControls() {
        binding.controlsContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()
    }

    private fun hideControls() {
        binding.controlsContainer.animate()
            .alpha(0f)
            .translationY(200f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(300)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .start()
    }

    private fun hideControlsAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            hideControls()
        }, 5000)
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Show a beautiful error animation
            binding.tvCallStatus.text = "Connection Error"
            binding.tvCallStatus.animate()
                .alpha(0.5f)
                .setDuration(500)
                .withEndAction {
                    binding.tvCallStatus.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start()
                }
                .start()
        }
    }


    private fun setupLocalVideo() {
        try {
            val localParticipant = room?.localParticipant
            if (localParticipant != null && isVideo && !isLocalVideoAttached) {
                Log.d("CallActivity", "Setting up local video")

                lifecycleScope.launch {
                    try {
                        // Check if camera is enabled
                        if (localParticipant.isCameraEnabled()) {
                            Log.d("CallActivity", "Camera is enabled, setting up local video")
                            
                            // Get the local video track
                            val videoTrack = localParticipant.getTrackPublication(Track.Source.CAMERA)
                            if (videoTrack is LocalVideoTrack) {
                                runOnUiThread {
                                    try {
                                        // Initialize the video renderer if not already done
                                        if (!isLocalVideoInitialized) {
                                            binding.localVideoView.init(null, null)
                                            binding.localVideoView.setEnableHardwareScaler(true)
                                            binding.localVideoView.setMirror(true)
                                            isLocalVideoInitialized = true
                                        }
                                        
                                        // Add the video track to the renderer
                                        videoTrack.addRenderer(binding.localVideoView)
                                        
                                        isLocalVideoAttached = true
                                        Log.d("CallActivity", "Local video setup completed successfully")
                                    } catch (e: Exception) {
                                        Log.e("CallActivity", "Failed to setup local video renderer: ${e.message}")
                                        binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                                    }
                                }
                            } else {
                                Log.d("CallActivity", "No local video track available")
                                runOnUiThread {
                                    binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                                }
                            }
                        } else {
                            Log.d("CallActivity", "Camera is not enabled")
                            runOnUiThread {
                                binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CallActivity", "Failed to setup local video track: ${e.message}")
                        runOnUiThread {
                            binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CallActivity", "Failed to setup local video: ${e.message}")
        }
    }

    private fun setupRemoteVideo(participant: RemoteParticipant) {
        try {
            if (isVideo) {
                Log.d("CallActivity", "Setting up remote video for ${participant.identity}")

                lifecycleScope.launch {
                    try {
                        // Get the remote video track
                        val videoTrack = participant.getTrackPublication(Track.Source.CAMERA)
                        if (videoTrack is VideoTrack) {
                            runOnUiThread {
                                try {
                                    // Initialize the remote video renderer if not already done
                                    if (!isRemoteVideoInitialized) {
                                        binding.remoteVideoView.init(null, null)
                                        binding.remoteVideoView.setEnableHardwareScaler(true)
                                        binding.remoteVideoView.setMirror(false)
                                        isRemoteVideoInitialized = true
                                    }
                                    
                                    // Add the video track to the renderer
                                    videoTrack.addRenderer(binding.remoteVideoView)
                                    
                                    Log.d("CallActivity", "Remote video setup completed successfully for ${participant.identity}")
                                } catch (e: Exception) {
                                    Log.e("CallActivity", "Failed to setup remote video renderer: ${e.message}")
                                    binding.remoteVideoView.setBackgroundColor(android.graphics.Color.DKGRAY)
                                }
                            }
                        } else {
                            Log.d("CallActivity", "No remote video track available for ${participant.identity}")
                            runOnUiThread {
                                binding.remoteVideoView.setBackgroundColor(android.graphics.Color.DKGRAY)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CallActivity", "Failed to setup remote video: ${e.message}")
                        runOnUiThread {
                            binding.remoteVideoView.setBackgroundColor(android.graphics.Color.DKGRAY)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CallActivity", "Failed to setup remote video: ${e.message}")
        }
    }

    private fun startVideoTrackMonitoring() {
        // Monitor for video track changes every 3 seconds
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                try {
                    // Check local video
                    if (isVideo && !isLocalVideoAttached) {
                        setupLocalVideo()
                    }

                    // Check remote video
                    remoteParticipant?.let { participant ->
                        if (isVideo) {
                            setupRemoteVideo(participant)
                        }
                    }

                    // Continue monitoring
                    Handler(Looper.getMainLooper()).postDelayed(this, 3000)
                } catch (e: Exception) {
                    Log.e("CallActivity", "Error in video track monitoring: ${e.message}")
                }
            }
        }, 3000)
    }

    private fun onTrackSubscribed(event: RoomEvent.TrackSubscribed) {
        val track = event.track
        val participant = event.participant
        
        Log.d("CallActivity", "Track subscribed: ${track.kind} from ${participant.identity}")
        
        if (track is VideoTrack && isVideo) {
            // Handle video track subscription
            if (participant is RemoteParticipant) {
                // Remote participant video
                runOnUiThread {
                    try {
                        // Check if remote video view is already initialized
                        if (!isRemoteVideoInitialized) {
                            binding.remoteVideoView.init(null, null)
                            binding.remoteVideoView.setEnableHardwareScaler(true)
                            binding.remoteVideoView.setMirror(false)
                            isRemoteVideoInitialized = true
                        }
                        track.addRenderer(binding.remoteVideoView)
                        Log.d("CallActivity", "Remote video track attached successfully")
                    } catch (e: Exception) {
                        Log.e("CallActivity", "Failed to attach remote video track: ${e.message}")
                    }
                }
            } else {
                // Local participant video
                runOnUiThread {
                    try {
                        // Check if local video view is already initialized
                        if (!isLocalVideoInitialized) {
                            binding.localVideoView.init(null, null)
                            binding.localVideoView.setEnableHardwareScaler(true)
                            binding.localVideoView.setMirror(true)
                            isLocalVideoInitialized = true
                        }
                        track.addRenderer(binding.localVideoView)
                        isLocalVideoAttached = true
                        Log.d("CallActivity", "Local video track attached successfully")
                    } catch (e: Exception) {
                        Log.e("CallActivity", "Failed to attach local video track: ${e.message}")
                    }
                }
            }
        }
    }

    private fun onTrackUnsubscribed(event: RoomEvent.TrackUnsubscribed) {
        val track = event.track
        val participant = event.participant
        
        Log.d("CallActivity", "Track unsubscribed: ${track.kind} from ${participant.identity}")
        
        if (track is VideoTrack) {
            // Clear the video view when track is unsubscribed
            if (participant is RemoteParticipant) {
                runOnUiThread {
                    binding.remoteVideoView.setBackgroundColor(android.graphics.Color.DKGRAY)
                }
            } else {
                runOnUiThread {
                    binding.localVideoView.setBackgroundColor(android.graphics.Color.GRAY)
                    isLocalVideoAttached = false
                }
            }
        }
    }

    private fun logStep(step: String, details: String = "") {
        Log.d("CallActivity", "$step - $details")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only end call if activity is being destroyed and call is still active
        if ((room != null || isDemoMode) && !isCallEnding) {
            endCall()
        }
    }

    override fun onBackPressed() {
        // Show confirmation dialog when back button is pressed
        showEndCallConfirmation()
    }
}
