# Walkthrough - Robust Center Camera on Start

I have updated the logic to ensure the map camera centers on the user's location more reliably when the app starts.

## Changes Made

### MapScreen.kt
- Moved the location state initialization (`locationProvider`, `locationState`) outside the `MaplibreMap` block to the top level of the `Box` container.
- Implemented a `LaunchedEffect(locationState.location)` that specifically watches for the first valid location update.
- This ensures that as soon as the GPS (or emulator simulation) provides a position, the camera triggers the animation.
- Removed the centering logic from the `LocationTrackingEffect` lambda to prevent potential conflicts or missed updates.

## Verification Results

### Manual Verification
- Start the app.
- If no location is set in the emulator, the map will show the default position.
- Set a location in the emulator (Extended Controls -> Location -> Set Location).
- The map should now immediately and smoothly animate to that location.
- After this initial centering, you can freely move the map around.
