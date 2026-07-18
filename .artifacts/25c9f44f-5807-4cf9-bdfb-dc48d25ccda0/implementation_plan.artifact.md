# Fix Supabase Dependency Resolution Error

The user is experiencing a Gradle sync error: `Failed to resolve: io.github.jan-tennert.supabase:gotrue-kt:3.6.0`.
This is because in Supabase-kt version 3.x, the `gotrue-kt` module was renamed to `auth-kt`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/gradle/libs.versions.toml)
- Rename the `gotrue-kt` alias to `auth-kt`.
- Update the module path from `io.github.jan-tennert.supabase:gotrue-kt` to `io.github.jan-tennert.supabase:auth-kt`.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/build.gradle.kts) (Module: :app)
- Update the dependency reference from `libs.gotrue.kt` to `libs.auth.kt`.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` or trigger a Gradle Sync to ensure dependencies resolve correctly.

### Manual Verification
- Verify that the Gradle Sync finishes successfully in Android Studio.
