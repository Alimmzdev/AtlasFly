# AtlasFly

**A production-oriented Android flight & travel companion — built to demonstrate modern Kotlin, Compose, and clean architecture.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20Multi--module-2EA043)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> Portfolio project by **ALI Mohammadzadeh** — Android developer focused on scalable architecture, polished UX, and maintainable Kotlin codebases.  
> Open to opportunities across **Europe (EU/EEA)** · Remote or relocation.

---

## Why this project exists

AtlasFly is not a tutorial clone. It is a **deliberately structured Android application** that mirrors how I build software in professional teams:

- **Modular boundaries** that scale with team size and feature velocity
- **Unidirectional data flow** (MVI-style) for predictable UI state
- **Security-first auth** with encrypted local storage and OAuth providers
- **Modern Android stack** aligned with what EU product companies expect in 2026

If you are a recruiter, hiring manager, or fellow Android developer, this repository is meant to answer one question quickly: *Can this engineer design, implement, and document a real Android product?*

---

## Demo

### Video walkthrough

<!-- Replace the link below with your hosted demo (YouTube, Loom, Google Drive, etc.) -->
[![AtlasFly demo video](docs/screenshots/00-demo-video-thumbnail.png)](docs/videos/atlasfly-demo.mp4)

> **Placeholder:** Add your screen recording to [`docs/videos/atlasfly-demo.mp4`](docs/videos/)  
> Recommended: 60–90 seconds covering splash → auth → email verification → home navigation.

**Suggested hosting options:** YouTube (unlisted), Loom, or GitHub release assets.

---

## Screenshots

| Splash & branding | Authentication | Email verification |
|:---:|:---:|:---:|
| ![Splash screen placeholder](docs/screenshots/01-splash.png) | ![Auth screen placeholder](docs/screenshots/02-auth-login.png) | ![Email verification placeholder](docs/screenshots/03-email-verification.png) |
| *Splash screen* | *Login / Sign up* | *Verify email flow* |

| Social sign-in | Deep link handling | Home (WIP) |
|:---:|:---:|:---:|
| ![Social login placeholder](docs/screenshots/04-social-login.png) | ![Deep link placeholder](docs/screenshots/05-deep-link-verification.png) | ![Home placeholder](docs/screenshots/06-home.png) |
| *Google & GitHub OAuth* | *Email link opens app* | *Main destination* |

> **Placeholder:** Drop PNG/WebP files into [`docs/screenshots/`](docs/screenshots/).  
> See [`docs/media/README.md`](docs/media/README.md) for naming conventions and capture tips.

---

## Highlights for reviewers

| Area | What to look at |
|---|---|
| **Architecture** | Multi-module Clean Architecture: `app` → `feature` → `service` → `core` |
| **UI pattern** | Compose + MVI (`UiState` / `UiIntent` / `Event`) in ViewModels |
| **Auth** | Email/password, Google, GitHub via Firebase Auth |
| **Security** | Auth tokens encrypted with Google Tink + DataStore |
| **Navigation** | Type-safe routes with Navigation 3 |
| **Deep links** | Email verification handled in `MainActivity` → `AtlasFlyViewModel` |
| **DI** | Hilt modules across network, local, and auth layers |
| **Build hygiene** | Gradle Version Catalog, KSP, Kotlin DSL |

---

## Features

### Implemented

- [x] Multi-module project scaffold (`app`, `core`, `feature`, `service`)
- [x] Jetpack Compose UI with Material 3
- [x] Splash screen & app shell with auth gate
- [x] Email/password sign-up and sign-in
- [x] Google Sign-In (Credential Manager)
- [x] GitHub OAuth login
- [x] Sign-up email verification screen with resend
- [x] Deep link parsing for email verification (`oobCode` + verified landing)
- [x] Encrypted auth token persistence (Tink + DataStore)
- [x] Ktor HTTP client with debug network inspection (Chucker)
- [x] Hilt dependency injection across layers
- [x] Use-case driven domain layer (`LoginUseCase`, `VerifyEmailUseCase`, …)

### Roadmap

- [ ] Flight search & results
- [ ] Travel itinerary planning
- [ ] User profile & settings
- [ ] Offline caching strategy
- [ ] Unit & UI test coverage expansion
- [ ] CI pipeline (build, lint, test)

---

## Architecture

AtlasFly follows **Clean Architecture** with strict module boundaries and a **feature-first** organization.

