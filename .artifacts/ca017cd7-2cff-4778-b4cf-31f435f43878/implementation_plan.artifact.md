# Fix Supabase Storage Dependency Resolution

The project is failing to sync because of a typo in the Group ID for the Supabase Storage dependency.

## Proposed Changes

### [app]

#### [MODIFY] [build.gradle.kts](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/build.gradle.kts)
- Correct the Group ID for `storage-kt` from `io.github.jan.supabase` to `io.github.jan-tennert.supabase`.

```diff
-    implementation("io.github.jan.supabase:storage-kt:2.5.0")
+    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.0")
```

## Verification Plan

### Automated Tests
- Run Gradle Sync to verify the dependency resolves correctly.
