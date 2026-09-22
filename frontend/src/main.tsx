import React from "react";
import { createRoot } from "react-dom/client";
import { ScreenshotApp } from "./screenshot-app";
import "./styles.css";

createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ScreenshotApp />
  </React.StrictMode>,
);
