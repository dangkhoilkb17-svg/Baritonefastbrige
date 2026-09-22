# Screenshot MVP

This repository contains a Tauri 2 + React/TypeScript/Vite screenshot
application under `frontend/` and `src-tauri/`. Install dependencies with
`npm install`, then run `npm run tauri dev`.

The capture phase supports explicit display selection, full-display PNG capture,
and an in-app drag overlay for selecting a validated pixel region from a
display preview. Operation states are explicit (`idle`, `capturing`, `success`,
and `error`). Capture stays local and previews PNG data in the window. A global
custom hotkey is not included yet because the current Tauri dependencies do not
include a verified global-hotkey plugin or binding. Cloud AI, OCR, history,
export, scrolling capture, and sidecars are deferred to later phases.

## Build

```bash
npm run build
```

The frontend build runs TypeScript validation followed by Vite production
bundling. Rust checks can be run from `src-tauri/` with `cargo check` and
`cargo test`.