```
┌─────────────────────────────────────────────────────────────┐
│                         :app                               │
│   Compose UI · Navigation · Deep links · Hilt entry point  │
└──────────────────────────┬──────────────────────────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         ▼                 ▼                 ▼
   ┌───────────┐    ┌────────────┐    ┌─────────────┐
   │ :feature  │    │  :service  │    │    :core    │
   │ auth      │    │  domain    │    │  network    │
   │ home      │    │  data      │    │  local      │
   │ search …  │    │ (Firebase) │    │  navigation │
   └───────────┘    └────────────┘    └─────────────┘
```

### Data flow (MVI-style)

```
User action → UiIntent → ViewModel → UseCase → Repository → DataSource
                ↑                                              │
                └──────── StateFlow<UiState> ←─────────────────┘
```

### Module map

```
AtlasFly/
├── app/                    # Application entry, theme, navigation shell
├── core/
│   ├── design-system/      # Shared UI tokens & components
│   ├── presentation/       # Base presentation utilities
│   ├── navigation/         # Type-safe Routes (kotlinx.serialization)
│   ├── network/            # Ktor client, Hilt NetworkModule, Chucker
│   └── local/              # Encrypted DataStore, Tink CryptoManager
├── feature/
│   ├── auth/               # Login, signup, email verification UI
│   ├── home/               # Home dashboard (scaffold)
│   ├── search/             # Flight search (scaffold)
│   ├── travel/             # Travel planning (scaffold)
│   ├── flight/             # Flight details (scaffold)
│   └── profile/            # User profile (scaffold)
├── service/
│   ├── domain/             # Auth use cases, models, repository contracts
│   └── data/               # Firebase Auth, local/remote data sources
└── gradle/libs.versions.toml
```

---

## Tech stack

| Category | Technology |
|---|---|
| Language | Kotlin 2.4.10 |
| UI | Jetpack Compose, Material 3 |
| Architecture | Clean Architecture, MVI-style UDF, multi-module |
| DI | Hilt 2.60.1, KSP 2.3.10 |
| Navigation | Navigation 3 (type-safe routes) |
| Auth | Firebase Auth, Credential Manager, Google & GitHub OAuth |
| Networking | Ktor 3.5.x (OkHttp engine), kotlinx-serialization |
| Image loading | Coil 3.5.x |
| Local storage | DataStore 1.2.1, Google Tink 1.23.0 |
| Debug tooling | Chucker 4.3.1 |
| Build | AGP 9.2.1, Gradle 9.5.0, Version Catalog |
| Min / Target SDK | 24 / 37 |

Dependency versions are centralized in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

---

## Skills demonstrated

These map directly to common **EU Android job requirements**:

| Skill | Evidence in this repo |
|---|---|
| **Kotlin** | Coroutines, Flow, sealed interfaces, extension functions |
| **Jetpack Compose** | Declarative UI, state hoisting, lifecycle-aware collection |
| **Clean Architecture** | Domain use cases, repository pattern, module separation |
| **Dependency Injection** | Hilt modules for network, local storage, auth |
| **Security awareness** | Encrypted token storage, OAuth, deep link validation |
| **Modern Gradle** | Version catalog, Kotlin DSL, multi-module builds |
| **Product thinking** | Auth gate, verification UX, error messaging, loading states |

---

## Getting started

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 17+
- Android SDK 37

### Firebase setup (required for auth)

1. Create a Firebase project and add an Android app with package `tech.nullexdev.atlasfly`
2. Download `google-services.json` into `app/`
3. Enable **Email/Password**, **Google**, and **GitHub** sign-in in Firebase Console
4. Configure OAuth redirect URIs for GitHub if using GitHub login

> The repo includes a placeholder `google-services.json`. Replace it with your own for local development.

### Build & run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Run tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Project conventions

- **Package scheme:** `tech.nullexdev.atlasfly.{layer}.{module}`
- **Feature modules** own their screens, ViewModels, and UI components
- **Service modules** encapsulate backend integration (Firebase Auth today)
- **Core modules** provide shared infrastructure consumed by features
- **Use cases** expose single-responsibility domain operations
- **Version catalog:** bump dependencies in `gradle/libs.versions.toml`, reference via `libs.*`

---

## About the author

**ALI Mohammadzadeh** — Android Developer

Building AtlasFly to showcase how I approach real-world Android development: modular architecture, thoughtful UX, and code that teams can maintain and extend.

| | |
|---|---|
| **GitHub** | [Alimmzdev](https://github.com/Alimmzdev) |
| **Location** | Open to EU opportunities (remote / relocation) |
| **Focus** | Kotlin · Jetpack Compose · Clean Architecture · Product-quality UX |

> **Recruiters:** A video walkthrough and screenshots significantly improve first impressions — see [`docs/media/README.md`](docs/media/README.md) for the asset checklist.

---

## License

```
MIT License

Copyright (c) 2026 ALI Mohammadzadeh
```

See [LICENSE](LICENSE) for full text.
