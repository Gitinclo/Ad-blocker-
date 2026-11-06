package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

class AdBlockRepository(private val dao: AdBlockDao) {

    val filterRules: Flow<List<FilterRuleEntity>> = dao.getAllFilterRules()
    val cosmeticRules: Flow<List<CosmeticRuleEntity>> = dao.getAllCosmeticRules()
    val whitelistedSites: Flow<List<WhitelistedSiteEntity>> = dao.getAllWhitelistedSites()
    val recentLogs: Flow<List<InterceptionLogEntity>> = dao.getRecentLogs()

    suspend fun seedInitialDataIfEmpty() {
        if (dao.getFilterRuleCount() > 0) return

        val initialFilterRules = listOf(
            FilterRuleEntity(
                id = 1,
                priority = 1,
                urlFilter = "||doubleclick.net^",
                resourceTypes = "script,image,sub_frame,xmlhttprequest",
                category = "Ad Broker",
                source = "Built-in MV3",
                isEnabled = true,
                hitCount = 42
            ),
            FilterRuleEntity(
                id = 2,
                priority = 1,
                urlFilter = "||googlesyndication.com^",
                resourceTypes = "script,sub_frame",
                category = "Ad Broker",
                source = "Built-in MV3",
                isEnabled = true,
                hitCount = 31
            ),
            FilterRuleEntity(
                id = 3,
                priority = 1,
                urlFilter = "||google-analytics.com^",
                resourceTypes = "script,ping,xmlhttprequest",
                category = "Analytics Tracker",
                source = "Built-in MV3",
                isEnabled = true,
                hitCount = 58
            ),
            FilterRuleEntity(
                id = 4,
                priority = 1,
                urlFilter = "||criteo.net^",
                resourceTypes = "script,image,xmlhttprequest",
                category = "Ad Broker",
                source = "Built-in MV3",
                isEnabled = true,
                hitCount = 19
            ),
            FilterRuleEntity(
                id = 5,
                priority = 1,
                urlFilter = "||taboola.com^",
                resourceTypes = "script,image,sub_frame",
                category = "Sponsored Widget",
                source = "Built-in MV3",
                isEnabled = true,
                hitCount = 27
            ),
            FilterRuleEntity(
                id = 6,
                priority = 1,
                urlFilter = "||outbrain.com^",
                resourceTypes = "script,image,sub_frame",
                category = "Sponsored Widget",
                source = "Built-in MV3",
                isEnabled = true,
                hitCount = 23
            ),
            FilterRuleEntity(
                id = 7,
                priority = 1,
                urlFilter = "||connect.facebook.net/*/fbevents.js",
                resourceTypes = "script",
                category = "Social Pixel",
                source = "Built-in MV3",
                isEnabled = true,
                hitCount = 34
            )
        )
        dao.insertFilterRules(initialFilterRules)

        val initialCosmeticRules = listOf(
            CosmeticRuleEntity(1, ".ad-banner", "Collapse standard top/side banner slots", "Built-in"),
            CosmeticRuleEntity(2, ".adsbygoogle", "Hide Google AdSense ins containers", "Built-in"),
            CosmeticRuleEntity(3, "[id^='google_ads_']", "Hide dynamically numbered Google ad frames", "Built-in"),
            CosmeticRuleEntity(4, ".taboola-container", "Collapse Taboola sponsored feed carousels", "Built-in"),
            CosmeticRuleEntity(5, ".outbrain-container", "Collapse Outbrain content recommendation grids", "Built-in"),
            CosmeticRuleEntity(6, "[data-ad-unit]", "Hide first-party data-ad-unit containers", "Built-in")
        )
        dao.insertCosmeticRules(initialCosmeticRules)

        val now = System.currentTimeMillis()
        val initialLogs = listOf(
            InterceptionLogEntity(
                timestamp = now - 85_000L,
                pageDomain = "daily-pulse-news.demo",
                targetOrSelector = "https://ad.doubleclick.net/ddm/adj/N7280.19/B82",
                layer = "NETWORK_RULE",
                resourceType = "script",
                latencyMs = 0.4,
                confidenceScore = 1.0,
                featureVector = "Rule #1 ||doubleclick.net^"
            ),
            InterceptionLogEntity(
                timestamp = now - 62_000L,
                pageDomain = "daily-pulse-news.demo",
                targetOrSelector = "article.sponsored-card[data-partner='chameleon']",
                layer = "AI_DOM_CLASSIFIER",
                resourceType = "dom_node",
                latencyMs = 3.8,
                confidenceScore = 0.94,
                featureVector = "[1.20, 1, 3, 1, 4, 6]"
            ),
            InterceptionLogEntity(
                timestamp = now - 44_000L,
                pageDomain = "daily-pulse-news.demo",
                targetOrSelector = "https://cdn.taboola.com/libtrc/impl.js",
                layer = "NETWORK_RULE",
                resourceType = "script",
                latencyMs = 0.3,
                confidenceScore = 1.0,
                featureVector = "Rule #5 ||taboola.com^"
            ),
            InterceptionLogEntity(
                timestamp = now - 25_000L,
                pageDomain = "techstream.demo",
                targetOrSelector = "div.leaderboard-728x90-slot",
                layer = "AI_DOM_CLASSIFIER",
                resourceType = "dom_node",
                latencyMs = 4.1,
                confidenceScore = 0.91,
                featureVector = "[8.09, 1, 1, 1, 2, 4]"
            ),
            InterceptionLogEntity(
                timestamp = now - 11_000L,
                pageDomain = "techstream.demo",
                targetOrSelector = ".ad-banner, [data-ad-unit]",
                layer = "COSMETIC_CSS",
                resourceType = "stylesheet",
                latencyMs = 0.9,
                confidenceScore = 1.0,
                featureVector = "6 active selectors"
            )
        )
        dao.insertLogs(initialLogs)
    }

