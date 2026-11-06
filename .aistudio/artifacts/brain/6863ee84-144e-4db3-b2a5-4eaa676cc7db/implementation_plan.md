# CleanBrowse AI 2.0 — Power Blocker & Multi-Tab Privacy Browser Upgrade

**CleanBrowse AI 2.0** upgrades the hybrid ad-blocking browser and rule studio with an **Interactive Element Zapper**, **Cookie & Annoyance Auto-Blocker**, **Bulk Filter List Subscriptions (`EasyList`, `EasyPrivacy`, `Cookie Shield`, `Anti-Adblock`)**, **CNAME Tracker & Anti-Adblock Bypass**, and **Multi-Tab Browsing with Bookmarks and a One-Tap History Burner**.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Upgrade Scope**
> Every capability you selected in the clarification step is included below while preserving all existing Hybrid Browser, 6-Feature DOM Scanner, Multimodal Screenshot AI, Rule Studio, and Telemetry features.

- **Confirmed Ad-Blocking Enhancements**:
  1. **Interactive Element Zapper (`Zap Mode`)**: Toggle Zap Mode in the browser to tap any element on the page (in both the interactive DOM view and live WebView via injected click-interceptor JS) to vanish it instantly and persist its CSS selector.
  2. **Cookie Banner & Popup Annoyance Auto-Blocker**: Dedicated shield toggle that hides GDPR/cookie consent overlays (`OneTrust`, `Cookiebot`, `TrustArc`, newsletter popups, and sticky app-install banners).
  3. **Bulk Filter List Subscriptions & Syntax Importer**: Manage curated filter lists (**EasyList Core**, **EasyPrivacy**, **Fanboy Cookie & Annoyance List**, **Anti-Adblock Defuser**) and paste/import raw EasyList/uBlock syntax (`||domain^` and `##.selector`) into active rules.
  4. **Anti-Adblock Bypass & CNAME Tracker Detector**: Spoofs bait elements (`.adsbox`, `window.canRunAds = true`) to defeat adblock-wall scripts and analyzes first-party subdomain entropy to catch CNAME-cloaked trackers.
- **Confirmed Browser Experience Upgrades**:
  - **Multi-Tab Manager**: Create, switch, and close multiple concurrent browser tabs with a tab count badge in the omnibox.
  - **Bookmarks & Quick History Burner**: Save favorite URLs, view local browsing history, and trigger a **1-Tap Fire/Burn Action** that wipes browsing history, WebView cache, and session storage.

---

## 1. Overview & Core Concept

- **What It Does**: Transforms CleanBrowse AI into a comprehensive everyday privacy browser and ad-blocking workstation. Users can browse across multiple tabs, zap distracting page elements on the fly, subscribe to industry-standard filter lists, automatically dismiss cookie consent walls, defeat anti-adblock scripts, and wipe their browsing footprint with a single tap.
- **Target Audience / Persona**: Privacy-first mobile users and power users who want uBlock-Origin-caliber control (Element Zapper, Filter Lists, Anti-Adblock, CNAME inspection) combined with on-device ML and Gemini AI assistance.
- **Key Value**: Eliminates not only traditional display ads, but also modern web annoyances—cookie consent popups, newsletter overlays, anti-adblock walls, and CNAME-cloaked first-party trackers.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Multi-Tab Omnibox, Bookmarks & 1-Tap History Burner (`Browser` Tab)**:
   - **Tab Bar & Switcher Sheet**: Tap the **Tabs Badge (`[2]`)** next to the URL bar to switch between open tabs, open a new tab (`+`), or close individual tabs.
   - **Bookmarks & History Drawer**: Star any URL to add it to **Bookmarks** (displayed as quick-launch chips below the omnibox) or open the **History & Privacy Burner Sheet** to inspect visited pages and tap **Burn History & Cache** with animated confirmation.
   - **Interactive Element Zapper (`⚡ Zap Mode`)**: Tap the **Element Zapper** chip in the browser toolbar. In Zap Mode, tapping any page element or DOM container highlights it in coral-amber, collapses it immediately, and saves a permanent cosmetic rule (`Zapped Element`) so it never reappears.

2. **Popup Shield Controls: Cookie Blocker & Anti-Adblock (`Popup Sheet`)**:
   - Extends the CleanBrowse Popup Sheet with two new instant switches:
     - **Cookie & Annoyance Auto-Blocker**: Strips consent modals and scroll-locking overlays.
     - **Anti-Adblock & CNAME Cloaking Shield**: Injects bait-defusing scriptlets and flags disguised first-party tracker subdomains.

3. **Bulk Filter List Subscriptions & Importer (`Rule Studio` Tab)**:
   - Adds a new **Filter Lists** sub-tab alongside Network Rules, Cosmetic CSS, and MV3 Kit:
     - Toggle subscriptions for **EasyList Core**, **EasyPrivacy**, **Cookie & Annoyance Shield**, and **Anti-Adblock Defuser**, with rule counts and last-sync timestamps.
     - **Bulk Syntax Importer**: Paste multiple lines of standard adblock syntax (`||tracker.com^`, `##.promo-modal`) to parse and import them into the local Room database in one batch.

4. **CNAME & Anti-Adblock Telemetry (`Telemetry` Tab)**:
   - Adds filter chips and KPI tracking for **CNAME Cloaked Trackers Caught**, **Cookie Banners Dismissed**, and **Anti-Adblock Scripts Neutralized**.

### Visual Identity & Theme

