# Fix Kotlin Inference Error in FriendsScreenCompose.kt

The user is encountering a "Cannot infer type for this parameter" error at line 103 of `FriendsScreenCompose.kt`. Research shows that the `FriendDto` and `ProfileDto` models, which are used by `FriendsViewModel` and the UI, are missing from the project. This likely causes the Kotlin compiler to fail type inference for parameters in `items` blocks.

Additionally, there is a package name mismatch and inconsistent casing in `FriendsViewModel.kt` and `FriendsScreenCompose.kt`.

## Proposed Changes

### [Component: Data Models]

#### [MODIFY] [DatabaseModels.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/data_models/DatabaseModels.kt)
- Add `ProfileDto` and `FriendDto` data classes to support the friends feature.
- Include a `displayName` extension property or member to match usage in the UI.

### [Component: Friends Feature]

#### [MODIFY] [FriendsViewModel.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/friendsScreenComposables/FriendsViewModel.kt)
- Correct the package name to `com.example.ap2.friendsScreenComposables` (lowercase) to match the directory structure and standard conventions.

#### [MODIFY] [FriendsScreenCompose.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/friendsScreenComposables/FriendsScreenCompose.kt)
- Update the import for `FriendsViewModel` to match the corrected package.
- Explicitly specify the type for the `friend` parameter in `items` to ensure the inference error is resolved.

## Verification Plan

### Automated Tests
- Run `:app:compileDebugKotlin` to verify the build error is gone.

### Manual Verification
- Check that the UI correctly displays the friends list and the "online/offline" status as intended.
