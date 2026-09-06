# Brick Cost Calculator

Android app to calculate brick factory manufacturing costs.

Enter your production batch's manufacturing costs (komiche, cement, labor,
water, other) and how many 10", 15", and 20" bricks the batch produced. The
app allocates cost across the three sizes by relative material usage, then
shows cost per brick, selling price, profit per brick, and total profit.

## How it's built

This is a native Android app (Kotlin) that hosts the calculator UI in a
full-screen `WebView`, loading a self-contained HTML/CSS/JS page bundled in
`app/src/main/assets/index.html`. Saved batches are stored on-device via
`localStorage` — no backend or internet connection required.

## Opening the project

1. Install [Android Studio](https://developer.android.com/studio) (Hedgehog
   or newer).
2. `File > Open` and select this project's root folder.
3. Let Gradle sync (Android Studio will download the Android SDK components
   and the Gradle 8.7 distribution automatically on first sync).
4. Press **Run** (▶) with an emulator or a physical device connected.

## Building from the command line

Once Android Studio has installed the SDK (or `ANDROID_HOME` points at one):

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Project structure

```
app/src/main/java/com/kibrom/brickcost/MainActivity.kt  - WebView host activity
app/src/main/assets/index.html                          - calculator UI & logic
app/src/main/res/                                        - app icon, theme, strings
```
