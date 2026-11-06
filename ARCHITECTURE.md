# Architecture Overview (`ARCHITECTURE.md`)

**CleanBrowse AI** is architected as a modular Kotlin + Jetpack Compose Android application paired with a Chrome Manifest V3 extension bundle generator. It implements **Clean Architecture / MVVM** with unidirectional data flow (`StateFlow`) and local-first Room persistence.

---

## 1. High-Level System Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer (Jetpack Compose)                │
│  ┌──────────────────┐ ┌──────────────────┐ ┌─────────────┐ ┌────────────┐  │
│  │  BrowserScreen   │ │  AiScannerScreen │ │ RuleStudio  │ │ Dashboard  │  │
│  │  & Shield Popup  │ │  (DOM & Vision)  │ │ & MV3 Kit   │ │ Telemetry  │  │
│  └────────┬─────────┘ └────────┬─────────┘ └──────┬──────┘ └─────┬──────┘  │
└───────────┼────────────────────┼──────────────────┼──────────────┼─────────┘
            │                    │                  │              │
            ▼                    ▼                  ▼              ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     Domain & State Layer (CleanBrowseViewModel)            │
│  • Reactive StateFlows (ShieldConfig, DomCandidates, AiTaskState, Logs)    │
└───────────┬──────────────────────────────────┬───────────────────┬─────────┘
            │                                  │                   │
            ▼                                  ▼                   ▼
┌───────────────────────────┐    ┌──────────────────────────┐ ┌──────────────┐
│   Hybrid Ad-Block Engine  │    │   CleanBrowseAiService   │ │ AdBlockDatabase│
│                           │    │   (Firebase AI Logic)    │ │ (Room SQLite)│
│ • Tier 1: Network Matcher │    │                          │ │              │
│   shouldInterceptRequest  │    │ • gemini-3.8-flash       │ │ • FilterRule │
│ • Tier 2: 6-Feature DOM   │    │ • Schema.obj JSON output │ │ • Cosmetic   │
│   Vector Scorer & CSS     │    │ • App Check Attestation  │ │ • Whitelist  │
└───────────────────────────┘    └──────────────────────────┘ └──────────────┘
```

---

## 2. Core Subsystems

### A. Tier 1: Fast Network Interception Engine
- **Android Runtime**: Overrides `WebViewClient.shouldInterceptRequest(view, request)` on a background I/O thread.
- **Pattern Matching**: Evaluates active `FilterRuleEntity` patterns (`||domain^` and wildcard rules) in `< 0.5 ms`. Matching requests return an empty `WebResourceResponse("text/plain", "UTF-8", EmptyInputStream)` before any network socket is opened.
- **Manifest V3 Parity**: Exports directly to `declarativeNetRequest` JSON rules (`rules/ads.json`).

### B. Tier 2: 6-Feature DOM Classifier & Cosmetic Collapsing
- **Feature Vector (`[1, 6]`)**:
  1. `width_to_height_ratio`: Detects standard IAB aspect ratios (`300x250` = `1.20`, `728x90` = `8.09`, `160x600` = `0.27`, native feed cards `~4.5`).
  2. `has_iframe`: Binary flag (`0` or `1`) indicating nested ad frames.
  3. `external_link_count`: Number of `<a href="...">` tags inside the container.
  4. `contains_tracking_keywords`: Binary flag (`0` or `1`) for semantic tokens (`"sponsored"`, `"promoted"`, `"advertisement"`, `"ad choices"`, `"recommended by"`).
  5. `child_node_count`: Structural complexity of immediate children.
  6. `depth_in_tree`: Nesting depth from `document.body`.
- **Cosmetic Stylesheet Injection**: Injects `{ display: none !important; }` rules on page completion to eliminate empty whitespace and prevent Cumulative Layout Shift (CLS).

### C. Cloud AI Inspector (`CleanBrowseAiService`)
- **Security**: Protected by **Firebase App Check** (`DebugAppCheckProviderFactory` in debug builds via launch intent token; `RecaptchaAppCheckProviderFactory` in release builds).
- **Structured Output**: Enforces strict JSON schemas via `Schema.obj(...)` and `responseMimeType = "application/json"` on `gemini-3.8-flash` for deterministic parsing without markdown wrappers.

### D. On-Device Persistence (`AdBlockDatabase`)
- Uses **Room** with Kotlin Symbol Processing (**KSP**) and reactive `Flow<List<T>>` queries so UI tables and the WebView interceptor stay synchronized in real time.
