# AtlasFly

An Android flight and travel companion app built with Kotlin and Jetpack Compose.

**Package:** `tech.nullexdev.atlasfly`
**License:** MIT
**Author:** ALI Mohammadzadeh

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin 2.4.10 |
| UI | Jetpack Compose, Material 3 |
| Build | AGP 9.2.1, Gradle 9.5.0, Kotlin DSL, Version Catalog |
| DI | Hilt 2.60.1, KSP 2.3.10 |
| Networking | Ktor 3.5.1 (OkHttp engine), kotlinx-serialization |
| Image loading | Coil 3.5.0 (Ktor network fetcher) |
| Local storage | DataStore 1.2.1, Google Tink 1.23.0 (encrypted auth tokens) |
| Debug tooling | Chucker 4.3.1 (network inspector) |
| Min SDK | 24 (Android 7.0) |
| Target / Compile SDK | 37 |

## Architecture

AtlasFly uses a **multi-module architecture** organized into four layers: `app`, `core`, `feature`, and `service`.

```
AtlasFly/
├── app/                          # Application entry point (Compose, Hilt, Coil)
├── core/                         # Shared infrastructure
│   ├── design-system/            # Reusable UI components & theme (JVM scaffold)
│   ├── presentation/             # Base UI utilities & common composables (JVM scaffold)
│   ├── data/                     # Data layer abstractions (JVM scaffold)
│   ├── domain/                   # Domain layer — use cases, models (JVM scaffold)
│   ├── network/                  # Ktor HTTP client, Hilt DI, Chucker (Android)
│   └── local/                    # Encrypted DataStore, Tink crypto (Android)
├── feature/                      # Feature modules (self-contained UI)
│   ├── auth/                     # Authentication flow (JVM scaffold)
│   ├── home/
│   ├── search/
│   ├── travel/
│   ├── flight/
│   └── profile/
├── service/                      # Backend / service integrations
│   ├── data/                     # Service-layer data implementations (JVM scaffold)
│   └── domain/                   # Service-layer domain logic (JVM scaffold)
├── gradle/
│   └── libs.versions.toml        # Centralized version catalog
└── build.gradle.kts
```

### Module Responsibilities

| Module | Type | Purpose |
|---|---|---|
| `:app` | Android application | App config, Compose theme, splash screen, Hilt entry point, Coil setup |
| `:core:design-system` | JVM library | Design tokens, colors, typography, reusable components |
| `:core:presentation` | JVM library | Base ViewModel, common UI patterns, extensions |
| `:core:data` | JVM library | Repository implementations, data source coordination |
| `:core:domain` | JVM library | Use cases, domain models, repository interfaces |
| `:core:network` | Android library | Ktor HTTP client (OkHttp engine), JSON serialization, Hilt `NetworkModule`, Chucker |
| `:core:local` | Android library | Encrypted DataStore for auth tokens, Tink `CryptoManager`, Hilt `LocalModule` |
| `:service:data` | JVM library | Service-layer data implementations |
| `:service:domain` | JVM library | Service-layer domain models and business logic |
| `:feature:auth` | JVM library | Login, registration, and session management |
| `:feature:home` | JVM library | Home / dashboard screen |
| `:feature:search` | JVM library | Flight search functionality |
| `:feature:travel` | JVM library | Travel planning & itinerary |
| `:feature:flight` | JVM library | Flight details & booking |
| `:feature:profile` | JVM library | User profile & settings |

> **Note:** Most feature and core modules are currently JVM library scaffolds. `:app`, `:core:network`, and `:core:local` contain active Android implementations.

## Dependencies

All dependency versions are managed in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

### Build & Language

| Library | Version |
|---|---|
| Android Gradle Plugin | 9.2.1 |
| Kotlin | 2.4.10 |
| Gradle | 9.5.0 |
| KSP | 2.3.10 |

### AndroidX & Compose

| Library | Version |
|---|---|
| Core KTX | 1.19.0 |
| Core Splashscreen | 1.2.0 |
| Lifecycle Runtime KTX | 2.11.0 |
| Activity Compose | 1.13.0 |
| Compose BOM | 2026.06.01 |
| Annotation Experimental | 1.4.1 |

### Dependency Injection

| Library | Version |
|---|---|
| Hilt | 2.60.1 |

### Networking

| Library | Version |
|---|---|
| Ktor Client | 3.5.1 |
| kotlinx-serialization-json | 1.11.0 |
| Chucker (debug) | 4.3.1 |

### Image Loading

| Library | Version |
|---|---|
| Coil 3 | 3.5.0 |

### Local Storage

| Library | Version |
|---|---|
| DataStore | 1.2.1 |
| Google Tink (Android) | 1.23.0 |
| Protobuf JavaLite | 4.35.1 |

### Navigation (catalog — not yet wired)

| Library | Version |
|---|---|
| Navigation 3 Runtime | 1.1.4 |
| Navigation 3 UI | 1.1.4 |

### Testing

| Library | Version |
|---|---|
| JUnit | 4.13.2 |
| AndroidX JUnit | 1.3.0 |
| Espresso Core | 3.7.0 |

### Module Dependency Graph

```
:app
 ├── :core:network          (Ktor, Hilt, Chucker)
 ├── Hilt, Coil, Compose
 └── (feature modules — to be wired)

:core:local
 ├── DataStore, Tink, kotlinx-serialization
 └── Hilt

:core:network
 ├── Ktor (core, okhttp, content-negotiation, logging, serialization)
 └── Hilt
```

## Getting Started

### Prerequisites

- Android Studio Quail (2026.1) or later
- JDK 11+ (JDK 17 required for `:core:local`)

### Build & Run

```bash
./gradlew assembleDebug
```

Install on a connected device or emulator:

```bash
./gradlew installDebug
```

## Project Structure Conventions

- **Package scheme:** `tech.nullexdev.atlasfly.{layer}.{module}`
- **Each feature module** is self-contained — it declares its own navigation, screen composables, and ViewModels
- **Core modules** provide shared logic consumed by feature and service modules
- **Service modules** handle external API / backend integration independently
- **Version catalog:** add or bump dependencies in `gradle/libs.versions.toml`, then reference them via `libs.*` aliases in module `build.gradle.kts` files

## License

```
MIT License

Copyright (c) 2026 ALI Mohammadzadeh
```