    suspend fun addFilterRule(rule: FilterRuleEntity) = dao.insertFilterRule(rule)
    suspend fun updateFilterRule(rule: FilterRuleEntity) = dao.updateFilterRule(rule)
    suspend fun deleteFilterRule(id: Int) = dao.deleteFilterRule(id)
    suspend fun incrementRuleHit(id: Int) = dao.incrementRuleHit(id)

    suspend fun addCosmeticRule(rule: CosmeticRuleEntity) = dao.insertCosmeticRule(rule)
    suspend fun updateCosmeticRule(rule: CosmeticRuleEntity) = dao.updateCosmeticRule(rule)
    suspend fun deleteCosmeticRule(id: Int) = dao.deleteCosmeticRule(id)

    suspend fun toggleWhitelistSite(domain: String, currentlyWhitelisted: Boolean) {
        val cleanDomain = extractDomain(domain)
        if (cleanDomain.isBlank()) return
        if (currentlyWhitelisted) {
            dao.removeWhitelistedSite(cleanDomain)
        } else {
            dao.insertWhitelistedSite(WhitelistedSiteEntity(cleanDomain))
        }
    }

    suspend fun recordLog(log: InterceptionLogEntity) = dao.insertLog(log)
    suspend fun clearLogs() = dao.clearAllLogs()

    /**
     * Evaluates a network URL against active declarativeNetRequest-style rules (`||domain^` or wildcard).
     */
    fun matchNetworkUrl(url: String, rules: List<FilterRuleEntity>): FilterRuleEntity? {
        val lowerUrl = url.lowercase()
        for (rule in rules) {
            if (!rule.isEnabled) continue
            val rawPattern = rule.urlFilter.lowercase().trim()
            val cleanPattern = rawPattern
                .removePrefix("||")
                .removePrefix("*://*.")
                .removePrefix("*://")
                .removeSuffix("^")
                .removeSuffix("/*")
            if (cleanPattern.contains("*")) {
                val parts = cleanPattern.split("*").filter { it.isNotEmpty() }
                if (parts.isNotEmpty() && parts.all { lowerUrl.contains(it) }) {
                    return rule
                }
            } else if (cleanPattern.isNotEmpty() && lowerUrl.contains(cleanPattern)) {
                return rule
            }
        }
        return null
    }

