# Fix "Unresolved reference 'viewmodel'" in Gradle Sync

The project is failing to sync because `libs.androidx.lifecycle.viewmodel.compose` is used in `app/build.gradle.kts` but is not defined in the Version Catalog (`gradle/libs.versions.toml`). Additionally, other dependencies used in the build script are also missing from the catalog.

## Proposed Changes

### [gradle/libs.versions.toml](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/gradle/libs.versions.toml)

#### [MODIFY] [libs.versions.toml](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/gradle/libs.versions.toml)
- Add missing versions for Navigation and CameraX.
- Add library definitions for:
    - `androidx-lifecycle-viewmodel-compose`
    - `androidx-navigation-compose`
    - `androidx-camera-core`
    - `androidx-camera-camera2`
    - `androidx-camera-lifecycle`
    - `androidx-camera-view`

### [app/build.gradle.kts](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/build.gradle.kts)

#### [MODIFY] [build.gradle.kts](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/build.gradle.kts)
- Remove the local `camerax_version` variable as it will be managed by the Version Catalog.

## Verification Plan

### Automated Tests
- Run Gradle Sync to ensure the "Unresolved reference 'viewmodel'" error is resolved.
- Run `./gradlew assembleDebug` to verify the build completes successfully with the new dependencies.
