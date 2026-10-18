# InAir — "Your phone, in the air."

InAir turns any Android device into an ultra-low-latency wireless workstation, allowing nearby computers, tablets, or phones to view the screen, send gestures and keystrokes, and transfer files seamlessly over a high-speed local Wi-Fi or Local-Only Hotspot without leaving the local network.

[![Build InAir APK](https://github.com/ashrefknewy6-del/inair2/actions/workflows/build-apk.yml/badge.svg)](https://github.com/ashrefknewy6-del/inair2/actions/workflows/build-apk.yml)
[![Download APK](https://img.shields.io/badge/Download-InAir.apk-FF2D3F?style=for-the-badge&logo=android)](https://github.com/ashrefknewy6-del/inair2/releases/latest/download/InAir.apk)

## Direct APK Download

Download the latest version directly to your phone:
**[Download InAir.apk](https://github.com/ashrefknewy6-del/inair2/releases/latest/download/InAir.apk)**

## Features

- **Real-Time Screen Mirroring**: HTTP Motion JPEG (MJPEG) ultra-low-latency streaming on port 8080 with adjustable resolution (720p, 1080p, 1080p HD) and frame rate (15, 30, 60 FPS).
- **Remote Touch & Navigation**: Native Android `AccessibilityService` integration allowing mouse clicks, touch swipes, and system buttons (Back, Home, Recents, Notifications) from any desktop browser.
- **Wireless File Hub**: Drag-and-drop file transfers between browser and phone, strictly isolated to a user-chosen Storage Access Framework (SAF) folder with per-file progress meters.
- **Automatic & Manual Hotspot**: `WifiManager.startLocalOnlyHotspot` support with QR code pairing and manual SSID/Password credential configuration with live password strength meters.
- **Privacy & Security First**: 100% offline and local (zero cloud telemetry), interactive on-screen device approval prompts, 4-digit PIN protection, IP blacklisting/kicking, and auto-stop idle sleep timer.
- **Strict Material Design 3**: Dark-first aesthetic with signature 135° red gradient (#FF2D3F to #B3001B), glassmorphism 28dp cards, radar-style pulsing power button, and animated bandwidth graphs.
- **Bilingual & RTL**: Complete English and Arabic localization with Inter and Cairo typography.

## Architecture

- **Language**: Kotlin 100%
- **UI Toolkit**: Jetpack Compose with Material 3 (Material You)
- **Architecture**: MVVM with Clean Architecture and Kotlin Coroutines/Flow
- **Server Engine**: Pure Kotlin non-blocking embedded HTTP & MJPEG server
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)

## Build Locally

To assemble the debug APK:
```bash
./gradlew assembleDebug
```

To run unit and Robolectric tests:
```bash
./gradlew testDebugUnitTest
```
