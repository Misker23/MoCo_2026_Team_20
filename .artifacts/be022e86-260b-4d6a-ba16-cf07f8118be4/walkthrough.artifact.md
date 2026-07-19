# Walkthrough - Fix Dp/Int Type Mismatch in MapScreen

I have resolved the build error in `MapScreen.kt` where `Int` and `Dp` types were incorrectly mixed.

## Changes Made

### MapScreen Component

#### [MODIFY] [MapScreen.kt](file:///C:/Users/terme/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/MapScreenComposeables/MapScreen.kt)
- **Import Cleanup**: Removed redundant `import androidx.compose.ui.unit.dp`.
- **Marker Positioning Logic**: Fixed the `Modifier.offset` calculations for both saved markers and the temporary marker.
    - Used `16.dp` and `32.dp` instead of raw integers for subtractions.
    - Used `roundToPx()` within the `Density` scope of the lambda-based `offset` to correctly convert `Dp` to `Px` for `IntOffset`.
    - Added comments explaining the conversion logic.

```diff
-                        IntOffset(
-                            (screenPos.x - 16).roundToInt(),
-                            (screenPos.y - 32).roundToInt()
-                        )
+                        // screenPos.x/y are Dp. We subtract Dp and convert to Px for IntOffset.
+                        IntOffset(
+                            (screenPos.x - 16.dp).roundToPx(),
+                            (screenPos.y - 32.dp).roundToPx()
+                        )
```

## Verification Results

### Automated Tests
- Executed `./gradlew :app:compileDebugKotlin` which finished successfully.

> [!NOTE]
> The successful compilation confirms that the type mismatch is resolved and the `roundToPx()` extension is correctly used within the `Density` scope of the `Modifier.offset` lambda.
