# Ainaa - Project Context & Agent Instructions

## Overview
Ainaa is an advanced anti-pornography and digital safety application designed to help users maintain a clean browsing experience and focus. It provides robust uninstall protection and uses a secure VPN tunnel to filter and block harmful content.

## Key Features
- **Uninstall Protection:** Prevents users from removing the app or its permissions by analyzing screen UI and blocking dangerous system activities.
- **Content Filtering via VPN:** Uses a custom VPN service to filter and block targeted URLs and domains.
- **App & Keyword Blocking:** Monitors and restricts specific apps or words instantly.
- **Accessibility Enforcement:** Uses Android Accessibility Services to maintain protection settings and prevent bypass attempts.
- **Overlay Monitoring:** Displays real-time content warnings or lock screens when violations are detected.
- **Background Operations:** WorkManager handles continuous monitoring and protection tasks.

## Tech Stack
- **Language:** Kotlin (100%)
- **UI:** Jetpack Compose + Navigation Component (Nav3)
- **Architecture:** MVVM + Clean Architecture (Layers: `domain`, `data`, `ui`, `viewmodels`)
- **Dependency Injection:** Koin (Configured in `di/Di.kt`)
- **Networking:** Ktor Client (`data/remote/KtorRepo.kt`) with `kotlinx.serialization`
- **Scripting:** Mozilla Rhino (org.mozilla:rhino:1.7.14) for evaluating JavaScript heuristic scripts against UI trees.
- **Local Storage:** SharedPreferences (managed in `data/local/SharedPrefs.kt`)
- **Background Tasks:** WorkManager

## Project Structure & Core Components
The core logic resides under `app/src/main/java/com/mafazaa/ainaa/`:
- `service/MyAccessibilityService.kt`: The brain of the system. Scans `onAccessibilityEvent`, checks apps, and triggers blocks.
- `helpers/ScreenAnalyser.kt`: Maps UI nodes to an internal tree for evaluation.
- `helpers/LockOverlayManager.kt`: Manages the display of blocking overlays.
- `utils/Constants.kt`: Hosts essential variables, app lists, and device-specific JS heuristic scripts (`defaultCodes`).
- `ui/`: Compose-based UI organized by feature domains.
- `viewmodels/`: bridges UI with repositories.

## Development Guidelines
- **Dependency Injection:** Use Koin. Register new repos/managers in `di/Di.kt`. Use `by inject()` in non-compose classes and constructor injection in ViewModels.
- **UI:** Use Jetpack Compose and Nav3.
- **Screen Analysis:** Evasion patterns are detected using JS scripts mapped to specific devices (Xiaomi, Samsung, Realme). Check `Constants.kt` for these scripts.
- **Dependencies:** Managed via Gradle version catalogs (`gradle/libs.versions.toml`).
- **Permissions:** The app relies heavily on Accessibility and VPN permissions. Handle state changes via `PermissionState.kt`.
