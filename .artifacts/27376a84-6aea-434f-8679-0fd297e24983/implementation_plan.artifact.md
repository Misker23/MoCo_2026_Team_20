# Fix Supabase GoTrue dependency resolution error

The project is failing to sync because it tries to resolve `io.github.jan-tennert.supabase:gotrue-kt:3.6.0`. In version 3.x of the Supabase Kotlin SDK, the `gotrue-kt` module has been renamed to `auth-kt`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/gradle/libs.versions.toml)
- Rename `gotrue-kt` alias to `auth-kt`.
- Update the module path from `io.github.jan-tennert.supabase:gotrue-kt` to `io.github.jan-tennert.supabase:auth-kt`.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/build.gradle.kts)
- Update the dependency from `libs.gotrue.kt` to `libs.auth.kt`.

## Verification Plan

### Automated Tests
- Run Gradle sync to verify the dependency resolves correctly.
- `gradlew :app:assembleDebug` to ensure the project builds.

### Manual Verification
- Verify that the Supabase client can still be initialized (though no code changes are strictly required for sync, adding `install(Auth)` might be needed later by the user).
