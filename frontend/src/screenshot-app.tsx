import { invoke } from "@tauri-apps/api/core";
import { FormEvent, useState } from "react";

type CaptureMode =
  | { kind: "fullscreen" }
  | { kind: "region"; x: number; y: number; width: number; height: number };

type CaptureResult = {
  data_url: string;
  width: number;
  height: number;
};

type OperationState =
  | { kind: "idle" }
  | { kind: "capturing" }
  | { kind: "success"; result: CaptureResult }
  | { kind: "error"; message: string };

const initialRegion = { x: 0, y: 0, width: 800, height: 600 };

export function ScreenshotApp() {
  const [mode, setMode] = useState<"fullscreen" | "region">("fullscreen");
  const [region, setRegion] = useState(initialRegion);
  const [operation, setOperation] = useState<OperationState>({ kind: "idle" });

  async function capture(event: FormEvent) {
    event.preventDefault();
    setOperation({ kind: "capturing" });
    const captureMode: CaptureMode =
      mode === "fullscreen" ? { kind: "fullscreen" } : { kind: "region", ...region };

    try {
      const result = await invoke<CaptureResult>("capture_screenshot", { mode: captureMode });
      setOperation({ kind: "success", result });
    } catch (error) {
      setOperation({ kind: "error", message: String(error) });
    }
  }

  const updateRegion = (name: keyof typeof region, value: string) => {
    setRegion((current) => ({ ...current, [name]: Number(value) }));
  };

  return (
    <main className="app-shell">
      <header>
        <p className="eyebrow">AI Screenshot App</p>
        <h1>Capture a screen</h1>
        <p className="intro">The MVP captures a full display or a pixel-bounded region locally.</p>
      </header>

      <form onSubmit={capture} className="capture-panel">
        <fieldset>
          <legend>Capture source</legend>
          <label>
            <input
              type="radio"
              checked={mode === "fullscreen"}
              onChange={() => setMode("fullscreen")}
            />
            Full screen
          </label>
          <label>
            <input type="radio" checked={mode === "region"} onChange={() => setMode("region")} />
            Region
          </label>
        </fieldset>

        {mode === "region" && (
          <div className="region-grid">
            {(["x", "y", "width", "height"] as const).map((name) => (
              <label key={name}>
                {name}
                <input
                  type="number"
                  min={name === "width" || name === "height" ? 1 : 0}
                  value={region[name]}
                  onChange={(event) => updateRegion(name, event.target.value)}
                />
              </label>
            ))}
          </div>
        )}

        <button type="submit" disabled={operation.kind === "capturing"}>
          {operation.kind === "capturing" ? "Capturing…" : "Capture screenshot"}
        </button>
      </form>

      {operation.kind === "error" && <p className="status error">{operation.message}</p>}
      {operation.kind === "success" && (
        <section className="result">
          <p className="status success">
            Captured {operation.result.width} × {operation.result.height}px.
          </p>
          <img src={operation.result.data_url} alt="Latest screenshot" />
        </section>
      )}
    </main>
  );
}
