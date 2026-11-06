package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BlockingMode(val displayName: String, val subtitle: String) {
    SMART_AI("Smart AI Hybrid", "Rule Engine + 6-Feature Neural DOM Scanner"),
    STANDARD("Standard Rules", "Manifest V3 declarativeNetRequest + CSS Only")
}

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val priority: Int = 1,
    val urlFilter: String,
    val resourceTypes: String, // Comma-separated: script,image,sub_frame,xmlhttprequest,ping
    val category: String,      // "Ad Broker", "Analytics Tracker", "Sponsored Widget", "Social Pixel", "AI Generated"
    val source: String,        // "Built-in MV3", "AI Studio", "Custom"
    val isEnabled: Boolean = true,
    val hitCount: Int = 0
)

@Entity(tableName = "cosmetic_rules")
data class CosmeticRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cssSelector: String,
    val description: String,
    val source: String = "Built-in",
    val isEnabled: Boolean = true
)

@Entity(tableName = "whitelisted_sites")
data class WhitelistedSiteEntity(
    @PrimaryKey val domain: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interception_logs")
data class InterceptionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pageDomain: String,
    val targetOrSelector: String,
    val layer: String,          // "NETWORK_RULE" | "AI_DOM_CLASSIFIER" | "COSMETIC_CSS"
    val resourceType: String,   // "script", "sub_frame", "dom_node", "xmlhttprequest", etc.
    val latencyMs: Double,
    val confidenceScore: Double = 1.0,
    val featureVector: String = "" // e.g. "[1.20, 1, 3, 1, 4, 6]"
)

data class ShieldConfig(
    val isBlockingEnabled: Boolean = true,
    val blockingMode: BlockingMode = BlockingMode.SMART_AI,
    val confidenceThreshold: Float = 0.80f,
    val scanIntervalMs: Int = 250,
    val autoCollapseWhitespace: Boolean = true
)

data class DomElementCandidate(
    val id: String,
    val tagSummary: String,
    val textSnippet: String,
    val widthPx: Int,
    val heightPx: Int,
    val hasIframe: Int,
    val externalLinkCount: Int,
    val hasTrackingKeyword: Int,
    val childNodeCount: Int,
    val depthInTree: Int,
    val localConfidence: Float = 0f,
    val isClassifiedAd: Boolean = false
) {
    val ratio: Float
        get() = if (heightPx > 0) widthPx.toFloat() / heightPx.toFloat() else 1f

    val featureVectorFormatted: String
        get() = String.format(
            java.util.Locale.US,
            "[%.2f, %d, %d, %d, %d, %d]",
            ratio,
            hasIframe,
            externalLinkCount,
            hasTrackingKeyword,
            childNodeCount,
            depthInTree
        )
}

data class GeneratedRuleResult(
    val ruleName: String,
    val urlFilter: String,
    val resourceTypes: List<String>,
    val cssSelector: String,
    val category: String,
    val explanation: String,
    val confidence: Double
)

data class VisualAdFinding(
    val elementTitle: String,
    val adType: String,
    val locationRegion: String,
    val confidence: Double,
    val suggestedCssSelector: String,
    val suggestedUrlPattern: String,
    val reasoning: String
)

data class VisualScanReport(
    val summary: String,
    val overallRiskLevel: String,
    val detectedAds: List<VisualAdFinding>
)

data class SemanticDomReport(
    val classification: String, // "AD" or "CLEAN"
    val confidence: Double,
    val reasoning: String,
    val recommendedCssSelector: String,
    val featureAnalysis: String
)
