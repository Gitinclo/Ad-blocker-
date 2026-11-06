# Project Roadmap (`ROADMAP.md`)

This document outlines the strategic vision and upcoming milestones for **CleanBrowse AI** across both the Android application and the Chrome Manifest V3 extension kit.

---

## Completed Milestones (v1.0)

- [x] **Two-Tier Hybrid Engine**: Static `declarativeNetRequest` domain rules + 6-feature structural DOM classifier.
- [x] **Interactive Ad Test Sandbox**: Built-in before/after X-Ray view and dynamic DOM mutation testing.
- [x] **Multimodal Perceptual Ad Detector**: Screenshot ad and dark-pattern detection powered by `gemini-3.8-flash`.
- [x] **AI Rule & CSS Selector Generator**: Structured JSON generation of `||domain^` filters and cosmetic rules.
- [x] **Chrome Manifest V3 Kit Exporter**: Live synchronization and export of `manifest.json`, `rules/ads.json`, `content.js`, and `popup.js`.
- [x] **100% On-Device Room Persistence**: Local SQLite storage for rules, whitelists, and telemetry logs.

---

## Short-Term Milestones (v1.1 – Q1 2027)

- [ ] **Quantized ONNX Runtime Mobile Integration**: Bundle `model.onnx` directly via ONNX Runtime Android C++ JNI alongside the WebAssembly worker.
- [ ] **Import / Export EasyList & uBlock Syntax**: Bulk import third-party `.txt` and `.json` filter lists with automatic `declarativeNetRequest` rule conversion.
- [ ] **CNAME Cloaking & First-Party Tracker Heuristics**: Detect disguised first-party analytics subdomains using URL path entropy scoring.

---

## Medium-Term Milestones (v1.2 – Q2 2027)

- [ ] **Video Pre-Roll & Overlay Ad Skipping**: Detect and fast-forward skippable video ad containers and mid-roll overlays.
- [ ] **Multi-Language Sponsored Token Embeddings**: Expand semantic keyword detection across 35+ languages for localized social and news feeds.
- [ ] **Firefox & Safari WebExtension Export Targets**: Add one-tap export profiles for Firefox MV3 (`browser_specific_settings.gecko`) and Safari Web Extensions.
