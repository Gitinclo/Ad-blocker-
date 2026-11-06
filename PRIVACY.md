# Privacy Policy (`PRIVACY.md`)

**Effective Date:** November 6, 2026

**CleanBrowse AI** is engineered around a strict **Privacy-by-Design** and **Zero-Telemetry** architecture. Your browsing history, filter rules, whitelisted domains, and interception logs remain exclusively on your device.

---

## 1. Data Stored Locally on Your Device

All operational data is stored inside a sandboxed, device-local SQLite database (`cleanbrowse_ai.db`) managed by Android Room:

| Data Category | Storage Location | Sent to External Servers? | Purpose |
| :--- | :--- | :--- | :--- |
| **Network Filter Rules (`||domain^`)** | Local Room Database | **Never** | Blocking ad and tracker requests in real time |
| **Cosmetic CSS Selectors** | Local Room Database | **Never** | Collapsing empty ad containers in the DOM |
| **Per-Site Whitelist** | Local Room Database | **Never** | Remembering sites where you paused blocking |
| **Interception & Latency Logs** | Local Room Database | **Never** | Displaying live telemetry in the Dashboard tab |
| **6-Feature DOM Scoring** | Local Memory / WebView | **Never** | Sub-5ms structural classification of DOM nodes |

You can clear all stored interception logs at any time with a single tap in the **Telemetry** tab (`Clear Telemetry Logs`).

---

## 2. Opt-In AI Assistant Features (Firebase AI Logic)

CleanBrowse AI includes three **explicitly user-triggered** AI tools powered by Google Gemini (`gemini-3.8-flash`) via Firebase AI Logic:

1. **AI Filter Rule Generator**: Sends only the text prompt, URL, or HTML snippet you type into the generator box when you tap **Generate with AI**.
2. **Visual Webpage Screenshot Ad Detector**: Sends only the specific screenshot bitmap you capture or select when you tap **Scan Page with AI Vision** or **Pick Screenshot**.
3. **Gemini Semantic DOM Check**: Sends only the selected DOM element's tag summary, 6-feature vector, and text snippet when you explicitly tap **Gemini Semantic Check**.

> **Important:** Background browsing, automatic network blocking, and local 6-feature DOM classification **never** invoke cloud AI APIs or transmit URLs off your device.

---

## 3. Zero Analytics, Cookies, or Third-Party Trackers

* We do **not** bundle third-party analytics SDKs, crash reporters, or advertising identifiers.
* We do **not** sell, rent, or monetize user data or browsing telemetry.
* All requests to Firebase AI Logic are attested via **Firebase App Check** to prevent unauthorized API abuse without requiring user accounts or personal identifiers.
