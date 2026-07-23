# Fix Unresolved Reference 'MapMode' and Reconcile MapViewModel

The build error `Unresolved reference 'MapMode'` is caused by the missing `MapMode` enum class. Additionally, `MapViewModel` and `MarkerDto` are missing several properties and methods that are currently being used in `HomeScreenCompose.kt` and `MapScreen.kt`.

## Proposed Changes

### [Component] Data Models

#### [MODIFY] [DatabaseModels.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/data_models/DatabaseModels.kt)
- Add `image_url: String? = null` to `MarkerDto`.

### [Component] Map Screen Composables

#### [MODIFY] [MapViewModel.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/mapScreenComposables/MapViewModel.kt)
- Define `enum class MapMode { DEFAULT, PLACING_MARKER, CONFIRMING }`.
- Add missing state properties:
    - `userPosition: Position`
    - `currentMode: MapMode`
    - `selectedMarker: MarkerDto?`
    - `markerList: List<MarkerDto>`
    - `temporaryPosition: Position?`
- Implement missing methods:
    - `updateUserPosition(position: Position)`
    - `fetchMarkers()` (as an alias or replacement for `loadMarkersForMap()`)
    - `handleMapClick(pos: Position)`
    - `startPlacingMode()`
    - `cancelPlacing()`
    - `confirmMarker(description: String)`
    - `selectMarker(markerDto: MarkerDto)`
    - `updateMarkerWithImage(...)`
    - `deleteMarker(id: String)`

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure all references are resolved and the project builds successfully.

### Manual Verification
- Deploy the app and verify that the map functions (placing markers, selecting markers, etc.) work as expected.
