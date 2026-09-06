# Baritone Fast Bridge 1.21.1

Client-side Fabric addon for Baritone 1.11.2. It plans a straight, cardinal bridge, places reachable cells predictively, and keeps movement active only while a configurable safety margin exists. It does not send custom placement packets, bypass server limits, or fork Baritone.

## Requirements

- Minecraft Java 1.21.1
- Java 21
- Fabric Loader 0.16.x
- Fabric API for 1.21.1
- Baritone 1.11.2 Fabric

The official Baritone v1.11.2 release states support for Minecraft 1.21 and 1.21.1. For development, put the official `baritone-api-1.11.2.jar` in `libs/`. If absent, the build uses the verified Curse Maven mirror `curse.maven:baritone-bot-1119902:7673623` only as a compile-time artifact. The built addon does not bundle Baritone.

## Build

```bash
# Recommended: download official baritone-api-1.11.2.jar and place it here
mkdir -p libs
cp /path/to/baritone-api-1.11.2.jar libs/
./gradlew build
```

Output: `build/libs/baritone-fast-bridge-1.0.0.jar`.

## Install

Copy these three mods into `.minecraft/mods/`:

1. Fabric API for Minecraft 1.21.1
2. Baritone 1.11.2 Fabric, preferably `baritone-api-fabric-1.11.2.jar` or the matching Fabric distribution
3. `baritone-fast-bridge-1.0.0.jar`

Do not install the plain API-only jar as the runtime Baritone mod if it has no Fabric entrypoint. The local `libs/baritone-api-1.11.2.jar` is for compilation.

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
