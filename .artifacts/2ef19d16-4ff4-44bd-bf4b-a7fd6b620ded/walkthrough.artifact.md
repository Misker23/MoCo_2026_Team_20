# Fixed Room Compilation Error and Dependency Cleanup

I have resolved the `Unresolved reference 'mutableListOf'` error and standardized the project dependencies.

## Changes Made

### Room Compilation Fix
- Added `room.generateKotlin = "false"` to the KSP configuration in `app/build.gradle.kts`. This forces Room to generate Java code instead of Kotlin code, which bypasses the classpath/import issue encountered with the current Kotlin/KSP version.

### Dependency Standardization
- Migrated hardcoded dependencies in `app/build.gradle.kts` to the Version Catalog (`libs.versions.toml`).
- Reverted Supabase and Ktor to their previous stable versions (2.5.0 and 2.3.12 respectively) to maintain compatibility with the project's Kotlin version and existing code.
- Unified Hilt versions to `2.60.1`.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:assembleDebug`: **SUCCESS**
- Verified that Room-generated code now compiles correctly.
