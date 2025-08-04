package com.example.livekitdemo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.livekitdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            Toast.makeText(this, "Permissions required for video calling", Toast.LENGTH_LONG).show()
        }
    }
    private lateinit var apiService: LiveKitAPIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = LiveKitAPIService()

        requestPermissions()
        setupUI()
        // Apply window insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.INTERNET
        )
        val hasPermissions = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions) {
            permissionLauncher.launch(permissions)
        }
    }

    private fun setupUI() {
        binding.apply {
            btnVideoCall.setOnClickListener {
                startCall(true, true)
            }

            btnAudioCall.setOnClickListener {
                startCall(false, true)
            }

            btnJoinRoom.setOnClickListener {
                joinExistingRoom()
            }

            btnTestAPI.setOnClickListener {
                testAPIConnection()
            }
        }
    }

    private fun startCall(isVideo: Boolean, isInitiator: Boolean) {
        val roomName = binding.etRoomName.text.toString().trim().ifEmpty { UrlFactory.DEFAULT_ROOM_NAME }
        val participantName = binding.etParticipantName.text.toString().trim().ifEmpty { "user_${UUID.randomUUID().toString().take(8)}" }
        val callerId = binding.etCallerId.text.toString().trim().ifEmpty { participantName }
        val calleeId = binding.etCalleeId.text.toString().trim().ifEmpty { "remote_user" }
        
        val intent = Intent(this, CallActivity::class.java).apply {
            putExtra("IS_VIDEO", isVideo)
            putExtra("IS_INITIATOR", isInitiator)
            
            if (isInitiator) {
                putExtra("CALLER_ID", callerId)
                putExtra("CALLEE_ID", calleeId)
            } else {
                putExtra("ROOM_NAME", roomName)
                putExtra("PARTICIPANT_NAME", participantName)
            }
        }

        Log.d("MainActivity", "✅ Launching CallActivity with intent data - Room: $roomName, Participant: $participantName")
        startActivity(intent)
    }

    private fun joinExistingRoom() {
        val roomName = binding.etRoomName.text.toString().trim()
        val participantName = binding.etParticipantName.text.toString().trim()
        if (roomName.isEmpty()) {
            Toast.makeText(this, "Please enter Room Name", Toast.LENGTH_SHORT).show()
            return
        }

        if (participantName.isEmpty()) {
            Toast.makeText(this, "Please enter Participant Name", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CallActivity::class.java).apply {
            putExtra("ROOM_NAME", roomName)
            putExtra("PARTICIPANT_NAME", participantName)
            putExtra("IS_VIDEO", true)
            putExtra("IS_INITIATOR", false)
        }

        Log.d("MainActivity", "✅ Joining existing room: $roomName as $participantName")
        startActivity(intent)
    }

    private fun testAPIConnection() {
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "Testing API connection...")
                val result = apiService.joinRoom(UrlFactory.DEFAULT_ROOM_NAME, UrlFactory.DEFAULT_PARTICIPANT_NAME)
                Log.d("MainActivity", "API test successful: ${result.serverUrl}")
                Toast.makeText(this@MainActivity, "API Test: SUCCESS - ${result.serverUrl}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "API test failed: ${e.message}")
                Toast.makeText(this@MainActivity, "API Test: FAILED - ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}