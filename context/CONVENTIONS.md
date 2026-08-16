# Conventions

Terse imperative rules for writing and verifying code in Perspective - Live.

## Verification Commands

- Build debug APK: `./gradlew assembleDebug`
- Run unit tests: `./gradlew test`
- Run Kotlin linter: `./gradlew detekt`
- Run Markdown linter: `npx markdownlint-cli2 "**/*.md"`

## Code Style & Formatting

- Write in Kotlin following standard Kotlin conventions.
- Format code with 4 spaces for indentation.
- Keep line lengths under 120 characters where possible.
- Avoid wildcard imports except for `java.util.*` where configured.
- Avoid magic numbers by extracting named constants.

## Architecture & Layering

- Maintain strict layer boundaries: UI in `settings`, calculation logic in `modules`, drawing in `rendering`, persistence and data models in `data`, wallpaper runtime in `service`, general helpers in `utils`.
- Use MVVM for UI components (`SettingsViewModel`, LiveData).
- Keep calculation modules free of Android UI/View dependencies.
- Use dependency injection through ViewModel factories (`SettingsViewModelFactory`).

## Compatibility & Migrations

- Do not maintain backward compatibility considerations, legacy aliases, deprecated fallbacks, or migration layers anywhere; target current schema and active state directly.

## State & Persistence

- Keep `UserPreferences` and data models immutable.
- Use `copy()` to generate updated instances.
- Use `PreferencesManager` as the persistent source of truth backed by `SharedPreferences`.
- Persist Health Connect cache entries with metric type, covered date range, and refresh timestamp metadata via `HealthCacheManager`.
- Validate cache metadata before hydrating cached metrics in wallpaper services.

## Live Wallpaper Services

- Extend `BaseWallpaperService` for common surface, visibility, and color-change handling.
- Override `onSurfaceDestroyed` to stop animators, coroutines, and scheduled callbacks.
- Override `onComputeColors` on API 27+ and trigger `notifyColorsChanged` upon palette changes.
- Isolate feature-specific background jobs (such as Health Connect polling) to the owning service (`DayCounterService`).
- Throttle Health Connect refreshes while the wallpaper is visible.

## Graphics & Rendering

- Use `CanvasRenderer` with `ShapeDrawer` strategies for primitive drawing (Circle, Rounded Square, Rhombus).
- Pre-allocate Paint, Path, and geometry objects in layout calculation passes rather than inside `onDraw`.
- Drive breathing pulse animations through `PulseAnimator`.

## UI & Design

- Use Material Design 3 components.
- Use the bundled `Geist` font family for all text rendering.
- Keep XML layout hierarchies flat.
