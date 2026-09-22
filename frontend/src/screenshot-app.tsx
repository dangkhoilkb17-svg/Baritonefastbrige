import { invoke } from "@tauri-apps/api/core";
import { FormEvent, PointerEvent, useEffect, useRef, useState } from "react";

type CaptureMode =
  | { kind: "fullscreen"; display_id: number }
  | { kind: "region"; display_id: number; x: number; y: number; width: number; height: number };

type CaptureResult = {
  data_url: string;
  width: number;
  height: number;
};

type Display = {
  id: number;
  width: number;
  height: number;
  is_primary: boolean;
};

type OperationState =
  | { kind: "idle" }
  | { kind: "capturing" }
  | { kind: "success"; result: CaptureResult }
  | { kind: "error"; message: string };

type Selection = { startX: number; startY: number; endX: number; endY: number };

export function ScreenshotApp() {
  const [mode, setMode] = useState<"fullscreen" | "region">("fullscreen");
  const [displays, setDisplays] = useState<Display[]>([]);
  const [displayId, setDisplayId] = useState<number>();
  const [selectionPreview, setSelectionPreview] = useState<CaptureResult>();
  const [selection, setSelection] = useState<Selection>();
  const [dragging, setDragging] = useState(false);
  const [operation, setOperation] = useState<OperationState>({ kind: "idle" });
  const previewRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    invoke<Display[]>("list_displays")
      .then((available) => {
        setDisplays(available);
        setDisplayId((current) => current ?? available.find((display) => display.is_primary)?.id ?? available[0]?.id);
      })
      .catch((error) => setOperation({ kind: "error", message: String(error) }));
  }, []);

  async function capture(event: FormEvent) {
    event.preventDefault();
    if (displayId === undefined) {
      setOperation({ kind: "error", message: "Select a display before capturing." });
      return;
    }
    if (mode === "region" && (!selectionPreview || !selection)) {
      setOperation({ kind: "error", message: "Drag across the display preview to select a region." });
      return;
    }
    setOperation({ kind: "capturing" });
    const captureMode: CaptureMode = mode === "fullscreen"
      ? { kind: "fullscreen", display_id: displayId }
      : { kind: "region", display_id: displayId, ...selectionToRegion(selection!, selectionPreview!, previewRef.current) };

    try {
      const result = await invoke<CaptureResult>("capture_screenshot", { mode: captureMode });
      setOperation({ kind: "success", result });
    } catch (error) {
      setOperation({ kind: "error", message: String(error) });
    }
  }

  async function prepareRegionSelection() {
    if (displayId === undefined) return;
    setOperation({ kind: "capturing" });
    try {
      const preview = await invoke<CaptureResult>("capture_screenshot", {
        mode: { kind: "fullscreen", display_id: displayId },
      });
      setSelectionPreview(preview);
      setSelection(undefined);
      setOperation({ kind: "idle" });
    } catch (error) {
      setOperation({ kind: "error", message: String(error) });
    }
  }

  function beginSelection(event: PointerEvent<HTMLDivElement>) {
    const point = previewPoint(event, previewRef.current);
    if (!point) return;
    event.currentTarget.setPointerCapture(event.pointerId);
    setDragging(true);
    setSelection({ startX: point.x, startY: point.y, endX: point.x, endY: point.y });
  }

  function updateSelection(event: PointerEvent<HTMLDivElement>) {
    if (!dragging) return;
    const point = previewPoint(event, previewRef.current);
    if (point) setSelection((current) => current && { ...current, endX: point.x, endY: point.y });
  }

  function endSelection() {
    setDragging(false);
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

        <label>
          Display
          <select value={displayId ?? ""} onChange={(event) => setDisplayId(Number(event.target.value))}>
            {displays.map((display, index) => (
              <option key={display.id} value={display.id}>
                Display {index + 1} · {display.width} × {display.height}{display.is_primary ? " · Primary" : ""}
              </option>
            ))}
          </select>
        </label>

        {mode === "region" && (
          <section>
            <button type="button" onClick={prepareRegionSelection} disabled={operation.kind === "capturing" || displayId === undefined}>
              {selectionPreview ? "Retake display preview" : "Choose region"}
            </button>
            {selectionPreview && (
              <div
                className="selection-preview"
                ref={previewRef}
                onPointerDown={beginSelection}
                onPointerMove={updateSelection}
                onPointerUp={endSelection}
                onPointerCancel={endSelection}
              >
                <img src={selectionPreview.data_url} alt="Display preview for region selection" />
                {selection && <div className="selection-box" style={selectionStyle(selection, previewRef.current)} />}
              </div>
            )}
          </section>
        )}

        <button type="submit" disabled={operation.kind === "capturing" || displayId === undefined}>
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

function previewPoint(event: PointerEvent<HTMLDivElement>, preview: HTMLDivElement | null) {
  if (!preview) return;
  const bounds = preview.getBoundingClientRect();
  return {
    x: Math.max(0, Math.min(bounds.width, event.clientX - bounds.left)),
    y: Math.max(0, Math.min(bounds.height, event.clientY - bounds.top)),
  };
}

function selectionBounds(selection: Selection) {
  return {
    x: Math.min(selection.startX, selection.endX),
    y: Math.min(selection.startY, selection.endY),
    width: Math.abs(selection.endX - selection.startX),
    height: Math.abs(selection.endY - selection.startY),
  };
}

function selectionStyle(selection: Selection, preview: HTMLDivElement | null) {
  const bounds = preview?.getBoundingClientRect();
  if (!bounds) return {};
  const selected = selectionBounds(selection);
  return {
    left: `${(selected.x / bounds.width) * 100}%`,
    top: `${(selected.y / bounds.height) * 100}%`,
    width: `${(selected.width / bounds.width) * 100}%`,
    height: `${(selected.height / bounds.height) * 100}%`,
  };
}

function selectionToRegion(
  selection: Selection,
  image: CaptureResult,
  preview: HTMLDivElement | null,
) {
  const bounds = preview?.getBoundingClientRect();
  if (!bounds) return { x: 0, y: 0, width: image.width, height: image.height };
  const selected = selectionBounds(selection);
  return {
    x: Math.round((selected.x / bounds.width) * image.width),
    y: Math.round((selected.y / bounds.height) * image.height),
    width: Math.max(1, Math.round((selected.width / bounds.width) * image.width)),
    height: Math.max(1, Math.round((selected.height / bounds.height) * image.height)),
  };
}
