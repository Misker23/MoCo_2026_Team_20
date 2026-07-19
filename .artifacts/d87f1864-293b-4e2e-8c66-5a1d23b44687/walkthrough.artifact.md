# Build Error Fix: Missing `markerDto` Parameter

I have fixed the build errors related to the missing `markerDto` parameter in several Composables and addressed related issues in the Supabase integration.

## Changes

### UI Components

#### [SmallMarkerCompose.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/HomeScreenComposables/SmallMarkerCompose.kt)
- Updated `SmallMarkerPreview` to pass a dummy `MarkerDto`.

#### [SneakPeekMarkerCompose.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/HomeScreenComposables/SneakPeekMarkerCompose.kt)
- Updated `SneakPeekMarkerPreview` to pass a dummy `MarkerDto`.
- Fixed a typo: changed `isNullEmpty()` to `isNullOrEmpty()`.

#### [MapScreen.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/MapScreenComposeables/MapScreen.kt)
- Updated the temporary marker instantiation to pass a `MarkerDto` created from the temporary position.
- Added the missing `MarkerDto` import.

### Data & Logic

#### [MapViewModel.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/MapScreenComposeables/MapViewModel.kt)
- Removed a duplicate declaration of `markerList`.
- Removed an unused/unresolved import for `MarkerModel`.
- Fixed the Supabase `storage` integration by adding missing imports and correcting the `upload` method syntax.

#### [SupabaseClient.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/SupabaseClient.kt)
- Installed the `Storage` plugin in the Supabase client configuration.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:compileDebugKotlin` which now finishes successfully.

```
{
  "status": "Build finished successfully."
}
```