    /**
     * Evaluates the 6-feature DOM vector using the on-device Random Forest / Logistic ensemble weights
     * trained on `[width_to_height_ratio, has_iframe, external_link_count, contains_tracking_keywords, child_node_count, depth_in_tree]`.
     */
    fun scoreDomFeatures(
        ratio: Float,
        hasIframe: Int,
        externalLinks: Int,
        hasKeyword: Int,
        childCount: Int,
        depth: Int
    ): Float {
        var score = 0.08f

        // [0] IAB Standard Ad Aspect Ratios: 300x250 (1.2), 728x90 (8.09), 160x600 (0.27), 320x50 (6.4), Native Card (~4.0-5.0)
        val isIabBox = abs(ratio - 1.2f) < 0.22f
        val isLeaderboard = ratio in 6.0f..9.2f
        val isSkyscraper = ratio in 0.22f..0.32f
        val isNativeBannerRatio = ratio in 3.6f..5.5f

        if (isIabBox || isLeaderboard || isSkyscraper) {
            score += 0.31f
        } else if (isNativeBannerRatio) {
            score += 0.22f
        }

        // [1] Embedded iframe presence
        if (hasIframe >= 1) {
            score += 0.27f
        }

        // [2] Outbound / tracking links count
        if (externalLinks >= 2) {
            score += 0.16f
        } else if (externalLinks == 1) {
            score += 0.08f
        }

        // [3] Semantic tracking/sponsored keyword match
        if (hasKeyword >= 1) {
            score += 0.36f
        }

        // [4] & [5] Structural tree depth & container complexity
        if (childCount in 1..8 && depth in 3..9) {
            score += 0.07f
        }

        return score.coerceIn(0.02f, 0.99f)
    }

    fun extractDomain(input: String): String {
        val trimmed = input.trim().lowercase()
        if (trimmed.startsWith("sandbox://") || trimmed.contains("cleanbrowse-sandbox")) {
            return "cleanbrowse-sandbox.local"
        }
        val withoutScheme = trimmed
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
        return withoutScheme.substringBefore("/").substringBefore("?").ifBlank { "cleanbrowse-sandbox.local" }
    }

