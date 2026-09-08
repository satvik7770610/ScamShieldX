# 🛡 ScamShield X — Pre-Action Mobile Security & Threat Console

> **"Don't trust. Verify before you act."**  
> *Core Principle: Verification is not the same as trust.*  
> *Core Loop: DETECT → EXPLAIN → WARN → PROTECT*

---

## 📌 Executive Summary

**ScamShield X** is a privacy-first, on-device Android mobile security application and desktop command center built to solve one of the most critical digital threats: **financial scamming and phishing in the split-second BEFORE the user acts.**

Digital scams (banking phishing, look-alike domain impersonation, fake UPI payment QR codes, recruitment fee demands, and OTP harvesting) trick users by creating artificial urgency or simulating trusted brands. Traditional security tools analyze threats *after* a link is opened or *after* credentials are sent. **ScamShield X provides Pre-Action Security**—intervening on-device before the click, before the payment, and before the OTP.

---

## 🚀 Key Features & Attack Surface Coverage

### 📱 1. Shield Scan (Screen Region Analysis)
- **Select Anything. ScamShield It.** Allows users to select any suspicious portion of their screen while inside other apps (WhatsApp, SMS, Chrome, Email, Telegram, Instagram).
- **Access Methods**:
  - **Quick Settings Tile**: `SCAN WITH SCAMSHIELD` tile in Android Notification Shade for single-tap activation over any app.
  - **Floating Shield Shortcut**: Draggable cyan shield bubble overlay (`🛡`) appearing above other apps.
  - **Home Screen Tool Card**: `06 SHIELD SCAN` entry in the main app menu.
- **Regional Crop & OCR**: Uses Android `MediaProjection` consent capture, displays an interactive drag-to-select cyan overlay box, crops the selection, and runs on-device ML Kit OCR to extract text and URLs without recording the screen or uploading data to the cloud.

### 🔔 2. Notification Shield
- On-device `NotificationListenerService` that inspects incoming SMS, WhatsApp, and app notifications in real time.
- Categorizes messages and alerts the user with an actionable warning before they tap the notification.

### 📷 3. QR Code & UPI Payment Scanner
- CameraX scanner parsing payment URIs (`upi://pay`), inspecting recipient VPAs, payee names, and transaction notes.
- Identifies suspicious payment request notes ("Refund processing fee", "Prize verification deposit").

### 🔗 4. Link & Domain Intelligence
- Evaluates root domain boundaries, registrable hostnames, TLD risk profiles, and brand impersonation patterns (e.g., `github.com.verification.invalid` -> **HIGH RISK**).
- Flags homograph character substitutions, look-alike domains, and unverified redirect paths.

### 💬 5. Text & Recruitment Risk Analyzer
- Differentiates legitimate recruitment notices from scam campaigns demanding upfront registration fees or OTP credentials.
- Flags coercion language, account suspension threats ("Account blocked in 12 hours"), and prize claims.

### 🖼 6. Screenshot OCR Shield
- Multi-URL gallery analysis extracting embedded web links and text from screenshots via ML Kit.

### 📜 7. Threat History Audit Log
- Local Room Database audit trail preserving risk scores, categories, explanations, and timestamped security signals.

