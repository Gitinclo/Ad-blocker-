# Support & Troubleshooting Guide (`SUPPORT.md`)

Need help configuring **CleanBrowse AI**, fixing a broken webpage (false positive), or exporting your Chrome Manifest V3 extension bundle? Start here.

---

## 1. Common Questions & Quick Fixes

### Q: A legitimate website layout looks broken or is missing a section. How do I fix it?
1. Tap the **Shield Pill** in the top-left of the `Browser` tab (or tap **Popup Controls**).
2. Either:
   - Lower the **AI Aggressiveness Threshold** slider (e.g., from `0.80` to `0.90`) to make the DOM classifier more conservative, **or**
   - Toggle **Whitelist [domain]** to pause blocking on that specific site.
3. Use **X-Ray View** in the Browser tab to inspect which rule (`NETWORK_RULE`, `AI_DOM_CLASSIFIER`, or `COSMETIC_CSS`) matched the element.

### Q: How do I load the exported Manifest V3 rules into desktop Google Chrome?
1. Open the **Rule Studio -> MV3 Kit** tab in the Android app.
2. Copy or share `manifest.json`, `rules/ads.json`, `content.js`, and `popup.js` into a local `clean-browse/` folder on your computer.
3. Open Chrome and navigate to `chrome://extensions/`.
4. Enable **Developer mode** in the top-right corner and click **Load unpacked**, selecting your `clean-browse/` directory.

### Q: Why did an AI Vision or Rule Generation request fail?
- Ensure your device has an active internet connection for opt-in Gemini cloud features (`AI Rule Generator`, `Visual Screenshot AI`, and `Gemini Semantic Check`).
- Note that Tier 1 Network Blocking and Tier 2 Local 6-Feature DOM Classification work **100% offline** without internet access.

---

## 2. Filing Bug Reports or False-Positive Reports

When opening an issue using [`.github/ISSUE_TEMPLATE.md`](.github/ISSUE_TEMPLATE.md), please include:
* The target webpage URL or domain.
* The active **Blocking Mode** (`Smart AI Hybrid` vs `Standard Rules`) and **Confidence Threshold**.
* The 6-feature vector or rule ID shown in the **Telemetry** interception log.
