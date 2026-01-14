# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep all data classes
-keep class com.timehorizons.wallpaper.data.** { *; }

# Keep wallpaper service
-keep class com.timehorizons.wallpaper.service.LifeCalendarService { *; }

# Keep broadcast receiver
-keep class com.timehorizons.wallpaper.service.MidnightReceiver { *; }

# Keep module interface and implementations
-keep interface com.timehorizons.wallpaper.modules.CountdownModule { *; }
-keep class * implements com.timehorizons.wallpaper.modules.CountdownModule { *; }

# Keep LocalDate and related java.time classes
-keep class java.time.** { *; }
-dontwarn java.time.**