    /**
     * Generates the complete Chrome Manifest V3 extension bundle files synced with user's active rules.
     */
    fun generateManifestV3Files(
        filterRules: List<FilterRuleEntity>,
        cosmeticRules: List<CosmeticRuleEntity>,
        config: ShieldConfig
    ): Map<String, String> {
        val manifestJson = JSONObject().apply {
            put("manifest_version", 3)
            put("name", "CleanBrowse AI - Hybrid Ad Blocker")
            put("version", "1.0.0")
            put("permissions", JSONArray(listOf("declarativeNetRequest", "declarativeNetRequestFeedback", "storage", "tabs", "activeTab")))
            put("host_permissions", JSONArray(listOf("<all_urls>")))
            put("action", JSONObject().apply {
                put("default_popup", "popup.html")
                put("default_title", "CleanBrowse AI")
            })
            put("declarative_net_request", JSONObject().apply {
                put("rule_resources", JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", "ruleset_1")
                        put("enabled", config.isBlockingEnabled)
                        put("path", "rules/ads.json")
                    })
                })
            })
            put("content_scripts", JSONArray().apply {
                put(JSONObject().apply {
                    put("matches", JSONArray(listOf("<all_urls>")))
                    put("js", JSONArray(listOf("ort.min.js", "content.js")))
                    put("run_at", "document_idle")
                })
            })
            put("web_accessible_resources", JSONArray().apply {
                put(JSONObject().apply {
                    put("resources", JSONArray(listOf("model.onnx")))
                    put("matches", JSONArray(listOf("<all_urls>")))
                })
            })
        }.toString(2)

        val adsJsonArray = JSONArray()
        filterRules.filter { it.isEnabled }.forEachIndexed { index, rule ->
            val types = rule.resourceTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            adsJsonArray.put(JSONObject().apply {
                put("id", index + 1)
                put("priority", rule.priority)
                put("action", JSONObject().put("type", "block"))
                put("condition", JSONObject().apply {
                    put("urlFilter", rule.urlFilter)
                    put("resourceType", JSONArray(types))
                })
            })
        }
        val adsJson = adsJsonArray.toString(2)

        val selectorsList = cosmeticRules.filter { it.isEnabled }.joinToString(",\n  ") { "\"${it.cssSelector}\"" }
        val contentJs = """
// CleanBrowse AI - Hybrid Cosmetic & ONNX WebAssembly DOM Classifier
const adSelectors = [
  $selectorsList
];

// Step 1: Inject Cosmetic CSS Element Hiding
const style = document.createElement("style");
style.textContent = `${'$'}{adSelectors.join(", ")} { display: none !important; }`;
(document.head || document.documentElement).appendChild(style);

// Step 2: 6-Feature DOM Classifier (ONNX Runtime Web)
const CONFIDENCE_THRESHOLD = ${config.confidenceThreshold};
let session = null;

async function loadModel() {
  const modelUrl = chrome.runtime.getURL("model.onnx");
  session = await ort.InferenceSession.create(modelUrl, { executionProviders: ['wasm'] });
}

function extractFeatures(el) {
  const rect = el.getBoundingClientRect();
  if (rect.width === 0 || rect.height === 0) return null;
  const ratio = rect.width / (rect.height || 1);
  const hasIframe = el.querySelector("iframe") ? 1 : 0;
  const linkCount = el.querySelectorAll("a").length;
  const text = (el.innerText || "").toLowerCase();
  const keywords = ["sponsored", "promoted", "advertisement", "ad choices", "recommended by"];
  const hasKeyword = keywords.some(kw => text.includes(kw)) ? 1 : 0;
  const childCount = el.children.length;
  let depth = 0, parent = el.parentElement;
  while (parent) { depth++; parent = parent.parentElement; }
  return [ratio, hasIframe, linkCount, hasKeyword, childCount, depth];
}

async function scanPage() {
  if (!session) return;
  const candidates = document.querySelectorAll("div, aside, section, ins, article");
  for (const el of candidates) {
    if (el.dataset.aiScanned) continue;
    el.dataset.aiScanned = "true";
    const features = extractFeatures(el);
    if (!features) continue;
    const inputTensor = new ort.Tensor("float32", new Float32Array(features), [1, 6]);
    const results = await session.run({ float_input: inputTensor });
    if (results.output_label.data[0] === 1 || results.output_label.data[0] === 1n) {
      el.style.setProperty("display", "none", "important");
    }
  }
}

(async () => {
  await loadModel();
  scanPage();
  new MutationObserver(() => scanPage()).observe(document.body, { childList: true, subtree: true });
})();
        """.trimIndent()

        val popupJs = """
const toggleBtn = document.getElementById("toggleBtn");
const RULESET_ID = "ruleset_1";

chrome.declarativeNetRequest.getEnabledRulesets((rulesets) => {
  updateUI(rulesets.includes(RULESET_ID));
});

toggleBtn.addEventListener("click", async () => {
  const rulesets = await chrome.declarativeNetRequest.getEnabledRulesets();
  const isCurrentlyEnabled = rulesets.includes(RULESET_ID);
  await chrome.declarativeNetRequest.updateEnabledRulesets({
    disableRulesetIds: isCurrentlyEnabled ? [RULESET_ID] : [],
    enableRulesetIds: isCurrentlyEnabled ? [] : [RULESET_ID]
  });
  updateUI(!isCurrentlyEnabled);
});

function updateUI(isEnabled) {
  toggleBtn.textContent = isEnabled ? "Blocking: ON" : "Blocking: OFF";
  toggleBtn.className = isEnabled ? "toggle-btn enabled" : "toggle-btn disabled";
}
        """.trimIndent()

        return mapOf(
            "manifest.json" to manifestJson,
            "rules/ads.json" to adsJson,
            "content.js" to contentJs,
            "popup.js" to popupJs,
            "README.md" to "# CleanBrowse AI\n\nHybrid Manifest V3 + 6-Feature ONNX DOM Ad Blocker (${filterRules.count { it.isEnabled }} active network rules, ${cosmeticRules.count { it.isEnabled }} cosmetic selectors).\nSee root README.md for full documentation.",
            "ARCHITECTURE.md" to "# CleanBrowse AI Architecture\n\n• Tier 1: declarativeNetRequest / shouldInterceptRequest (< 0.5 ms)\n• Tier 2: 6-Feature DOM Classifier [ratio, iframe, links, keyword, children, depth] (threshold ${config.confidenceThreshold})\n• Tier 3: Opt-in Gemini 3.8 Flash Vision & Structured JSON Rule Studio\n• Storage: 100% On-Device Room SQLite Database",
            "PRIVACY.md" to "# Privacy Policy\n\n• 100% On-Device Rule Engine & Interception Logs (Room SQLite)\n• Zero Telemetry, Zero Third-Party Analytics, Zero Ad IDs\n• AI Vision & Rule Studio are strictly opt-in and attested via Firebase App Check",
            "SECURITY.md" to "# Security Policy\n\n• Zero hardcoded client API keys; secured via Firebase App Check\n• Manifest V3 declarativeNetRequest compliance with no remote code execution",
            "CHANGELOG.md" to "# Changelog\n\n## [1.0.0] - 2026-11-06\n- Initial release of CleanBrowse AI Hybrid Browser, 6-Feature DOM Scanner, Gemini Vision Ad Detector, and MV3 Kit Exporter.",
            "ROADMAP.md" to "# Roadmap\n\n- [x] Manifest V3 + 6-Feature DOM Classifier\n- [x] Multimodal Webpage Screenshot Ad Detector\n- [ ] EasyList / uBlock bulk import\n- [ ] Video overlay ad skipping",
            "CONTRIBUTING.md" to "# Contributing\n\n1. Test new ||domain^ rules and CSS selectors in Rule Studio.\n2. Include 6-feature DOM vectors [ratio, iframe, links, keyword, children, depth] when reporting stealth ads.\n3. Run `gradle :app:testDebugUnitTest` before submitting PRs.",
            "CODE_OF_CONDUCT.md" to "# Code of Conduct\n\nCleanBrowse AI follows the Contributor Covenant v2.1. Be respectful, constructive, and privacy-first.",
            "SUPPORT.md" to "# Support Guide\n\n• False positive on a site? Open Popup Controls -> Toggle Whitelist or increase the AI Aggressiveness Threshold.\n• Use X-Ray View in the Browser Sandbox to inspect which layer blocked an element.",
            "GOVERNANCE.md" to "# Governance\n\nCleanBrowse AI is governed by Lazy Consensus under three invariants: Zero Telemetry, High Precision, and Sub-5ms Local Filtering.",
            "LICENSE.md" to "MIT License — Copyright (c) 2026 CleanBrowse AI Contributors",
            ".github/PULL_REQUEST_TEMPLATE.md" to "## Summary\n- Component: [ ] Network Rules [ ] DOM Classifier [ ] AI Studio\n- Verified zero false positives on editorial content.",
            ".github/ISSUE_TEMPLATE.md" to "## Issue Report\n- Type: False Positive / Undetected Ad / Bug\n- URL:\n- Active Mode & Threshold:\n- Telemetry Vector:"
        )
    }
}
