# Fix Room Unresolved Reference Error and Cleanup Dependencies

The build error `Unresolved reference 'mutableListOf'` in the generated `AppDatabase_Impl.kt` is caused by an issue in Room's Kotlin code generation (KCG) when using KSP with certain Kotlin versions (in this case 2.2.10). Disabling Kotlin code generation in Room will force it to generate Java code, which is more stable and resolves this specific classpath/import issue.

Additionally, several dependencies in `app/build.gradle.kts` are hardcoded with older versions, conflicting with the versions defined in `libs.versions.toml`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/terme/AndroidStudioProjects/MoCo_2026_Team_20/app/build.gradle.kts)
- Add KSP argument to disable Room's Kotlin code generation: `room.generateKotlin = "false"`.
- Replace hardcoded Supabase and Ktor dependencies with their `libs` counterparts to ensure consistency.
- Standardize version usage for WorkManager and Hilt.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the project builds successfully and the generated Room code compiles.

### Manual Verification
- Inspect the generated `AppDatabase_Impl` to ensure it is now a Java file (or compiles correctly as Kotlin if the classpath issue is resolved by dependency cleanup).
