# InAir — "Your phone, in the air."

InAir turns any Android device into an ultra-low-latency wireless workstation, allowing nearby computers, tablets, or phones to view the screen, send gestures and keystrokes, and transfer files seamlessly over a high-speed local Wi-Fi or Local-Only Hotspot without leaving the local network.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during Phase 1 clarification:
> - **Screen Streaming Protocol**: Standard HTTP Motion JPEG (MJPEG) streaming for zero-install universal browser compatibility across all desktop, tablet, and mobile browsers.
> - **Default Network Pairing**: Automatic Local-Only Hotspot (`WifiManager.startLocalOnlyHotspot`) accompanied by dynamic QR code pairing and manual hotspot credentials fallback.
> - **Client Access Security**: Instant on-screen approval prompts on the phone whenever a new browser requests connection or screen access, with fallback 4-digit PIN protection configurable in Settings.

- **Confirmed Decision 1**: HTTP MJPEG streaming served over embedded HTTP server on port 8080.
- **Confirmed Decision 2**: Local-Only Hotspot generation with clear SSID/Password display, Wi-Fi tethering shortcut, and QR Code display.
- **Confirmed Decision 3**: Explicit user authorization dialog on the phone for incoming client sessions.
- **Dependency Strategy**: Lightweight embedded HTTP server and WebSocket engine with non-blocking Java NIO/ServerSocket. Avoids third-party binary mismatches and provides zero-dependency streaming, multipart uploads, and remote control commands.

---

## 1. Overview & Core Concept

- **What It Does**: InAir broadcasts the phone's display in real time, accepts touch and navigation gestures (Back, Home, Recents, clicks, swipes) via Android's Accessibility Service, and manages bidirectional file transfers through a responsive web dashboard served directly from the phone.
- **Target Audience / Persona**: Professionals presenting directly from their phones, developers and testers operating headless or remote test devices, educators, and everyday users wanting to wirelessly transfer files and control their devices from a PC/Mac browser without cables or third-party cloud servers.
- **Key Value**: 100% private and offline-first (no internet required, zero cloud telemetry), high performance, and zero client setup needed on the receiving laptop or tablet.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Onboarding & Permission Primer**: 3-stage visual walkthrough illustrating Screen Cast, Remote Gestures, and File Access with prominent privacy disclosures prior to permission prompts.
2. **One-Tap Home Broadcasting**: Circular pulsating power button with radar-style glow ripples when broadcasting. Displays network status chip, connection IP:port, copyable password, dynamic QR code, and connected devices count.
3. **Screen Mirroring & Quality Tuning**: Configurable Low (720p 15fps), Medium (1080p 30fps), and High (1080p 60fps) streaming presets with live thumbnail preview.
4. **Interactive Remote Control**: Clear setup guide that routes to Accessibility Settings with real-time service status validation and safety disclaimers.
5. **Secure Storage File Hub**: Storage Access Framework (SAF) folder picker for dedicated shared folder isolation, upload progress meters, and web-to-phone downloads.
6. **Device Gatekeeper & Settings**: Real-time connected clients list with one-tap kick/block, auto-stop sleep timer (15m, 30m, 1h, Never), PIN locks, and full bilingual English / Arabic (RTL) localization.

### Visual Identity & Theme
- **Theme Posture**: Premium dark-first luxury aesthetic with deep contrast and rich glowing accents.
- **Color Palette**:
  - Background: Near-black `#0D0507`
  - Surface: Rich deep maroon `#1A0A0D`
  - Surface Variant: `#2A1216`
  - Primary Accent: Vivid Red `#FF2D3F`
  - Primary Container: Deep Wine `#7A0012`
  - Secondary Accent: Coral Flame `#FF6B6B`
  - On-Surface: Crisp Soft Rose `#FBEAEA`
  - Signature Gradient: 135° Red Gradient (`#FF2D3F` to `#B3001B`) applied to main interactive actions, progress indicators, and active badges.
- **Typography & Hierarchy**:
  - English: Clean sans-serif hierarchy (display headings in bold geometric sans, body in high-legibility sans).
  - Arabic: Full RTL layout support with Cairo typography styling and comprehensive `res/values/strings.xml` and `res/values-ar/strings.xml` parity.
- **Component Styling & Depth**:
  - 28dp smooth rounded cards with subtle red hairline borders (`#FF2D3F` at 15% opacity).
  - Ambient red glow effects on the central broadcast power trigger.
  - Full edge-to-edge layout with dynamic top app bar and safe insets navigation.

