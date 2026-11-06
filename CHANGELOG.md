# Changelog (`CHANGELOG.md`)

All notable changes to **CleanBrowse AI** are documented in this file.
This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-11-06

### Added
* **Hybrid Ad-Blocking Android Browser (`BrowserScreen`)**:
  * Tier 1 `WebViewClient.shouldInterceptRequest` network blocker supporting `declarativeNetRequest`-style `||domain^` and wildcard filter rules.
  * Tier 2 Cosmetic CSS injection and 6-feature structural DOM classifier (`[ratio, hasIframe, externalLinks, hasKeyword, childCount, depth]`).
  * Built-in **Daily Pulse News Ad Sandbox** with one-tap **X-Ray View** vs **Clean View** comparison and dynamic **Inject Stealth Ad** mutation testing.
  * **CleanBrowse Popup Sheet** with master ON/OFF toggle, **Standard** vs **Smart AI** mode selector, **AI Aggressiveness Threshold** slider (`0.50`–`0.95`), and per-site whitelist switch.
* **AI DOM & Perceptual Screenshot Scanner (`AiScannerScreen`)**:
  * Interactive 6-Feature DOM Vector Lab with custom node simulation and **Gemini Semantic Check** (`gemini-3.8-flash`).
  * Multimodal **Visual Webpage Screenshot Ad Detector** using Android Photo Picker (`PickVisualMedia`), live WebView capture, and demo ad-page scanning.
* **AI Filter Rule Studio & Manifest V3 Exporter (`RuleStudioScreen`)**:
  * Natural-language and HTML-to-rule generator using structured JSON output (`Schema.obj`).
  * Live **Manifest V3 Extension Kit Exporter** (`manifest.json`, `rules/ads.json`, `content.js`, `popup.js`) with clipboard copy and system share sheet support.
* **Real-Time Telemetry Dashboard (`DashboardScreen`)**:
  * Live KPI cards, MobileNet-DOM v2 benchmark comparison table, whitelist manager, and filterable interception log backed by Android **Room**.
* **Security & Design**:
  * Configured **Firebase App Check** (`configureAppCheck` in `MainActivity.onCreate`) for zero-key client security.
  * Designed **Clean Trustworthy Minimalist** adaptive Light and Dark Material 3 themes with local `Plus Jakarta Sans`, `Inter`, and `JetBrains Mono` typography.
