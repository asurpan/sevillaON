# Simplify Activity Profiles and Fix 'PASEO' Reference

The goal is to reduce the number of activity profiles to a specific set and fix the unresolved reference `PASEO` by renaming `CAMINAR` to `PASEO`.

## Proposed Activity Profiles
- `NORMAL` (Keep as default)
- `MOTO`
- `CICLISMO`
- `SENDERISMO`
- `PASEO` (Renamed from `CAMINAR`)
- `SOCORRISTAS`
- `CARAVANAS`

## Proposed Changes

### [shared Component](file:///C:/Users/Jose/AndroidStudioProjects/on/shared)

#### [MODIFY] [Models.kt](file:///C:/Users/Jose/AndroidStudioProjects/on/shared/src/commonMain/kotlin/com/sagon/on/Models.kt)
- Update `ActivityProfile` enum:
    - Keep `NORMAL`, `MOTO`, `CICLISMO`, `SENDERISMO`, `SOCORRISTAS`, `CARAVANAS`.
    - Rename `CAMINAR` to `PASEO`.
    - Remove all other profiles (`MONTANA`, `RUNNING`, `OFFROAD`, `CAMIONEROS`, `TACTICO`).
- Update `getActivityIcon`:
    - Clean up the `when` block to only include the simplified profiles.
    - Ensure `PASEO` maps to `Icons.Rounded.DirectionsWalk`.

#### [MODIFY] [RadioDialogs.kt](file:///C:/Users/Jose/AndroidStudioProjects/on/shared/src/commonMain/kotlin/com/sagon/on/RadioDialogs.kt)
- Update all `when` expressions and logic involving `ActivityProfile` to use the simplified list.
- Fix the reference to `PASEO`.

#### [MODIFY] [MotorcycleCore.kt](file:///C:/Users/Jose/AndroidStudioProjects/on/shared/src/commonMain/kotlin/com/sagon/on/MotorcycleCore.kt)
- Update `getWindFilterCutoff` and `getActivityVoxThreshold` to handle only the remaining profiles.

### [webApp Component](file:///C:/Users/Jose/AndroidStudioProjects/on/webApp)

#### [MODIFY] [main.kt](file:///C:/Users/Jose/AndroidStudioProjects/on/webApp/src/webMain/kotlin/com/sagon/on/main.kt)
- Update any hardcoded strings or logic referencing the removed profiles.

## Verification Plan

### Automated Tests
- Run `./gradlew :shared:compileKotlinJs` to verify the fix and simplification.
- Run `./gradlew :webApp:compileKotlinJs` to verify consistency in the web module.

### Manual Verification
- None required as this is a structural/compile-time change.
