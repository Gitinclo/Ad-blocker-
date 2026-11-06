# CleanBrowse AI

> **CleanBrowse AI** is a privacy-first hybrid ad-blocking Android browser and Chrome Manifest V3 extension studio that combines zero-latency `declarativeNetRequest` domain rules with a 6-feature structural DOM classifier and Gemini multimodal perceptual ad detection.

---

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.md)
[![Platform: Android & MV3](https://img.shields.io/badge/Platform-Android%20%7C%20Chrome%20MV3-10B981.svg)](ARCHITECTURE.md)
[![Privacy: Zero Telemetry](https://img.shields.io/badge/Privacy-Zero%20Telemetry-2563EB.svg)](PRIVACY.md)

---

## Key Features

* **Two-Tier Hybrid Engine**:
  * **Tier 1 (Fast Network Filter)**: Drops requests to known ad brokers, analytics endpoints, and tracking pixels (`doubleclick.net`, `googlesyndication.com`, `google-analytics.com`, `criteo.net`, `taboola.com`, `outbrain.com`, `fbevents.js`) at near-zero CPU overhead.
  * **Tier 2 (6-Feature DOM Classifier)**: Evaluates candidate DOM containers using structural and semantic signals (`[width_to_height_ratio, has_iframe, external_link_count, contains_tracking_keywords, child_node_count, depth_in_tree]`) to collapse stealth native ads and empty frames.
* **Multimodal Webpage Screenshot Ad Detector**: Uses Gemini Vision (`gemini-3.8-flash`) via Firebase AI Logic with App Check attestation to identify disguised sponsored posts, IAB banners, and dark-pattern download buttons from screenshots.
* **AI Filter Rule & CSS Selector Generator**: Converts plain-English descriptions, URLs, or raw HTML snippets into structured `declarativeNetRequest` JSON rules and CSS element-hiding selectors.
* **Manifest V3 Extension Kit Exporter**: Generates ready-to-load `manifest.json`, `rules/ads.json`, `content.js`, and `popup.js` files synced with your custom ruleset.
* **Real-Time Telemetry Dashboard**: Tracks blocked requests, collapsed DOM nodes, per-element inference latency (`ms`), and per-site whitelists stored 100% locally in Room SQLite.

---

## Project Documentation Index

* [**ARCHITECTURE.md**](ARCHITECTURE.md) — Technical deep-dive into the Two-Tier Hybrid Engine, MVVM data flow, Room schema, and Firebase AI Logic integration.
* [**PRIVACY.md**](PRIVACY.md) — Strict zero-telemetry policy, local SQLite storage guarantees, and opt-in AI Vision handling.
* [**ROADMAP.md**](ROADMAP.md) — Upcoming milestones for CleanBrowse AI across Android and desktop browsers.
* [**CHANGELOG.md**](CHANGELOG.md) — Chronological release history and version notes.
* [**CONTRIBUTING.md**](CONTRIBUTING.md) — Guidelines for submitting filter rules, DOM training vectors, and pull requests.
* [**CODE_OF_CONDUCT.md**](CODE_OF_CONDUCT.md) — Community standards and contributor expectations.
* [**SECURITY.md**](SECURITY.md) — Vulnerability disclosure policy and App Check security architecture.
* [**SUPPORT.md**](SUPPORT.md) — How to get help, report broken pages (false positives), or request unblocked ad fixes.
* [**GOVERNANCE.md**](GOVERNANCE.md) — Project maintenance model, filter list curation roles, and decision-making process.
* [**LICENSE.md**](LICENSE.md) — MIT License terms.
