#!/bin/bash

# Source configuration
source ./config.sh

echo "🔍 LiveKit Demo - Setup Verification"
echo "=================================="

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK Platform Tools."
    exit 1
fi

echo "✅ ADB found"

# Check for connected devices
echo ""
echo "📱 Checking for connected devices..."
DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)

if [ $DEVICES -eq 0 ]; then
    echo "❌ No Android devices connected"
    echo "   Please connect a device or start an emulator"
    exit 1
elif [ $DEVICES -eq 1 ]; then
    echo "⚠️  Only one device connected"
    echo "   For device-to-device testing, connect a second device"
else
    echo "✅ Multiple devices detected ($DEVICES devices)"
fi

# Check if app is installed
echo ""
echo "📦 Checking if LiveKit Demo app is installed..."
if adb shell pm list packages | grep -q "$APP_PACKAGE_NAME"; then
    echo "✅ LiveKit Demo app is installed"
else
    echo "❌ LiveKit Demo app not found"
    echo "   Please build and install the app first"
    exit 1
fi

# Test API connectivity
echo ""
echo "🌐 Testing LiveKit API connectivity..."
API_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_TEST_URL")

if [ "$API_RESPONSE" = "200" ]; then
    echo "✅ LiveKit API is accessible"
else
    echo "❌ LiveKit API returned status: $API_RESPONSE"
    echo "   Check your internet connection"
fi

echo ""
echo "🎯 Testing Instructions:"
echo "======================="
echo ""
echo "1. Install the app on two devices"
echo "2. Grant camera and microphone permissions"
echo "3. On Device 1:"
echo "   - Enter your name in 'Your ID'"
echo "   - Enter target name in 'Person to Call'"
echo "   - Click 'Video Call'"
echo ""
echo "4. On Device 2:"
echo "   - Enter your name in 'Your Name'"
echo "   - Enter room name '$DEFAULT_ROOM_NAME' in 'Room Name'"
echo "   - Click 'Join Room'"
echo ""
echo "5. Both devices should connect and show 'Connected' status"
echo ""
echo "🔍 For debugging, run:"
echo "   adb logcat | grep 'CallActivity\|MainActivity'"
echo ""
echo "✅ Setup verification complete!" 