# LiveKit Demo - Device-to-Device Communication

This Android app demonstrates real-time video and audio communication between devices using LiveKit.

## Features

- ✅ Real-time video calling
- ✅ Audio calling
- ✅ Room-based communication
- ✅ Mute/unmute functionality
- ✅ Video on/off toggle
- ✅ Beautiful UI with animations
- ✅ Connection status monitoring
- ✅ Demo mode fallback

## How to Test Device-to-Device Communication

### Prerequisites

1. **Two Android devices** (or one device + emulator)
2. **Internet connection** on both devices
3. **Camera and microphone permissions** granted

### Method 1: Direct Call (Same Room)

1. **On Device 1:**
   - Open the app
   - Enter your name in "Your ID" field (e.g., "Alice")
   - Enter the person to call in "Person to Call" field (e.g., "Bob")
   - Click "Video Call" or "Audio Call"

2. **On Device 2:**
   - Open the app
   - Enter your name in "Your Name" field (e.g., "Bob")
   - Enter the same room name in "Room Name" field (e.g., "test")
   - Click "Join Room"

3. **Both devices will connect to the same room** and can communicate with each other.

### Method 2: Room-Based Communication

1. **Create a room:**
   - On Device 1, enter a unique room name (e.g., "meeting123")
   - Enter your name (e.g., "Host")
   - Click "Join Room"

2. **Join the same room:**
   - On Device 2, enter the same room name ("meeting123")
   - Enter your name (e.g., "Participant")
   - Click "Join Room"

3. **Both devices will join the same room** and can communicate.

### Testing Steps

1. **Test API Connection:**
   - Click "Test API Connection" to verify the LiveKit API is working
   - You should see "API Test: SUCCESS" message

2. **Test Video Call:**
   - Start a video call on one device
   - Join the same room on another device
   - Verify that both devices show "Connected" status
   - Check that video streams are working

3. **Test Audio Call:**
   - Start an audio call on one device
   - Join the same room on another device
   - Verify audio communication

4. **Test Controls:**
   - Mute/unmute button should work
   - Video on/off toggle should work
   - End call button should disconnect both devices

### Troubleshooting

#### If devices don't connect:

1. **Check API Connection:**
   - Click "Test API Connection" on both devices
   - Ensure both show "SUCCESS"

2. **Check Room Name:**
   - Make sure both devices use the same room name
   - Room names are case-sensitive

3. **Check Permissions:**
   - Grant camera and microphone permissions
   - Restart the app if needed

4. **Check Network:**
   - Ensure both devices have stable internet connection
   - Try switching between WiFi and mobile data

#### If video/audio doesn't work:

1. **Check device permissions**
2. **Try restarting the app**
3. **Check if demo mode is active** (indicated in status)

### Demo Mode

The app includes a demo mode that activates when:
- API connection fails
- Network issues occur
- LiveKit server is unavailable

In demo mode:
- UI shows "Demo Mode - Connected"
- Placeholder video/audio is shown
- All controls work with simulated responses

### Logs and Debugging

Check the Android logs for detailed information:
```bash
adb logcat | grep "CallActivity\|MainActivity"
```

Key log messages to look for:
- `✅ Launching CallActivity with intent data`
- `API call successful, connecting to room...`
- `Room connection completed successfully!`
- `Remote participant connected: [name]`

### Room Management

- **Room names** are shared between devices
- **Participant names** should be unique within a room
- **Rooms are temporary** and expire after inactivity
- **Multiple participants** can join the same room

### Security Notes

- This demo uses a public LiveKit sandbox
- For production use, implement proper authentication
- Consider using private LiveKit instances for sensitive communications

## Technical Details

### Architecture

- **LiveKit SDK**: Real-time communication
- **Retrofit**: API communication
- **Coroutines**: Asynchronous operations
- **ViewBinding**: UI management

### Key Components

- `MainActivity`: Room setup and participant management
- `CallActivity`: Real-time communication interface
- `LiveKitAPIService`: API communication layer
- `LiveKitAPI`: Retrofit interface

### API Endpoints

- Base URL: `https://cloud-api.livekit.io/api/sandbox/`
- Sandbox ID: `viveklivekit-1rg10o`
- Endpoint: `GET connection-details/`

## Building and Running

1. **Clone the repository**
2. **Open in Android Studio**
3. **Build and run on two devices**
4. **Follow the testing steps above**

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review the logs for error messages
3. Ensure both devices have proper permissions
4. Verify internet connectivity

---

**Happy Testing! 🎥📞** 