# Walkthrough - Fixing Nullability Issues in Map Components

I have fixed several "Argument type mismatch" errors where nullable types (`Double?`, `String?`) were being passed to functions or constructors expecting non-nullable types (`Double`, `String`).

## Changes Made

### [HomeScreenCompose.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/homeScreenComposables/HomeScreenCompose.kt)
- Added null checks for `markerLat` and `markerLon` in the `onPoiSelected` callback before animating the camera to the marker position.

### [MapScreen.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/mapScreenComposables/MapScreen.kt)
- Added null checks for `markerDto.lat` and `markerDto.lon` inside the marker rendering loop to ensure only valid markers are displayed and processed for screen projection.

### [MapViewModel.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/mapScreenComposables/MapViewModel.kt)
- Updated `loadMarkersForMap` to use `mapNotNull` when creating `MapMarkerUiState`. This ensures that any markers with missing `creator_id`, `lat`, or `lon` are filtered out of the UI state, preventing runtime crashes or further type mismatches.

## Verification

### Automated Tests
- Ran `./gradlew :app:compileDebugKotlin` and confirmed the build now finishes successfully.

### Manual Verification
- The changes ensure that the app won't attempt to process markers with incomplete GPS data, which was the root cause of the type mismatch errors.