### Interactive Feedback & Motion
- Animated splash screen with sequentially drawing wireless signal arcs.
- Radar ripples expanding outward from the power button while broadcasting.
- Spring-based animations for connected device cards entering and exiting.
- Haptic tactile feedback on key toggles and connection events.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: MJPEG Screen Streaming Engine**
  - *Chosen Approach*: `MediaProjection` capturing frames to a `VirtualDisplay` and `ImageReader`, compressing JPEG frames, and streaming over HTTP `multipart/x-mixed-replace; boundary=frame`.
  - *Why*: Eliminates complex WebRTC STUN/TURN negotiations and custom client player software; any browser natively renders MJPEG at 30-60 FPS with single-digit millisecond latency over local Wi-Fi.
  - *Alternatives Considered*: H.264 over WebSocket requires client-side WASM or Canvas decoders which struggle with latency and browser compatibility.
- **Decision 2: Embedded Server Engine**
  - *Chosen Approach*: Lightweight, high-throughput Kotlin non-blocking HTTP and WebSocket server with direct static asset caching and streaming endpoints.
  - *Why*: Eliminates brittle external third-party library dependency version conflicts and gives exact control over chunked uploads, range requests for file downloads, and client connection management.
- **Decision 3: Remote Control Dispatch**
  - *Chosen Approach*: Android `AccessibilityService` utilizing `dispatchGesture()` for tap, swipe, and scroll events, plus `performGlobalAction()` for Back, Home, and Recents.
  - *Why*: Official standard Android API for system-wide remote accessibility without requiring root access or developer ADB privileges.
- **Decision 4: State Management & Architecture**
  - *Chosen Approach*: Clean Architecture MVVM with `StateFlow` and centralized repository singletons.
  - *Why*: Provides clean separation of concerns, strict testability, and resilient background service binding without fragile annotation processor overhead.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        InAir Android Application                       │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│   UI Layer (Jetpack Compose - Material 3)                             │
│   ├── OnboardingScreen (3-stage permission walkthrough)               │
│   ├── HomeScreen (Power Radar, Status, QR Code, Stats)                 │
│   ├── NetworkSetupScreen (Hotspot settings, SSID/Pass, Strength)       │
│   ├── ScreenShareScreen (Quality, FPS, Live Thumbnail)                │
│   ├── RemoteControlScreen (Accessibility guide, active toggle)        │
│   ├── FileShareScreen (SAF folder picker, upload/download queue)       │
│   ├── ConnectedDevicesScreen (IP, connected time, kick/block)         │
│   └── SettingsScreen (Theme, Language, Auto-Stop, PIN, Privacy)       │
│                                │                                       │
│                                ▼                                       │
│   InAirViewModel (StateFlow & Intent Handlers)                         │
│                                │                                       │
│                                ▼                                       │
│   Core Repositories & Controllers                                      │
│   ├── HotspotManager (LocalOnlyHotspot + Wi-Fi IP detection)          │
│   ├── ScreenCaptureService (MediaProjection + ImageReader + FPS)       │
│   ├── InAirAccessibilityService (Gesture dispatch & Global actions)   │
│   ├── FileShareManager (Storage Access Framework content resolver)     │
│   ├── SecurityManager (Session PIN, Client approvals, Blacklist)      │
│   └── InAirWebServer (HTTP server + MJPEG stream + WebSocket API)      │
│                                │                                       │
└────────────────────────────────┼───────────────────────────────────────┘
                                 │ Local Wi-Fi / Hotspot
                                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 Connected Client Web Dashboard (HTML5)                 │
│  - Responsive Red Gradient Dark Theme                                  │
│  - Live MJPEG Stream with Touch/Mouse Interaction (Tap, Drag, Wheel)   │
│  - System Keys (Back, Home, Recents, Volume)                           │
│  - Drag-and-drop File Upload & Download Tree                           │
│  - Real-time Connection Quality & Latency Indicator                    │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- `BroadcastState`: `OFFLINE`, `STARTING`, `BROADCASTING`, `ERROR`
- `ClientSession`: `id`, `ipAddress`, `deviceName`, `connectedAt`, `isAuthorized`, `isBlocked`
- `StreamConfig`: `resolution` (720p, 1080p), `targetFps` (15, 30, 60), `compressionQuality` (50-90)
- `FileTransferItem`: `id`, `fileName`, `fileSize`, `bytesTransferred`, `direction` (UPLOAD/DOWNLOAD), `status`
- `SecuritySettings`: `requireApproval`, `pinProtectionEnabled`, `accessPin`, `autoStopTimeoutMinutes`

### Interactive Component & State Mapping
- **Home Power Action**: Toggles broadcast service. If permissions (Notification, MediaProjection) are not granted, directs smoothly to permission flows; starts `LocalOnlyHotspot` and binds `InAirWebServer`.
- **Remote Control Toggle**: Checks accessibility enabled status; if inactive, displays explanatory safety modal and launches system accessibility intent.
- **Folder Picker**: Invokes `OpenDocumentTree` contract, persists URI permissions, and updates web server root download/upload directory.
- **Device Management**: Kick immediately terminates client socket; block registers IP in `SecurityManager` blacklist, preventing re-authentication.
