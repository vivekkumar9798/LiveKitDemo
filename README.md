# LiveKit Demo – Device-to-Device Audio Communication

This Android app demonstrates real-time **audio-only** communication between devices using the [LiveKit](https://livekit.io) SDK.

---

## 🎧 Features

- Real-time **audio calling**
- Room-based audio communication
- Mute/unmute functionality
- Connection status monitoring
- Fallback **Demo Mode** for offline simulation
- Lightweight and optimized for audio use cases

---
## 📸 Screenshots
![Screenshot_20250804_185651](https://github.com/user-attachments/assets/4bd7da1a-8d02-4c01-a541-552998a8ab62)
![Screenshot_20250804_185723](https://github.com/user-attachments/assets/decd2fe0-00c1-4412-b9d5-bdc3d170095e)
![Screenshot_20250804_185728](https://github.com/user-attachments/assets/312fca57-81d7-4b51-a390-0bce12b4adfc)

## 🧪 How to Test Audio Communication

### 📋 Prerequisites

- Two Android devices (or one device + emulator)
- Internet connection on both devices
- **Microphone permission** granted on both devices

---

### 🔗 Steps to Start Audio Call

#### On **Device 1**:
1. Open the app
2. Enter your name in **"Your ID"** field (e.g., `Alice`)
3. Enter the name of the person to call in **"Person to Call"** (e.g., `Bob`)
4. Tap **Audio Call**

#### On **Device 2**:
1. Open the app
2. Enter your name in **"Your Name"** (e.g., `Bob`)
3. Enter the same room name in **"Room Name"** (e.g., `audio-room`)
4. Tap **Join Room**

➡️ Both devices will connect to the same audio room and begin real-time voice communication.

---

## ✅ Testing Steps

1. **Test API Connection**  
   - Tap **"Test API Connection"**  
   - Expected result: `API Test: SUCCESS`

2. **Start Audio Call**  
   - Initiate the audio call from one device  
   - Join the same room from the second device  
   - Confirm both devices show **Connected**  
   - Speak into one device and ensure the other device receives the audio clearly

3. **Test Mute/Unmute**  
   - Use the mute button to disable/enable microphone input

4. **End Call**  
   - Tap the **End Call** button to disconnect both devices

---

## 🛠️ Troubleshooting

### Devices Not Connecting?
- ✅ Ensure **Test API Connection** shows success
- ✅ Double-check **Room Name** (must match exactly)
- ✅ Microphone permission must be granted
- ✅ Restart the app if needed
- ✅ Check internet connection

### Audio Not Working?
- Ensure both devices have microphone access
- Try restarting the app
- Confirm if **Demo Mode** is active (see below)

---

## 🧪 Demo Mode

The app automatically enters Demo Mode if:
- API connection fails
- Network is unavailable
- LiveKit server is unreachable

In Demo Mode:
- You’ll see **"Demo Mode - Connected"**
- Audio functionality is simulated
- Mute/unmute and end call controls behave as if connected

---
