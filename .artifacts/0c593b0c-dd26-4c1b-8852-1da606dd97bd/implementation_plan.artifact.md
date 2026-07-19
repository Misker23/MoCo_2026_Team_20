# Implementation Plan - Fix Supabase Dependency and Versioning

The project is failing to sync because of an incorrect artifact identifier for the Supabase Storage module (`io.github.jan.supabase:storage-kt`). Additionally, the project has inconsistent Supabase dependency declarations between `build.gradle.kts` and `libs.versions.toml`.

## Proposed Changes

### 1. Build Configuration [gradle]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/gradle/libs.versions.toml)
- Update Supabase GoTrue to Auth module (renamed in 3.x).
- Add Storage module.
- Ensure all Supabase modules use the `io.github.jan-tennert.supabase` group ID.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/build.gradle.kts)
- Replace hardcoded Supabase and Ktor strings with version catalog aliases (`libs.*`).
- Use consistent 3.x versions for all Supabase modules as defined in the version catalog.

### 2. Supabase Integration [app]

#### [MODIFY] [SupabaseClient.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/SupabaseClient.kt)
- Update imports from `gotrue` to `auth`.
- Install the `Storage` plugin to enable storage functionality used in `MapViewModel`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/MainActivity.kt)
- Update imports from `gotrue` to `auth`.

#### [MODIFY] [MapViewModel.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/MapScreenComposeables/MapViewModel.kt)
- Update imports from `gotrue` to `auth`.

## Verification Plan

### Automated Tests
- Run Gradle Sync to ensure all dependencies are resolved.
- Build the project to verify that code changes (imports) are correct.

### Manual Verification
- Verify that the app launches without errors.
- (Optional) Test Supabase Auth and Storage functionality if a local backend is running.
