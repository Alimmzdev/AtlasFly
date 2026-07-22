# AtlasFly

An Android flight and travel companion app built with Kotlin and Jetpack Compose.

**Package:** `tech.nullexdev.atlasfly`
**License:** MIT
**Author:** ALI Mohammadzadeh

## Tech Stack

- **Language:** Kotlin 2.4.10
- **UI:** Jetpack Compose with Material3
- **Build:** AGP 9.2.1, Gradle Kotlin DSL, Version Catalog
- **Min SDK:** 24 (Android 7.0)
- **Target / Compile SDK:** 37

## Architecture

AtlasFly uses a **multi-module architecture** organized into three layers: `app`, `core`, `feature`, and `service`.

```
AtlasFly/
├── app/                          # Application entry point
├── core/                         # Shared infrastructure
│   ├── design-system/            # Reusable UI components & theme
│   ├── presentation/             # Base UI utilities & common composables
│   ├── data/                     # Data layer abstractions
│   ├── domain/                   # Domain layer (use cases, models)
│   ├── network/                  # API clients & network config
│   └── local/                    # Local storage (database, prefs)
├── feature/                      # Feature modules (self-contained UI)
│   ├── home/
│   ├── search/
│   ├── travel/
│   ├── flight/
│   └── profile/
├── service/                      # Backend / service integrations
│   ├── data/
│   └── domain/
├── gradle/
│   └── libs.versions.toml        # Version catalog
└── build.gradle.kts
```

### Module Responsibilities

| Module | Purpose |
|---|---|
| `:app` | App-level config, theme setup, navigation host, splash screen |
| `:core:design-system` | Design tokens, colors, typography, reusable components |
| `:core:presentation` | Base ViewModel, common UI patterns, extensions |
| `:core:data` | Repository implementations, data source coordination |
| `:core:domain` | Use cases, domain models, repository interfaces |
| `:core:network` | Retrofit/Ktor setup, API service definitions |
| `:core:local` | Room database, DAOs, DataStore preferences |
| `:service:data` | Service-layer data implementations |
| `:service:domain` | Service-layer domain models and business logic |
| `:feature:home` | Home / dashboard screen |
| `:feature:search` | Flight search functionality |
| `:feature:travel` | Travel planning & itinerary |
| `:feature:flight` | Flight details & booking |
| `:feature:profile` | User profile & settings |

## Getting Started

### Prerequisites

- Android Studio Quail (2026.1) or later
- JDK 11+

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

## License

```
MIT License

Copyright (c) 2026 ALI Mohammadzadeh
```
