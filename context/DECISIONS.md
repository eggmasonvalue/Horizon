# Architecture Decisions

Curated record of durable, non-obvious project-level architectural decisions and tradeoffs.

## 2026-08-16 — Paired Light/Dark Theme Model and Deterministic Daily Rotation

Context: The wallpaper color system originally consisted of an ad-hoc collection of disconnected color scheme presets without parity between light and dark modes. Additionally, rotating themes daily needed to work seamlessly across midnight boundaries, device reboots, and configuration changes without storing volatile scheduling state.

Decision: Refactor color schemes into `WallpaperTheme` entities with paired, curated `lightScheme` and `darkScheme` definitions that automatically resolve based on the system's night mode (`Configuration.UI_MODE_NIGHT_YES`). For daily rotation, compute the active theme deterministically via date epoch modulo (`epochDay % rotatableThemes.size`) upon wallpaper initialization and midnight boundary broadcasts.

Tradeoff: Themes must provide balanced light and dark variants rather than standalone arbitrary palettes, but eliminates complex rotation state management and ensures theme consistency across day/night system toggles.

Status: active

## 2026-04-20 — Health Connect Sync Ownership in DayCounterService

Context: Health Connect metrics (Steps, Calories, Distance, Sleep) are exclusive to the Micro (Day Counter) mode. Centralizing health sync in `BaseWallpaperService` bloated the shared base class and introduced unnecessary Health Connect queries for Macro (Life Calendar) mode.

Decision: Isolate Health Connect cache hydration, throttled visible-window refreshes, and midnight boundary syncs directly inside `DayCounterService`, keeping `BaseWallpaperService` completely agnostic of health data.

Tradeoff: `DayCounterService` manages its own coroutine lifecycle and throttled polling loop, but keeps `BaseWallpaperService` and Macro mode decoupled and lightweight.

Status: active

## 2026-04-20 — Health Cache Persistence with Validation Metadata

Context: Querying Health Connect APIs on every frame or lock screen unlock degrades battery life and introduces latency. Persisting raw metrics without cache metadata caused stale or incorrect values to render when users modified their tracked metric or date window.

Decision: Store health aggregates in SharedPreferences via `HealthCacheManager` accompanied by cache metadata (metric type, covered date range, last refresh timestamp), and validate this metadata before hydrating cached metrics.

Tradeoff: Requires invalidation and full backfill logic when metadata mismatches, but avoids redundant background API calls and prevents mismatched metrics from rendering.

Status: active

## 2026-03-13 — Strategy Pattern and Pre-allocation in CanvasRenderer

Context: Live wallpaper rendering runs continuously at up to 60fps on Android Canvas. Instantiating drawing primitives, objects, or branching dynamically for multiple shapes (Circle, Rounded Square, Rhombus) within `onDraw` causes frequent GC pauses and battery consumption.

Decision: Encapsulate shape drawing into dedicated `ShapeDrawer` strategy implementations and pre-calculate all layout coordinates, paths, and paint objects during `onSurfaceChanged` or configuration updates.

Tradeoff: Introduces additional strategy classes compared to a single conditional drawing function, but achieves allocation-free frame rendering during pulse animations.

Status: active

## 2026-03-01 — Direct Font Bundling for Geist Typeface

Context: Perspective - Live relies on precise geometric and numeric alignment for grid overlays and lockscreen visuals across diverse Android OEMs with different system default fonts.

Decision: Bundle the Geist font family directly into core Android resources (`res/font/geist*`) rather than relying on system fallback fonts or Google Fonts downloadable font providers.

Tradeoff: Increases APK distribution size by the size of the font assets, but guarantees consistent typography across all Android OEM skins without network latency or font download failures.

Status: active
