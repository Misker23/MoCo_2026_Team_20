# Fix Unresolved Reference 'FriendsScreen'

The project fails to build because `MainActivity.kt` (or the user's intent) references `FriendsScreen`, but the defined composable is named `FriendsScreenCompose`. Additionally, the call to this composable in `MainActivity.kt` has incorrect parameters.

## Proposed Changes

### [Component] Friends Screen

#### [MODIFY] [FriendsScreenCompose.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/FriendsScreenComposables/FriendsScreenCompose.kt)
- Rename `FriendsScreenCompose` function to `FriendsScreen` to match the project's naming convention (like `HomeScreen`) and resolve the reported error.
- Add a default value for `bottomPadding: Dp = 0.dp` to allow calling it without padding (e.g., from `MainActivity`).
- Rename `onDismiss` parameter to `onClose` for better clarity across different usage contexts (Popup vs Navigation), or keep it and align `MainActivity`. I'll go with `onClose` and update usages.

### [Component] Home Screen

#### [MODIFY] [HomeScreenCompose.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/HomeScreenComposables/HomeScreenCompose.kt)
- Update import and usage from `FriendsScreenCompose` to `FriendsScreen`.
- Update parameter name from `onDismiss` to `onClose`.

### [Component] Main Activity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/MainActivity.kt)
- Update import from `FriendsScreenCompose` to `FriendsScreen`.
- Update call in `NavHost` to use `FriendsScreen`.
- Fix parameter mismatch: Use `onClose` (or whatever name is chosen) and remove/handle `bottomPadding`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference error is gone and the project builds successfully.

### Manual Verification
- Deploy the app and navigate to the Friends screen from the Home screen to ensure it displays correctly.
