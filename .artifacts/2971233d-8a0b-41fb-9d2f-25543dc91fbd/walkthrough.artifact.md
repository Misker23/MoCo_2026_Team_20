# Fix for FriendsScreenCompose Build Error

I have resolved the "Cannot infer type for this parameter" error in `FriendsScreenCompose.kt` and addressed related issues with missing data models and incorrect package names.

## Changes Made

### Data Models
- **[DatabaseModels.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/data_models/DatabaseModels.kt)**: Added `ProfileDto` and `FriendDto` classes.
    - `FriendDto` includes a `displayName` helper property to retrieve the username from the nested profile.

### Friends Feature
- **[FriendsViewModel.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/friendsScreenComposables/FriendsViewModel.kt)**: Corrected the package name from `com.example.ap2.FriendsScreenComposables` to `com.example.ap2.friendsScreenComposables` (lowercase) to ensure consistency and correct internal resolution.
- **[FriendsScreenCompose.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/friendsScreenComposables/FriendsScreenCompose.kt)**:
    - Explicitly specified `friend: FriendDto` in the `items` block to fix the type inference error.
    - Updated imports to use the corrected `FriendsViewModel` package and the new `FriendDto` model.

## Build Status

> [!IMPORTANT]
> The specific error in `FriendsScreenCompose.kt` is now resolved. However, the project still fails to build due to numerous **unrelated** errors in `HomeScreenCompose.kt` and `MapScreen.kt`. These files reference several missing components and properties in `MapViewModel`, such as `MapMode`, `userPosition`, and `fetchMarkers`.

I recommend reviewing the `MapViewModel.kt` implementation to ensure it contains all the necessary logic required by the Map and Home screens.
