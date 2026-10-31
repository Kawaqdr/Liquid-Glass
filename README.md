# Liquid Glass Launcher

An Android home-screen replacement inspired by iOS Liquid Glass, built with Kotlin and Jetpack Compose.

## Current foundation
- Android HOME intent integration (select it as the default launcher in system settings).
- Installed launcher-app discovery and app launching.
- Search within the app drawer.
- Configurable vertical scrolling / horizontal paged app drawer.
- Reusable translucent glass surface styling.
- Initial home, dock, and settings UI.

## Build
Open this project in Android Studio with JDK 17 and Android SDK 35, then sync and run the `:app` configuration.

## Roadmap
- Persist home layout, drawer mode, hidden apps, and folders.
- Drag-and-drop home-screen placement and editable folders.
- Android AppWidgetHost integration.
- Icon-pack resource mapping.
- Notification badges (opt-in Notification Listener access).
- Wallpaper picker and adaptive glass tint.
- Gesture configuration and backup/restore.

Some Android capabilities require explicit system approval. The app does not bypass Android permissions.
