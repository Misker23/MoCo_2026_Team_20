# Implementation Plan - Center Camera on Start

Currently, the app starts with a fixed camera position. We want to automatically center the camera on the user's current location as soon as it becomes available after the app starts.

## User Review Required

> [!IMPORTANT]
> The camera will automatically move to the user's location only **once** after the app starts and gets a GPS fix. This allows the user to still pan around the map freely afterwards without being forced back to their position.

## Proposed Changes

### [Component Name] MapScreen

#### [MODIFY] [MapScreen.kt](file:///C:/Users/Artem/StudioProjects/MoCo_2026_Team_20/app/src/main/java/com/example/ap2/MapScreenComposeables/MapScreen.kt)
- Add a `remember { mutableStateOf(false) }` boolean `isInitialLocationSet` to track if we've already centered the camera.
- Modify the `LocationTrackingEffect` logic to only trigger the camera animation if `isInitialLocationSet` is false.
- Set `isInitialLocationSet` to true once the first animation is triggered.

## Verification Plan

### Manual Verification
- Start the app.
- Use the Emulator's location controls to set a position.
- Verify the map camera automatically moves to that location once.
- Verify you can then pan away from your location without the camera jumping back.
