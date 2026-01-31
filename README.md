# ResQCampus

Android app (Kotlin + Jetpack Compose) with fall detection, live GPS, and emergency alert.

## Features

- **Fall detection** – Accelerometer-based (SensorManager); status shows SAFE / FALL DETECTED
- **Live GPS** – Latitude and longitude via FusedLocationProviderClient
- **Simulate fall** – Button to trigger fall-detected state
- **Emergency alert** – Button to trigger emergency (toast; extend to backend/calls as needed)

## Permissions (runtime)

- `ACCESS_FINE_LOCATION`
- `ACTIVITY_RECOGNITION`

## Project structure (where each file goes)

```
ResQCampus/
├── settings.gradle.kts          # Project name & included modules (:app)
├── build.gradle.kts             # Project-level plugins (Android + Kotlin)
├── gradle.properties            # JVM args, AndroidX, etc.
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts         # App SDK, dependencies (Compose, Play Services Location)
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml  # Permissions, application, MainActivity
│       ├── java/com/resqcampus/
│       │   ├── MainActivity.kt           # Compose UI, sensors, location, buttons
│       │   └── ui/theme/
│       │       ├── Theme.kt
│       │       └── Type.kt
│       └── res/
│           ├── values/
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── drawable/
│               └── ic_launcher_foreground.xml
```

## Build APK

1. **Android Studio**  
   Open the `ResQCampus` folder, wait for sync, then **Build → Build Bundle(s) / APK(s) → Build APK(s)**.  
   Output: `app/build/outputs/apk/debug/app-debug.apk`

2. **Command line**  
   From project root, run:
   - Windows: `gradlew.bat assembleDebug`
   - macOS/Linux: `./gradlew assembleDebug`  
   If `gradlew` is missing, generate it with Android Studio (File → Sync Project with Gradle) or install Gradle and run `gradle wrapper`.

Min SDK 26, target/compile 34. Kotlin DSL: `settings.gradle.kts` and `build.gradle.kts` (equivalent to `settings.gradle` and `build.gradle`).
