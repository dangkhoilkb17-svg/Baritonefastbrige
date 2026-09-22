# Screenshot MVP

This repository contains a Tauri 2 + React/TypeScript/Vite screenshot
application under `frontend/` and `src-tauri/`. Install dependencies with
`npm install`, then run `npm run tauri dev`.

The MVP captures the first available display in full-screen mode or captures a
validated pixel region and previews the PNG locally in the window. Operation
states are explicit (`idle`, `capturing`, `success`, and `error`). Cloud AI,
OCR, history, export, scrolling capture, and sidecars are deferred to later
phases.

## Build

```bash
npm run build
```

The frontend build runs TypeScript validation followed by Vite production
bundling. Rust checks can be run from `src-tauri/` with `cargo check` and
`cargo test`.