### 💻 8. Office Kit & Live Threat Console
- Asynchronous HTTP/SSE sync manager that streams mobile security events in real time to a desktop Command Center dashboard ([http://localhost:8085/console](http://localhost:8085/console)).

---

## 🏗 System Architecture & Analysis Pipeline

```
                              ATTACK SURFACES
 (Notification Shield / Shield Scan / QR / URL / Text / Screenshot)
                                    │
                                    ▼
                          ON-DEVICE ML CLASSIFIER
                      (Local Feature Classifier)
                                    +
                        DETERMINISTIC SECURITY RULES
           (Domain / Message Structure / Payment / Recruitment)
                                    │
                                    ▼
                           RISK FUSION ENGINE
             (Fuses Neural Probabilities & Concrete Evidence)
                                    │
                                    ▼
                         EXPLAINABLE RISK RESULT
                    (0..100 Score, Level, Signals)
                                    │
         ┌──────────────────────────┴──────────────────────────┐
         ▼                                                     ▼
ROOM DATABASE HISTORY                                OFFICE KIT SSE SYNC
 (1 Local Audit Record)                            (Desktop Threat Console)
```

---

## 🛠 Tech Stack & Tools

### Android Mobile Application
- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose (Material3, Custom Dark Cyberpunk Theme)
- **Camera & Vision**: CameraX (1.4.1), ML Kit Barcode Scanning (17.3.0), ML Kit Text Recognition (16.0.1)
- **Database & Storage**: Room Database (2.6.1)
- **Architecture**: MVVM + Coroutines + StateFlow
- **System Integration**: MediaProjection API, Quick Settings `TileService`, Foreground Services, `NotificationListenerService`
- **Target SDK**: `compileSdk = 37`, `minSdk = 24`, `targetSdk = 37`

### Desktop Threat Console & Public Web Showcase
- **Frontend Framework**: React 18, Vite 5
- **Typography**: Space Grotesk, JetBrains Mono, Inter
- **Backend Server**: Node.js, Express, Server-Sent Events (SSE), CORS
- **Design System**: Futuristic Dark Cybersecurity HUD, Canvas 3D Cyber Particle Background, Glassmorphic Panels

---

## 📁 Repository Structure

```
ScamShield-X/
├── app/                                  # Android Application Module
│   ├── src/main/AndroidManifest.xml
│   └── src/main/java/com/example/scamshield/
│       ├── analysis/                     # ML Kit OCR Managers
│       ├── camerashield/                 # Camera OCR & Live Message Shield
│       ├── data/                         # Threat History Repository & Room DB
│       ├── db/                           # Room Entity, DAO, Database
│       ├── ml/                           # On-Device Feature Classifier & Model Manager
│       ├── model/                        # Risk Models, Signals, & Formatter
│       ├── notification/                 # NotificationListenerService
│       ├── officekit/                    # Office Kit Sync Manager (Phone → Laptop)
│       ├── risk/                         # Risk Engine, Analyzers & Fusion Engine
│       │   ├── analyzer/                 # Domain, Payment, Text, Recruitment Analyzers
│       │   └── fusion/                   # FusionEngine (ML + Rules)
│       ├── scanner/                      # CameraX QR Code Scanner
│       ├── shieldscan/                   # Shield Scan Module (Tile, Overlay, Activity)
│       └── ui/                           # Jetpack Compose Screens, Theme, Navigation
└── threat-console/                       # Web Desktop Command Center & Showcase
    ├── server.js                         # Express SSE Server (Port 8085)
    ├── vite.config.js                    # Vite Build Configuration
    └── src/
        ├── App.jsx                       # Client-side Route Manager & 3D Background
        ├── components/
        │   ├── LandingPage.jsx           # Product Landing Page Showcase
        │   └── ThreatConsole.jsx         # Live Threat Investigation Dashboard
        └── index.css                     # Cyberpunk HUD Stylesheet
```

---

## ⚙️ Installation & Setup Guide

### 1. Android Mobile App

#### Requirements:
- Android Studio Ladybug (2024.2+) or newer
- JDK 11 or 17
- Physical Android device (Android 7.0 / API 24+) or Android Emulator

#### Build & Run:
```bash
# Clone the repository
git clone https://github.com/scamshield/scamshield-x.git
cd ScamShield-X

# Build Debug APK
./gradlew :app:assembleDebug

# Install on connected Android device
./gradlew :app:installDebug
```

---

### 2. Desktop Threat Console

#### Requirements:
- Node.js (v18+) & npm

#### Setup & Launch:
```bash
# Navigate to threat-console directory
cd threat-console

# Install dependencies
npm install

# Build production frontend bundle
npm run build

# Start the Express SSE server
npm start
```

Open your browser at:
- **Product Landing Page**: [http://localhost:8085/](http://localhost:8085/)
- **Live Threat Console**: [http://localhost:8085/console](http://localhost:8085/console)

---

### 3. Connecting Phone to Laptop Console (Local Wi-Fi)

1. Connect both your Android phone and Laptop to the **same local Wi-Fi network**.
2. Find your laptop's local IP address using `ipconfig` (e.g., `192.168.31.247`).
3. Open **ScamShield X** on your phone -> **Settings → PHONE CONNECTION**.
4. Enter your laptop's local IP (`192.168.31.247`) and tap **`[ TEST CONNECTION ] (PING)`**.
5. Once connected (**`CONNECTED ●`**), any scan or notification threat on your phone streams live to your desktop console!

---

## 🧪 Test Suite & Verification

The project includes an extensive test suite verifying detection accuracy, adversarial security, and module integration:

```bash
# Run unit test suite
./gradlew :app:testDebugUnitTest
```

**Test Results**: **105 passed, 0 skipped, 0 failed**
- `BankPhishingFalseNegativeDiagnosticTest`: Verifies 100/100 risk score for bank phishing campaigns while keeping legitimate bank transaction alerts at 0/100 LOW RISK.
- `AdversarialUrlSecurityTest`: Verifies root domain parsing and look-alike TLD flags.
- `ShieldScanIntegrationTest`: Verifies Regional Screen Capture OCR text extraction through ML Classifier and Risk Fusion.

---

## ⚖️ Hackathon & Privacy Disclaimer

- **Privacy First**: Analysis is performed on-device whenever possible. Screen capture bitmaps are processed locally and discarded immediately. No SMS text or credentials are uploaded to external cloud services.
- **Pre-Action Guidance**: ScamShield X provides explainable security signals to assist users. Risk scores (0..100) indicate threat severity and do not guarantee safety.
- **iQOO Hackathon Submission**: Prototype built for synthetic demonstration and security research.
