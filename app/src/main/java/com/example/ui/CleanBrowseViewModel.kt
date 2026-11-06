package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AdBlockRepository
import com.example.data.BlockingMode
import com.example.data.CleanBrowseAiService
import com.example.data.CosmeticRuleEntity
import com.example.data.DomElementCandidate
import com.example.data.FilterRuleEntity
import com.example.data.GeneratedRuleResult
import com.example.data.InterceptionLogEntity
import com.example.data.SemanticDomReport
import com.example.data.ShieldConfig
import com.example.data.VisualScanReport
import com.example.data.WhitelistedSiteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AiTaskState<out T> {
    data object Idle : AiTaskState<Nothing>
    data object Loading : AiTaskState<Nothing>
    data class Success<T>(val data: T) : AiTaskState<T>
    data class Error(val message: String) : AiTaskState<Nothing>
}

class CleanBrowseViewModel(
    private val repository: AdBlockRepository,
    private val aiService: CleanBrowseAiService = CleanBrowseAiService()
) : ViewModel() {

    val filterRules: StateFlow<List<FilterRuleEntity>> = repository.filterRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cosmeticRules: StateFlow<List<CosmeticRuleEntity>> = repository.cosmeticRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedSites: StateFlow<List<WhitelistedSiteEntity>> = repository.whitelistedSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<InterceptionLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _shieldConfig = MutableStateFlow(ShieldConfig())
    val shieldConfig: StateFlow<ShieldConfig> = _shieldConfig.asStateFlow()

    private val _currentUrl = MutableStateFlow("sandbox://daily-pulse-news")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _injectedDynamicAdCount = MutableStateFlow(0)
    val injectedDynamicAdCount: StateFlow<Int> = _injectedDynamicAdCount.asStateFlow()

    private val _domCandidates = MutableStateFlow<List<DomElementCandidate>>(emptyList())
    val domCandidates: StateFlow<List<DomElementCandidate>> = _domCandidates.asStateFlow()

    private val _selectedDomCandidate = MutableStateFlow<DomElementCandidate?>(null)
    val selectedDomCandidate: StateFlow<DomElementCandidate?> = _selectedDomCandidate.asStateFlow()

    private val _domSemanticState = MutableStateFlow<AiTaskState<SemanticDomReport>>(AiTaskState.Idle)
    val domSemanticState: StateFlow<AiTaskState<SemanticDomReport>> = _domSemanticState.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private val _visualScanState = MutableStateFlow<AiTaskState<VisualScanReport>>(AiTaskState.Idle)
    val visualScanState: StateFlow<AiTaskState<VisualScanReport>> = _visualScanState.asStateFlow()

    private val _ruleGenState = MutableStateFlow<AiTaskState<GeneratedRuleResult>>(AiTaskState.Idle)
    val ruleGenState: StateFlow<AiTaskState<GeneratedRuleResult>> = _ruleGenState.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            refreshDefaultDomCandidates()
        }
    }

    fun dismissBanner() {
        _statusBannerMessage.value = null
    }

    fun toggleBlockingEnabled() {
        _shieldConfig.update { it.copy(isBlockingEnabled = !it.isBlockingEnabled) }
        recalculateDomCandidateScores()
    }

    fun setBlockingMode(mode: BlockingMode) {
        _shieldConfig.update { it.copy(blockingMode = mode) }
        recalculateDomCandidateScores()
    }

    fun setConfidenceThreshold(threshold: Float) {
        _shieldConfig.update { it.copy(confidenceThreshold = threshold.coerceIn(0.50f, 0.95f)) }
        recalculateDomCandidateScores()
    }

    fun navigateToUrl(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return
        val normalized = when {
            trimmed.startsWith("sandbox://") -> trimmed
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
        }
        _currentUrl.value = normalized
    }

    fun loadSandboxPage() {
        _currentUrl.value = "sandbox://daily-pulse-news"
        _injectedDynamicAdCount.value = 0
        refreshDefaultDomCandidates()
    }

    fun injectStealthDynamicAd() {
        val nextIndex = _injectedDynamicAdCount.value + 1
        _injectedDynamicAdCount.value = nextIndex
        val newCandidate = DomElementCandidate(
            id = "stealth_mut_$nextIndex",
            tagSummary = "div.x9f_rand_${100 + nextIndex}[data-slot='native-feed']",
            textSnippet = "Sponsored • Partner Offer #$nextIndex: Upgrade Your Cloud Security Stack Today",
            widthPx = 300,
            heightPx = 250,
            hasIframe = 1,
            externalLinkCount = 3,
            hasTrackingKeyword = 1,
            childNodeCount = 5,
            depthInTree = 6
        )
        val score = repository.scoreDomFeatures(
            newCandidate.ratio,
            newCandidate.hasIframe,
            newCandidate.externalLinkCount,
            newCandidate.hasTrackingKeyword,
            newCandidate.childNodeCount,
            newCandidate.depthInTree
        )
        val config = _shieldConfig.value
        val isBlocked = config.isBlockingEnabled &&
            config.blockingMode == BlockingMode.SMART_AI &&
            score >= config.confidenceThreshold

        val scoredCandidate = newCandidate.copy(
            localConfidence = score,
            isClassifiedAd = isBlocked
        )
        _domCandidates.update { listOf(scoredCandidate) + it }

        if (isBlocked) {
            viewModelScope.launch {
                repository.recordLog(
                    InterceptionLogEntity(
                        pageDomain = currentDomain(),
                        targetOrSelector = scoredCandidate.tagSummary,
                        layer = "AI_DOM_CLASSIFIER",
                        resourceType = "dom_mutation",
                        latencyMs = 3.9,
                        confidenceScore = score.toDouble(),
                        featureVector = scoredCandidate.featureVectorFormatted
                    )
                )
            }
            _statusBannerMessage.value =
                "MutationObserver caught & collapsed stealth ad (${(score * 100).toInt()}% confidence)"
        } else {
            _statusBannerMessage.value =
                "Injected dynamic ad #${nextIndex} (Enable Smart AI mode to auto-collapse)"
        }
    }

    fun currentDomain(): String = repository.extractDomain(_currentUrl.value)

    fun isCurrentSiteWhitelisted(whitelist: List<WhitelistedSiteEntity>): Boolean {
        val domain = currentDomain()
        return whitelist.any { it.domain.equals(domain, ignoreCase = true) }
    }

    fun toggleCurrentSiteWhitelist() {
        val domain = currentDomain()
        val currentlyWhitelisted = isCurrentSiteWhitelisted(whitelistedSites.value)
        viewModelScope.launch {
            repository.toggleWhitelistSite(domain, currentlyWhitelisted)
            _statusBannerMessage.value = if (currentlyWhitelisted) {
                "Removed $domain from whitelist — Shield active"
            } else {
                "Added $domain to per-site whitelist"
            }
        }
    }

    fun removeWhitelistedDomain(domain: String) {
        viewModelScope.launch {
            repository.toggleWhitelistSite(domain, currentlyWhitelisted = true)
        }
    }

    fun evaluateAndLogNetworkRequest(url: String, resourceType: String = "script"): Boolean {
        val config = _shieldConfig.value
        if (!config.isBlockingEnabled) return false
        if (isCurrentSiteWhitelisted(whitelistedSites.value)) return false

        val matchedRule = repository.matchNetworkUrl(url, filterRules.value)
        if (matchedRule != null) {
            viewModelScope.launch {
                repository.incrementRuleHit(matchedRule.id)
                repository.recordLog(
                    InterceptionLogEntity(
                        pageDomain = currentDomain(),
                        targetOrSelector = url.take(90),
                        layer = "NETWORK_RULE",
                        resourceType = resourceType,
                        latencyMs = 0.35,
                        confidenceScore = 1.0,
                        featureVector = "Rule #${matchedRule.id} ${matchedRule.urlFilter}"
                    )
                )
            }
            return true
        }
        return false
    }

    fun recordJsDomInterception(selectorOrTag: String, vectorString: String, score: Float, latencyMs: Float) {
        viewModelScope.launch {
            repository.recordLog(
                InterceptionLogEntity(
                    pageDomain = currentDomain(),
                    targetOrSelector = selectorOrTag,
                    layer = "AI_DOM_CLASSIFIER",
                    resourceType = "dom_node",
                    latencyMs = latencyMs.toDouble(),
                    confidenceScore = score.toDouble(),
                    featureVector = vectorString
                )
            )
        }
    }

    private fun refreshDefaultDomCandidates() {
        val baseList = listOf(
            DomElementCandidate(
                id = "dom_1",
                tagSummary = "div.ad-slot-300x250[data-ad-unit='sidebar_box']",
                textSnippet = "Sponsored • AdChoices • Discover Next-Gen Cloud Storage",
                widthPx = 300,
                heightPx = 250,
                hasIframe = 1,
                externalLinkCount = 3,
                hasTrackingKeyword = 1,
                childNodeCount = 4,
                depthInTree = 6
            ),
            DomElementCandidate(
                id = "dom_2",
                tagSummary = "section.leaderboard-top-728x90",
                textSnippet = "Advertisement • Flash Deal 50% Off Enterprise VPN",
                widthPx = 728,
                heightPx = 90,
                hasIframe = 1,
                externalLinkCount = 1,
                hasTrackingKeyword = 1,
                childNodeCount = 2,
                depthInTree = 4
            ),
            DomElementCandidate(
                id = "dom_3",
                tagSummary = "article.feed-card-obfuscated-x82a",
                textSnippet = "Promoted by Partner Network • 7 Financial Tricks Banks Hide",
                widthPx = 450,
                heightPx = 100,
                hasIframe = 0,
                externalLinkCount = 2,
                hasTrackingKeyword = 1,
                childNodeCount = 5,
                depthInTree = 5
            ),
            DomElementCandidate(
                id = "dom_4",
                tagSummary = "article.editorial-lead-story",
                textSnippet = "Browser Vendors Adopt WebAssembly SIMD for Local Neural Models",
                widthPx = 680,
                heightPx = 420,
                hasIframe = 0,
                externalLinkCount = 0,
                hasTrackingKeyword = 0,
                childNodeCount = 3,
                depthInTree = 3
            ),
            DomElementCandidate(
                id = "dom_5",
                tagSummary = "div.video-player-container-16x9",
                textSnippet = "Keynote Livestream: Privacy-First Web Architecture",
                widthPx = 640,
                heightPx = 360,
                hasIframe = 0,
                externalLinkCount = 0,
                hasTrackingKeyword = 0,
                childNodeCount = 2,
                depthInTree = 7
            )
        )
        _domCandidates.value = baseList
        recalculateDomCandidateScores()
        _selectedDomCandidate.value = _domCandidates.value.firstOrNull()
    }

    private fun recalculateDomCandidateScores() {
        val config = _shieldConfig.value
        _domCandidates.update { list ->
            list.map { item ->
                val score = repository.scoreDomFeatures(
                    ratio = item.ratio,
                    hasIframe = item.hasIframe,
                    externalLinks = item.externalLinkCount,
                    hasKeyword = item.hasTrackingKeyword,
                    childCount = item.childNodeCount,
                    depth = item.depthInTree
                )
                val isAd = config.isBlockingEnabled &&
                    config.blockingMode == BlockingMode.SMART_AI &&
                    score >= config.confidenceThreshold
                item.copy(localConfidence = score, isClassifiedAd = isAd)
            }
        }
        val currentSelectedId = _selectedDomCandidate.value?.id
        if (currentSelectedId != null) {
            _selectedDomCandidate.value = _domCandidates.value.find { it.id == currentSelectedId }
        }
    }

    fun selectDomCandidate(candidate: DomElementCandidate) {
        _selectedDomCandidate.value = candidate
        _domSemanticState.value = AiTaskState.Idle
    }

    fun addCustomDomCandidate(
        tagSummary: String,
        textSnippet: String,
        widthPx: Int,
        heightPx: Int,
        hasIframe: Boolean,
        externalLinks: Int
    ) {
        val lowerText = textSnippet.lowercase()
        val keywords = listOf("sponsored", "promoted", "advertisement", "ad choices", "recommended by", "partner")
        val hasKw = if (keywords.any { lowerText.contains(it) }) 1 else 0
        val candidate = DomElementCandidate(
            id = "custom_${System.currentTimeMillis()}",
            tagSummary = tagSummary.ifBlank { "div.custom-candidate" },
            textSnippet = textSnippet.ifBlank { "Sponsored content unit" },
            widthPx = widthPx.coerceAtLeast(10),
            heightPx = heightPx.coerceAtLeast(10),
            hasIframe = if (hasIframe) 1 else 0,
            externalLinkCount = externalLinks.coerceAtLeast(0),
            hasTrackingKeyword = hasKw,
            childNodeCount = 4,
            depthInTree = 5
        )
        _domCandidates.update { listOf(candidate) + it }
        recalculateDomCandidateScores()
        _selectedDomCandidate.value = _domCandidates.value.first()
    }

    fun runDeepSemanticDomAnalysis(candidate: DomElementCandidate) {
        viewModelScope.launch {
            _domSemanticState.value = AiTaskState.Loading
            val result = aiService.inspectDomElementSemantics(candidate)
            result.fold(
                onSuccess = { report ->
                    _domSemanticState.value = AiTaskState.Success(report)
                },
                onFailure = { error ->
                    _domSemanticState.value = AiTaskState.Error(error.message ?: "Semantic analysis failed.")
                }
            )
        }
    }

    fun onWebpageBitmapCaptured(bitmap: Bitmap, autoRunScan: Boolean = true) {
        _capturedBitmap.value = bitmap
        if (autoRunScan) {
            analyzeCapturedScreenshot(bitmap)
        }
    }

    fun loadSampleAdWebpageBitmapAndScan() {
        val sampleBitmap = createSampleAdPageBitmap()
        _capturedBitmap.value = sampleBitmap
        analyzeCapturedScreenshot(sampleBitmap, "Simulated news webpage with top 728x90 banner, native Sponsored card, and sidebar 300x250 AdChoices box")
    }

    fun analyzeCapturedScreenshot(bitmap: Bitmap, contextNote: String = "") {
        viewModelScope.launch {
            _visualScanState.value = AiTaskState.Loading
            val result = aiService.analyzeWebpageScreenshot(bitmap, contextNote)
            result.fold(
                onSuccess = { report ->
                    _visualScanState.value = AiTaskState.Success(report)
                },
                onFailure = { error ->
                    _visualScanState.value = AiTaskState.Error(error.message ?: "Visual ad scan failed.")
                }
            )
        }
    }

    fun generateRuleFromPrompt(promptText: String) {
        if (promptText.isBlank()) {
            _ruleGenState.value = AiTaskState.Error("Please enter a description, domain, or HTML snippet first.")
            return
        }
        viewModelScope.launch {
            _ruleGenState.value = AiTaskState.Loading
            val result = aiService.generateFilterRule(promptText)
            result.fold(
                onSuccess = { generated ->
                    _ruleGenState.value = AiTaskState.Success(generated)
                },
                onFailure = { error ->
                    _ruleGenState.value = AiTaskState.Error(error.message ?: "Failed to generate rule.")
                }
            )
        }
    }

    fun saveGeneratedRuleToDatabase(generated: GeneratedRuleResult) {
        viewModelScope.launch {
            repository.addFilterRule(
                FilterRuleEntity(
                    priority = 1,
                    urlFilter = generated.urlFilter,
                    resourceTypes = generated.resourceTypes.joinToString(","),
                    category = generated.category,
                    source = "AI Studio",
                    isEnabled = true,
                    hitCount = 0
                )
            )
            if (generated.cssSelector.isNotBlank() && generated.cssSelector != "N/A") {
                repository.addCosmeticRule(
                    CosmeticRuleEntity(
                        cssSelector = generated.cssSelector,
                        description = generated.ruleName,
                        source = "AI Studio",
                        isEnabled = true
                    )
                )
            }
            _statusBannerMessage.value = "Saved ${generated.urlFilter} & ${generated.cssSelector} to active shield!"
        }
    }

    fun addManualFilterRule(urlFilter: String, resourceTypes: String, category: String) {
        if (urlFilter.isBlank()) return
        viewModelScope.launch {
            repository.addFilterRule(
                FilterRuleEntity(
                    priority = 1,
                    urlFilter = urlFilter.trim(),
                    resourceTypes = resourceTypes.ifBlank { "script,image,sub_frame" },
                    category = category.ifBlank { "Custom" },
                    source = "Custom",
                    isEnabled = true
                )
            )
            _statusBannerMessage.value = "Added rule ${urlFilter.trim()}"
        }
    }

    fun addManualCosmeticSelector(selector: String, description: String) {
        if (selector.isBlank()) return
        viewModelScope.launch {
            repository.addCosmeticRule(
                CosmeticRuleEntity(
                    cssSelector = selector.trim(),
                    description = description.ifBlank { "Custom element hiding selector" },
                    source = "Custom",
                    isEnabled = true
                )
            )
            _statusBannerMessage.value = "Added cosmetic selector ${selector.trim()}"
        }
    }

    fun toggleFilterRule(rule: FilterRuleEntity) {
        viewModelScope.launch {
            repository.updateFilterRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteFilterRule(id: Int) {
        viewModelScope.launch {
            repository.deleteFilterRule(id)
        }
    }

    fun toggleCosmeticRule(rule: CosmeticRuleEntity) {
        viewModelScope.launch {
            repository.updateCosmeticRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteCosmeticRule(id: Int) {
        viewModelScope.launch {
            repository.deleteCosmeticRule(id)
        }
    }

    fun clearAllTelemetryLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _statusBannerMessage.value = "Telemetry logs cleared"
        }
    }

    fun getExportedMv3Files(): Map<String, String> {
        return repository.generateManifestV3Files(
            filterRules = filterRules.value,
            cosmeticRules = cosmeticRules.value,
            config = _shieldConfig.value
        )
    }

    private fun createSampleAdPageBitmap(): Bitmap {
        val width = 800
        val height = 1000
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Page background
        paint.color = AndroidColor.parseColor("#F8FAFC")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Top 728x90 Leaderboard Ad Banner
        paint.color = AndroidColor.parseColor("#FEF3C7")
        canvas.drawRoundRect(RectF(36f, 24f, 764f, 114f), 12f, 12f, paint)
        paint.color = AndroidColor.parseColor("#D97706")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("ADVERTISEMENT • 728x90 DOUBLECLICK LEADERBOARD", 60f, 62f, paint)
        paint.textSize = 18f
        paint.isFakeBoldText = false
        canvas.drawText("FLASH SALE: Click Here to Claim 70% Off Cloud HostingNow!", 60f, 94f, paint)

        // Editorial Article Header
        paint.color = AndroidColor.parseColor("#0F172A")
        paint.textSize = 30f
        paint.isFakeBoldText = true
        canvas.drawText("Daily Pulse Tech: Next-Gen Browser Privacy", 36f, 175f, paint)
        paint.color = AndroidColor.parseColor("#475569")
        paint.textSize = 20f
        paint.isFakeBoldText = false
        canvas.drawText("Modern extensions combine Manifest V3 rules with local WASM models.", 36f, 215f, paint)

        // Native In-Feed Sponsored Card
        paint.color = AndroidColor.parseColor("#FEE2E2")
        canvas.drawRoundRect(RectF(36f, 250f, 764f, 430f), 16f, 16f, paint)
        paint.color = AndroidColor.parseColor("#DC2626")
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("SPONSORED • PROMOTED BY TABOOLA NETWORK", 60f, 290f, paint)
        paint.color = AndroidColor.parseColor("#1E293B")
        paint.textSize = 24f
        canvas.drawText("7 Weird Tricks Utility Companies Don't Want You To Know", 60f, 335f, paint)
        paint.color = AndroidColor.parseColor("#2563EB")
        paint.textSize = 20f
        canvas.drawText("[Learn More -> tracker.taboola.com/click]", 60f, 385f, paint)

        // 300x250 Medium Rectangle Box Ad
        paint.color = AndroidColor.parseColor("#E0E7FF")
        canvas.drawRoundRect(RectF(36f, 470f, 440f, 760f), 16f, 16f, paint)
        paint.color = AndroidColor.parseColor("#4338CA")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("AdChoices [i] • 300x250 IAB Box", 60f, 515f, paint)
        paint.textSize = 24f
        canvas.drawText("Download Free PC Speedup Pro!", 60f, 580f, paint)
        paint.color = AndroidColor.parseColor("#16A34A")
        canvas.drawRoundRect(RectF(60f, 630f, 380f, 700f), 12f, 12f, paint)
        paint.color = AndroidColor.WHITE
        canvas.drawText("DOWNLOAD NOW (AD)", 95f, 673f, paint)

        // Bottom Sticky Outbrain Widget Grid
        paint.color = AndroidColor.parseColor("#F1F5F9")
        canvas.drawRoundRect(RectF(36f, 800f, 764f, 960f), 14f, 14f, paint)
        paint.color = AndroidColor.parseColor("#64748B")
        paint.textSize = 19f
        canvas.drawText("Recommended by Outbrain • Paid Partner Links", 60f, 840f, paint)
        canvas.drawText("• Why Everyone Is Switching To This Smart Gadget In 2026", 60f, 885f, paint)
        canvas.drawText("• Celebrity Homes You Won't Believe Exist (Sponsored)", 60f, 925f, paint)

        return bmp
    }

    companion object {
        fun provideFactory(repository: AdBlockRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CleanBrowseViewModel(repository) as T
                }
            }
    }
}