- **Aesthetic Direction**: Retains the **Clean Trustworthy Minimalist** adaptive Light (`#F8FAFC`) and Dark (`#0B0F19`) Material 3 system.
- **New Semantic Accents**:
  - **Zapper Mode Accent**: Electric Amber (`#F59E0B`) border and crosshair indicator when Element Zapper mode is active.
  - **Privacy Burner Accent**: Flame Coral (`#EF4444`) tonal button for the 1-tap History & Cache Burner.
  - **CNAME / Anti-Adblock Badge**: Neural Indigo (`#4F46E5` / `#818CF8`) for advanced heuristic interceptions.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual WebView + Sandbox Element Zapper**
  - *Chosen Approach*: When **Zap Mode** is active, inject a lightweight touch/click listener into the live Android `WebView` that computes the tapped element's unique CSS selector (`tagName.className` or `#id`), hides it (`display: none !important`), and posts the selector back to Kotlin via `@JavascriptInterface` to persist in Room—while also supporting 1-tap zapping on the interactive Sandbox cards.
  - *Why*: Gives users genuine uBlock-style point-and-click element zapping on both live websites and the built-in test bench.

- **Decision 2: CNAME Entropy & Anti-Adblock Defuser Engine**
  - *Chosen Approach*: Combine a heuristic CNAME subdomain entropy analyzer (detecting randomized first-party tracker prefixes like `trk-89f2a.site.com` or known cloaking CNAMEs like `eulerian`, `pardot`, `omtrdc`) with an injected anti-adblock defuser scriptlet that satisfies common bait checks (`window.adblock = false`, `window.canRunAds = true`).
  - *Why*: Standard Android `WebView` does not expose raw DNS CNAME records to JavaScript, so combining known CNAME tracker signatures with subdomain entropy scoring provides fast, zero-latency detection on mobile.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                      CleanBrowse AI 2.0 (Jetpack Compose)                    │
│  ┌────────────────────────┐ ┌────────────────┐ ┌──────────────┐ ┌──────────┐ │
│  │ Multi-Tab Browser      │ │ AI Scanner     │ │ Rule Studio  │ │Telemetry │ │
│  │ • Element Zapper Mode  │ │ • 6-Feat DOM   │ │ • Bulk Lists │ │ • CNAME  │ │
│  │ • Bookmarks & Burner   │ │ • Vision Scan  │ │ • Syntax Imp │ │ • Cookie │ │
│  │ • Cookie/Anti-Adblock  │ │                │ │ • MV3 Export │ │   Metrics│ │
│  └───────────┬────────────┘ └────────┬───────┘ └──────┬───────┘ └────┬─────┘ │
└──────────────┼───────────────────────┼────────────────┼──────────────┼───────┘
               │                       │                │              │
               ▼                       ▼                ▼              ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     CleanBrowseViewModel (StateFlow / MVVM)                  │
└──────────────┬────────────────────────────────────────┬──────────────┬───────┘
               │                                        │              │
               ▼                                        ▼              ▼
┌─────────────────────────────────────┐  ┌────────────────────┐ ┌──────────────┐
│     Enhanced Hybrid Block Engine    │  │ Firebase AI Logic  │ │ Room SQLite  │
│ • Tier 1: Network + CNAME Heuristic │  │ (gemini-3.8-flash) │ │ • FilterRule │
│ • Tier 2: 6-Feature DOM + Zapper JS │  │ • Rule Generator   │ │ • Cosmetic   │
│ • Tier 3: Cookie & Anti-Adblock JS  │  │ • Vision Detector  │ │ • FilterList │
└─────────────────────────────────────┘  └────────────────────┘ │ • Bookmark   │
                                                                │ • History    │
                                                                └──────────────┘
```

### Data Model & State Additions

- **`FilterListSubscriptionEntity` (Room)**: `id`, `title` (e.g., `"EasyList Core"`, `"EasyPrivacy"`, `"Fanboy Cookie & Annoyance Shield"`, `"Anti-Adblock Defuser"`), `description`, `category`, `ruleCount`, `isEnabled`, `lastUpdated`.
- **`BookmarkEntity` (Room)**: `id`, `title`, `url`, `addedAt`.
- **`BrowsingHistoryEntity` (Room)**: `id`, `title`, `url`, `visitedAt`, `blockedAdsOnVisit`.
- **`BrowserTabState`**: `tabId`, `title`, `url`, `blockedCount`.
- **`ShieldConfig` Extensions**: `isElementZapperActive`, `blockCookieBanners`, `antiAdblockEnabled`, `detectCnameTrackers`.

### Interactive Component & State Mapping

- **Interactive Element Zapper (`Zap Mode`)**: Toggling Zap Mode highlights the browser viewport in Amber. Tapping any element in the WebView or Sandbox extracts its CSS selector, appends a `CosmeticRuleEntity(source = "Element Zapper")` to Room, logs a `ZAPPER` interception event, and collapses the element immediately.
- **Bulk Filter List Subscriptions & Importer**: Toggling a subscription in `Rule Studio -> Filter Lists` activates or deactivates its associated network/cosmetic rules and updates total active rule counts. Pasting raw EasyList lines (`||domain^` and `##.selector`) parses and inserts them into Room with instant feedback.
- **Multi-Tab, Bookmarks & History Burner**: Users can open new tabs, switch between tabs, star/unstar the current URL into `BookmarkEntity`, view timestamped `BrowsingHistoryEntity` entries, and tap **Burn History & Cache** to purge all history rows and clear WebView caches.
