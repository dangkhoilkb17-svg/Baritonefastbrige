# Baritone Fast Bridge 1.21.1

Client-side Fabric addon for Baritone 1.11.3. It plans a straight, cardinal bridge, places reachable cells predictively, and keeps movement active only while a configurable safety margin exists. It does not send custom placement packets, bypass server limits, or fork Baritone.

## Requirements

- Minecraft Java 1.21.1
- Java 21
- Fabric Loader 0.16.x
- Fabric API for 1.21.1
- Baritone 1.11.3 Fabric

For development, the repository includes the Baritone 1.11.3 API jar at `libs/baritone-api-1.11.3.jar`. The API jar is compile-time only; the built addon does not bundle Baritone itself.

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
2. Baritone 1.11.3 Fabric runtime distribution
3. `baritone-fast-bridge-1.0.0.jar`

Do not install the repository's `libs/baritone-api-1.11.3.jar` as the runtime Baritone mod; it is for compilation only.

## Commands

- `#bridge`
- `#bridge 100`
- `#bridge 100 3`
- `#bridge stop`
- `/bridge` forms are also available as Fabric client commands.

Length is measured in rows. Total required placements are `length * width`. Width is perpendicular to the initial cardinal facing direction. Direction is locked when the command starts.

## Configuration

First launch creates `config/baritone-fast-bridge.json` with `defaultLength`, `defaultWidth`, `maxLength`, `maxWidth`, `placementRetryLimit`, `safetyMargin`, `useSprint`, `useSneak`, `reach`, `verifyDelayTicks`, and `preferredBlocks`.

## Fast bridge design and limits

- The planner orders the center lane before side lanes so forward safety can be established early.
- Every client tick it seeks the nearest reachable missing cell with a real neighboring support face.
- Movement and placement run in the same tick. There is no stop-look-place-step loop.
- Movement is released immediately on repeated failure, no blocks, death, disconnect, cancellation, or unsafe geometry.
- `BridgeProcess` registers through Baritone's pathing control manager and requests a pause while active, preventing another Baritone process from fighting the forced inputs.
- Placement uses `ClientPlayerInteractionManager.interactBlock`, one accepted interaction at most per client tick, followed by world-state verification and bounded retry.
- Actual speed depends on latency, server tick rate, anticheat, reach validation, inventory, and placement acceptance. No fixed speed is promised.

## Known constraints

- Straight, level, cardinal bridging only. No diagonal, staircase, jump-bridge, scaffold, or automatic inventory-screen swapping.
- Odd widths center naturally. Even widths are biased one block to the clockwise side because a player cannot stand on a half-block centerline.
- Camera is not forcibly rotated. The exact support face and hit vector are supplied to the normal interaction manager, avoiding camera snapping. Servers that require server-side view alignment may reject a placement; bounded retry then stops safely.
- The addon counts completion only from observed world block states, not from click acceptance.

## Safety testing checklist

Test in a disposable local world first: `#bridge`, explicit length, width 3, stop, empty hotbar, obstructed target, forced placement failure, death, and disconnect. Multiplayer servers may disallow automation.
