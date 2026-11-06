# Project Governance (`GOVERNANCE.md`)

This document defines the governance structure, roles, and decision-making procedures for **CleanBrowse AI**.

---

## 1. Project Roles

### A. Users & Community Members
Anyone using the CleanBrowse AI Android app or Chrome Manifest V3 extension kit. Community members contribute by reporting false positives, sharing undetected ad patterns, and suggesting UX improvements.

### B. Filter & Model Contributors
Community members who submit pull requests adding `declarativeNetRequest` filter patterns (`rules/ads.json`), cosmetic CSS selectors, or labeled 6-feature DOM training vectors.

### C. Core Maintainers
Maintainers oversee architectural integrity, review pull requests, curate the default built-in ruleset, manage Firebase App Check / security configurations, and cut official releases.

---

## 2. Decision-Making Process

CleanBrowse AI operates on a **Lazy Consensus** model guided by three non-negotiable core principles:

1. **Zero User Telemetry**: No change may introduce background tracking, browsing history exfiltration, or third-party analytics SDKs.
2. **High Precision Over Aggressive Breakage**: Built-in default rules and DOM classifier thresholds must prioritize zero false positives on editorial content and core site navigation.
3. **Sub-5ms Local Performance**: Local network and DOM filtering paths must never block the main UI thread or cause scroll jank.

### Pull Request Approval
* **Filter Rule Updates**: Require verification against false positives and approval from **1 Maintainer**.
* **Core Engine / Architecture Changes**: Require passing unit/Robolectric tests (`gradle :app:testDebugUnitTest`) and approval from **2 Maintainers** (or 72 hours with no objections from Core Maintainers).
