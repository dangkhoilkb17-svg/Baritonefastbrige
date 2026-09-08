# Baritone Fast Bridge 1.21.1

Client-side Fabric addon for Baritone 1.11.3. It plans a straight, cardinal bridge, places reachable cells predictively, and keeps movement active only while a configurable safety margin exists. It does not send custom placement packets, bypass server limits, or fork Baritone.

## Requirements

- Minecraft Java 1.21.1
- Java 21
- Fabric Loader 0.16.x
- Fabric API for 1.21.1
- Baritone API Fabric 1.11.3 from the official Baritone release

The repository does not include third-party Baritone binaries. Download the official
`baritone-api-fabric-1.11.3.jar` for version 1.11.3 and place it in `libs/`.

## Build

On Windows, use the included bootstrap script:

```bat
gradlew.bat clean build
```

On Linux/macOS:

```bash
./gradlew clean build
```

The Windows script downloads a clean Gradle 8.10.2 distribution when its local bootstrap copy is missing. This avoids depending on a broken or incomplete Gradle installation in `%USERPROFILE%\\.gradle`.

Output: `build/libs/baritone-fast-bridge-1.0.0.jar`.

## Install

Copy these mods into `.minecraft/mods/`:

1. Fabric API for Minecraft 1.21.1
2. The official `baritone-api-fabric-1.11.3.jar`
3. `baritone-fast-bridge-1.0.0.jar`

Do not install `baritone-standalone-fabric-1.11.3.jar` together with this addon; it uses a different obfuscated API surface.

The API JAR is a separate runtime mod and must not be bundled into the addon.

## Commands

- `#bridge`
- `#bridge 100`
- `#bridge 100 3`
- `#bridge auto 3`
- `/bridge` forms are also available as Fabric client commands, including `/bridge auto <width>`.

Length is measured in rows. Total required placements are `length * width`. Width is perpendicular to the initial cardinal facing direction. When a bridge starts, the current camera yaw/pitch is locked until the bridge completes, fails, is cancelled, or the client disconnects.

### Auto bridge

`#bridge auto <width>` scans forward from the player's current floor position for the first row where every lane across the requested width contains a non-replaceable block. That row is treated as the opposite shore, and the addon builds all replaceable rows before it. The scan and resulting bridge length are capped by `maxLength`; it will not search or build indefinitely.

## Configuration

First launch creates `config/baritone-fast-bridge.json` with `defaultLength`, `defaultWidth`, `maxLength`, `maxWidth`, `placementRetryLimit`, `useSprint`, `useSneak`, `reach`, `verifyDelayTicks`, and `preferredBlocks`.

## Fast bridge design and limits

- The planner orders the center lane before side lanes so forward safety can be established early.
- Every client tick it seeks the nearest reachable missing cell with a real neighboring support face.
- Movement and placement run in the same tick. There is no stop-look-place-step loop.
- Movement is released immediately on repeated failure, no blocks, death, disconnect, cancellation, or unsafe geometry.
- `BridgeProcess` registers through Baritone's pathing control manager and requests a pause while active, preventing another Baritone process from fighting the forced inputs.
- Placement uses `ClientPlayerInteractionManager.interactBlock`, one accepted interaction at most per client tick, followed by world-state verification and bounded retry.
- Actual speed depends on latency, server tick rate, anticheat, reach validation, inventory, and placement acceptance. No fixed speed is promised.
- While active, the camera is held at the start yaw/pitch so mouse movement cannot redirect the view during automated bridging.

## Known constraints

- Straight, level, cardinal bridging only. No diagonal, staircase, jump-bridge, scaffold, or automatic inventory-screen swapping.
- Odd widths center naturally. Even widths are biased one block to the clockwise side because a player cannot stand on a half-block centerline.
- Auto mode requires a full-width non-replaceable shore row. Partial-width or irregular terrain is not treated as the destination shore.
- The addon counts completion only from observed world block states, not from click acceptance.

## Safety testing checklist

Test in a disposable local world first: `#bridge`, explicit length, width 3, `#bridge auto 3`, stop, empty hotbar, obstructed target, forced placement failure, death, and disconnect. Multiplayer servers may disallow automation.
