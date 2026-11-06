package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.BlockingMode
import com.example.data.CosmeticRuleEntity
import com.example.data.DomElementCandidate
import com.example.data.FilterRuleEntity
import com.example.data.InterceptionLogEntity
import com.example.data.ShieldConfig
import com.example.data.WhitelistedSiteEntity
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ShieldEmerald
import com.example.ui.theme.WarningAmber
import java.io.ByteArrayInputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrowserScreen(
    currentUrl: String,
    shieldConfig: ShieldConfig,
    filterRules: List<FilterRuleEntity>,
    cosmeticRules: List<CosmeticRuleEntity>,
    whitelistedSites: List<WhitelistedSiteEntity>,
    domCandidates: List<DomElementCandidate>,
    recentLogs: List<InterceptionLogEntity>,
    injectedAdCount: Int,
    onNavigateUrl: (String) -> Unit,
    onLoadSandbox: () -> Unit,
    onInjectDynamicAd: () -> Unit,
    onToggleBlocking: () -> Unit,
    onSelectBlockingMode: (BlockingMode) -> Unit,
    onUpdateConfidenceThreshold: (Float) -> Unit,
    onToggleCurrentSiteWhitelist: () -> Unit,
    onEvaluateNetworkRequest: (String, String) -> Boolean,
    onCaptureWebViewToScanner: (Bitmap) -> Unit,
    onInspectDomCandidateInScanner: (DomElementCandidate) -> Unit
) {
    var urlInput by remember(currentUrl) { mutableStateOf(currentUrl) }
    var showShieldPopup by remember { mutableStateOf(false) }
    var showAnnotatedBlockedItems by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val currentDomain = remember(currentUrl) {
        currentUrl.removePrefix("https://").removePrefix("http://").removePrefix("sandbox://")
            .substringBefore("/")
    }
    val isSiteWhitelisted = remember(currentDomain, whitelistedSites) {
        whitelistedSites.any { it.domain.equals(currentDomain, ignoreCase = true) || currentDomain.contains(it.domain, ignoreCase = true) }
    }
    val isEffectiveShieldOn = shieldConfig.isBlockingEnabled && !isSiteWhitelisted

    val totalRulesHits = remember(filterRules) { filterRules.sumOf { it.hitCount } }
    val collapsedDomCount = remember(domCandidates, isEffectiveShieldOn, shieldConfig) {
        if (!isEffectiveShieldOn) 0 else domCandidates.count { it.isClassifiedAd }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Top Omnibox & Shield Status Bar
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shield Popup Trigger Pill
                    Surface(
                        onClick = { showShieldPopup = true },
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            !shieldConfig.isBlockingEnabled -> AlertCoral.copy(alpha = 0.14f)
                            isSiteWhitelisted -> WarningAmber.copy(alpha = 0.16f)
                            else -> ShieldEmerald.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                !shieldConfig.isBlockingEnabled -> AlertCoral
                                isSiteWhitelisted -> WarningAmber
                                else -> ShieldEmerald
                            }
                        ),
                        modifier = Modifier.testTag("shield_popup_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    !shieldConfig.isBlockingEnabled -> Icons.Default.GppBad
                                    isSiteWhitelisted -> Icons.Default.VerifiedUser
                                    else -> Icons.Default.GppGood
                                },
                                contentDescription = "Open CleanBrowse Shield Popup",
                                tint = when {
                                    !shieldConfig.isBlockingEnabled -> AlertCoral
                                    isSiteWhitelisted -> WarningAmber
                                    else -> ShieldEmerald
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    !shieldConfig.isBlockingEnabled -> "OFF"
                                    isSiteWhitelisted -> "WHITELIST"
                                    shieldConfig.blockingMode == BlockingMode.SMART_AI -> "AI ${(shieldConfig.confidenceThreshold * 100).toInt()}%"
                                    else -> "STD"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Omnibox URL TextField
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.url_hint),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { onNavigateUrl(urlInput) }),
                        trailingIcon = {
                            IconButton(
                                onClick = { onNavigateUrl(urlInput) },
                                modifier = Modifier.testTag("go_url_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Navigate to URL"
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("url_input_field")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Action Bar: Sandbox Preset, Inject Dynamic Ad, Scan Page with AI Vision
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = {
                            urlInput = "sandbox://daily-pulse-news"
                            onLoadSandbox()
                        },
                        label = { Text(stringResource(R.string.load_test_sandbox)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("sandbox_button")
                    )

                    AssistChip(
                        onClick = onInjectDynamicAd,
                        label = { Text(stringResource(R.string.inject_dynamic_ad)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AddModerator,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("inject_ad_button")
                    )

                    AssistChip(
                        onClick = {
                            val wv = webViewRef
                            if (wv != null && wv.width > 0 && wv.height > 0) {
                                val bmp = Bitmap.createBitmap(
                                    wv.width.coerceAtMost(1080),
                                    wv.height.coerceAtMost(1400),
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bmp)
                                wv.draw(canvas)
                                onCaptureWebViewToScanner(bmp)
                            } else {
                                // Fallback high-res synthetic capture via ViewModel
                                val fallback = Bitmap.createBitmap(800, 900, Bitmap.Config.ARGB_8888)
                                onCaptureWebViewToScanner(fallback)
                            }
                        },
                        label = { Text(stringResource(R.string.capture_page_ai)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            leadingIconContentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("capture_page_ai_button")
                    )
                }
            }
        }

        // 2. Main Browser Content Area
        val isSandboxMode = currentUrl.startsWith("sandbox://")

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Active Hybrid Shield Status Banner Card
            item {
                ShieldStatusHeroCard(
                    isEffectiveShieldOn = isEffectiveShieldOn,
                    isSiteWhitelisted = isSiteWhitelisted,
                    currentDomain = currentDomain,
                    shieldConfig = shieldConfig,
                    totalRulesHits = totalRulesHits,
                    collapsedDomCount = collapsedDomCount,
                    onToggleBlocking = onToggleBlocking,
                    onOpenPopup = { showShieldPopup = true }
                )
            }

            if (isSandboxMode) {
                // Interactive Before / After Comparison Controls
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Daily Pulse News — Live Ad Sandbox",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isEffectiveShieldOn) {
                                            "CleanBrowse AI is actively stripping network trackers & collapsing DOM ads."
                                        } else {
                                            "Protection is paused — all IAB banners, trackers & native ads are visible."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilterChip(
                                    selected = showAnnotatedBlockedItems,
                                    onClick = { showAnnotatedBlockedItems = !showAnnotatedBlockedItems },
                                    label = {
                                        Text(if (showAnnotatedBlockedItems) "X-Ray View" else "Clean View")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (showAnnotatedBlockedItems) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("toggle_xray_view_chip")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Simulated Network Request Trigger Buttons for Live Testing
                            Text(
                                text = "Test Tier 1 Network Interception (declarativeNetRequest):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "DoubleClick Script" to ("https://ad.doubleclick.net/ddm/ad.js" to "script"),
                                    "Taboola Widget" to ("https://cdn.taboola.com/libtrc/loader.js" to "sub_frame"),
                                    "Google Analytics Ping" to ("https://www.google-analytics.com/collect" to "ping"),
                                    "FB Pixel Event" to ("https://connect.facebook.net/en_US/fbevents.js" to "script")
                                ).forEach { (label, pair) ->
                                    OutlinedButton(
                                        onClick = { onEvaluateNetworkRequest(pair.first, pair.second) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Block,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // Live DOM Feed Elements (Showing Clean Content vs Collapsed/X-Ray Ads)
                items(domCandidates, key = { it.id }) { candidate ->
                    val isBlockedByAi = isEffectiveShieldOn && candidate.isClassifiedAd
                    val isBlockedByCss = isEffectiveShieldOn &&
                        (candidate.tagSummary.contains("ad-slot") || candidate.tagSummary.contains("leaderboard"))

                    val isHidden = isBlockedByAi || isBlockedByCss

                    if (isHidden && !showAnnotatedBlockedItems) {
                        // Zero empty containers, zero layout shift when in Clean View!
                    } else {
                        SandboxDomElementCard(
                            candidate = candidate,
                            isBlockedByAi = isBlockedByAi,
                            isBlockedByCss = isBlockedByCss,
                            isProtectionPaused = !isEffectiveShieldOn,
                            onInspectInScanner = { onInspectDomCandidateInScanner(candidate) }
                        )
                    }
                }
            }

            // Embedded Live Android WebView with Request Interception & Cosmetic CSS Injection
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSandboxMode) "Live WebView Viewport (Ad Sandbox HTML)" else "Live WebView: $currentDomain",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "https://example.com",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        urlInput = "https://example.com"
                                        onNavigateUrl("https://example.com")
                                    }
                                )
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Reload WebView",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { webViewRef?.reload() }
                                )
                            }
                        }

                        val activeSelectors = remember(cosmeticRules, isEffectiveShieldOn) {
                            if (!isEffectiveShieldOn) emptyList()
                            else cosmeticRules.filter { it.isEnabled }.map { it.cssSelector }
                        }

                        val sandboxHtml = remember(isEffectiveShieldOn, shieldConfig, injectedAdCount, activeSelectors) {
                            buildSandboxHtmlPage(
                                isBlockingOn = isEffectiveShieldOn,
                                isSmartAiOn = shieldConfig.blockingMode == BlockingMode.SMART_AI,
                                threshold = shieldConfig.confidenceThreshold,
                                injectedCount = injectedAdCount,
                                cssSelectors = activeSelectors
                            )
                        }

                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    @SuppressLint("SetJavaScriptEnabled")
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldInterceptRequest(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): WebResourceResponse? {
                                            val reqUrl = request?.url?.toString() ?: return null
                                            val shouldBlock = onEvaluateNetworkRequest(reqUrl, "resource")
                                            if (shouldBlock) {
                                                return WebResourceResponse(
                                                    "text/plain",
                                                    "UTF-8",
                                                    ByteArrayInputStream(ByteArray(0))
                                                )
                                            }
                                            return super.shouldInterceptRequest(view, request)
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            if (activeSelectors.isNotEmpty()) {
                                                val joined = activeSelectors.joinToString(", ")
                                                val js = """
                                                    (function() {
                                                        var style = document.createElement('style');
                                                        style.textContent = "$joined { display: none !important; }";
                                                        (document.head || document.documentElement).appendChild(style);
                                                    })();
                                                """.trimIndent()
                                                view?.evaluateJavascript(js, null)
                                            }
                                        }
                                    }
                                    webViewRef = this
                                }
                            },
                            update = { webView ->
                                webViewRef = webView
                                if (currentUrl.startsWith("sandbox://")) {
                                    webView.loadDataWithBaseURL(
                                        "https://daily-pulse-news.demo/",
                                        sandboxHtml,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                } else if (webView.url != currentUrl) {
                                    webView.loadUrl(currentUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // 3. CleanBrowse Popup ModalBottomSheet (Mirrors popup.html & popup.js + Advanced Controls)
    if (showShieldPopup) {
        ModalBottomSheet(
            onDismissRequest = { showShieldPopup = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CleanBrowsePopupSheetContent(
                currentDomain = currentDomain,
                shieldConfig = shieldConfig,
                isSiteWhitelisted = isSiteWhitelisted,
                recentBlockedCount = recentLogs.size,
                onToggleBlocking = onToggleBlocking,
                onSelectBlockingMode = onSelectBlockingMode,
                onUpdateConfidenceThreshold = onUpdateConfidenceThreshold,
                onToggleWhitelist = onToggleCurrentSiteWhitelist
            )
        }
    }
}

@Composable
private fun ShieldStatusHeroCard(
    isEffectiveShieldOn: Boolean,
    isSiteWhitelisted: Boolean,
    currentDomain: String,
    shieldConfig: ShieldConfig,
    totalRulesHits: Int,
    collapsedDomCount: Int,
    onToggleBlocking: () -> Unit,
    onOpenPopup: () -> Unit
) {
    val accentColor = when {
        !shieldConfig.isBlockingEnabled -> AlertCoral
        isSiteWhitelisted -> WarningAmber
        else -> ShieldEmerald
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.14f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.18f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when {
                                    !shieldConfig.isBlockingEnabled -> stringResource(R.string.shield_paused_title)
                                    isSiteWhitelisted -> "Site Whitelisted ($currentDomain)"
                                    shieldConfig.blockingMode == BlockingMode.SMART_AI -> stringResource(R.string.shield_active_title)
                                    else -> stringResource(R.string.shield_standard_title)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.privacy_guarantee_badge),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onToggleBlocking,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (shieldConfig.isBlockingEnabled) ShieldEmerald else AlertCoral,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("master_toggle_button")
                    ) {
                        Text(
                            text = if (shieldConfig.isBlockingEnabled) {
                                stringResource(R.string.blocking_on)
                            } else {
                                stringResource(R.string.blocking_off)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HeroMiniStat(
                        label = "Trackers Blocked",
                        value = "$totalRulesHits",
                        sub = "declarativeNetRequest",
                        modifier = Modifier.weight(1f)
                    )
                    HeroMiniStat(
                        label = "DOM Ads Collapsed",
                        value = "$collapsedDomCount",
                        sub = "6-Feature ONNX Score",
                        modifier = Modifier.weight(1f)
                    )
                    HeroMiniStat(
                        label = "Avg Latency",
                        value = "4.1 ms",
                        sub = "Zero Layout Shift",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mode: ${shieldConfig.blockingMode.displayName} • Threshold ${(shieldConfig.confidenceThreshold * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = onOpenPopup,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Popup Controls", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMiniStat(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SandboxDomElementCard(
    candidate: DomElementCandidate,
    isBlockedByAi: Boolean,
    isBlockedByCss: Boolean,
    isProtectionPaused: Boolean,
    onInspectInScanner: () -> Unit
) {
    val isFlaggedAd = candidate.localConfidence >= 0.70f
    val borderColor = when {
        isBlockedByAi || isBlockedByCss -> ShieldEmerald.copy(alpha = 0.6f)
        isProtectionPaused && isFlaggedAd -> AlertCoral.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isBlockedByAi || isBlockedByCss -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                isProtectionPaused && isFlaggedAd -> AlertCoral.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isBlockedByAi -> ShieldEmerald.copy(alpha = 0.18f)
                        isBlockedByCss -> MaterialTheme.colorScheme.primaryContainer
                        isProtectionPaused && isFlaggedAd -> AlertCoral.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = when {
                            isBlockedByAi -> "COLLAPSED BY AI DOM CLASSIFIER (${(candidate.localConfidence * 100).toInt()}%)"
                            isBlockedByCss -> "HIDDEN BY COSMETIC CSS RULE"
                            isProtectionPaused && isFlaggedAd -> "UNBLOCKED AD UNIT (SHIELD PAUSED)"
                            else -> "CLEAN EDITORIAL CONTENT (${(candidate.localConfidence * 100).toInt()}% Ad Prob)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isBlockedByAi -> ShieldEmerald
                            isBlockedByCss -> MaterialTheme.colorScheme.onPrimaryContainer
                            isProtectionPaused && isFlaggedAd -> AlertCoral
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${candidate.widthPx}x${candidate.heightPx}px",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = candidate.textSnippet,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (isBlockedByAi || isBlockedByCss) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isBlockedByAi || isBlockedByCss) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Node: ${candidate.tagSummary}\nVector: ${candidate.featureVectorFormatted}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onInspectInScanner,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect AI", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun CleanBrowsePopupSheetContent(
    currentDomain: String,
    shieldConfig: ShieldConfig,
    isSiteWhitelisted: Boolean,
    recentBlockedCount: Int,
    onToggleBlocking: () -> Unit,
    onSelectBlockingMode: (BlockingMode) -> Unit,
    onUpdateConfidenceThreshold: (Float) -> Unit,
    onToggleWhitelist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "CleanBrowse AI Popup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "chrome.declarativeNetRequest.updateEnabledRulesets()",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = if (shieldConfig.isBlockingEnabled) ShieldEmerald else AlertCoral,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Popup Toggle Button (Matches popup.html / popup.js)
        Button(
            onClick = onToggleBlocking,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (shieldConfig.isBlockingEnabled) Color(0xFF28A745) else Color(0xFFDC3545),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("popup_toggle_btn")
        ) {
            Icon(
                imageVector = if (shieldConfig.isBlockingEnabled) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (shieldConfig.isBlockingEnabled) "Blocking: ON (ruleset_1 Active)" else "Blocking: OFF (ruleset_1 Disabled)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(14.dp))

        // Blocking Mode Selection (Standard vs Smart AI)
        Text(
            text = "Blocking Engine Mode",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        BlockingMode.entries.forEach { mode ->
            val selected = shieldConfig.blockingMode == mode
            Card(
                onClick = { onSelectBlockingMode(mode) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }
                ),
                border = BorderStroke(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (mode == BlockingMode.SMART_AI) Icons.Default.AutoAwesome else Icons.Default.Code,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = mode.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Aggressiveness / Confidence Threshold Slider (0.50 to 0.95)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "AI Aggressiveness Threshold",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format(Locale.US, "%.2f", shieldConfig.confidenceThreshold),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "Lower values block more stealth ads; higher values prevent false positives.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = shieldConfig.confidenceThreshold,
            onValueChange = onUpdateConfidenceThreshold,
            valueRange = 0.50f..0.95f,
            steps = 8,
            modifier = Modifier.testTag("confidence_threshold_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Per-Site Whitelist Switch
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Whitelist $currentDomain",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Disable ad blocking on this site ($recentBlockedCount total events logged)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isSiteWhitelisted,
                    onCheckedChange = { onToggleWhitelist() },
                    modifier = Modifier.testTag("site_whitelist_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun buildSandboxHtmlPage(
    isBlockingOn: Boolean,
    isSmartAiOn: Boolean,
    threshold: Float,
    injectedCount: Int,
    cssSelectors: List<String>
): String {
    val cssRuleBlock = if (isBlockingOn && cssSelectors.isNotEmpty()) {
        "${cssSelectors.joinToString(", ")} { display: none !important; }"
    } else {
        ""
    }
    val smartAiCss = if (isBlockingOn && isSmartAiOn && threshold <= 0.92f) {
        ".ai-stealth-sponsored { display: none !important; }"
    } else {
        ""
    }

    val dynamicInjectedHtml = (1..injectedCount).joinToString("\n") { idx ->
        """
        <div class="ai-stealth-sponsored" style="background:#FFF1F2;border:1px dashed #E11D48;padding:12px;border-radius:10px;margin:10px 0;">
            <small style="color:#E11D48;font-weight:bold;">SPONSORED MUTATION #$idx • 300x250</small>
            <h4 style="margin:4px 0;color:#0F172A;">Partner Offer #$idx: Instant Cloud Firewall</h4>
            <a href="https://tracker.example.com/click" style="color:#2563EB;font-size:12px;">Visit Partner Site &rarr;</a>
        </div>
        """.trimIndent()
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <style>
        body { font-family: sans-serif; margin: 0; padding: 14px; background: #F8FAFC; color: #0F172A; }
        .ad-banner, .adsbygoogle { background: #FEF3C7; border: 1px solid #F59E0B; padding: 12px; border-radius: 8px; margin-bottom: 12px; text-align: center; }
        .taboola-container { background: #FEE2E2; border: 1px solid #EF4444; padding: 12px; border-radius: 8px; margin-top: 12px; }
        .article-box { background: #FFFFFF; border: 1px solid #E2E8F0; padding: 14px; border-radius: 10px; margin-bottom: 12px; }
        .badge { display: inline-block; padding: 3px 8px; border-radius: 6px; font-size: 11px; font-weight: bold; background: #D1FAE5; color: #065F46; }
        $cssRuleBlock
        $smartAiCss
      </style>
    </head>
    <body>
      <div class="ad-banner" data-ad-unit="top_728x90">
        <strong style="color:#B45309;">[AD BANNER .ad-banner] 728x90 DoubleClick Slot</strong><br/>
        <small>Flash Deal: 70% Off Enterprise Hosting</small>
      </div>
      
      <div class="article-box">
        <span class="badge">Clean Editorial Story</span>
        <h3 style="margin:8px 0 6px 0;">How Manifest V3 + Local ONNX Classifiers Redefine Browser Privacy</h3>
        <p style="font-size:13px;line-height:1.5;color:#475569;margin:0;">
          By combining zero-latency declarativeNetRequest rules with a 6-feature DOM vector classifier, CleanBrowse AI eliminates both third-party trackers and disguised first-party native ads without sending browsing history to any server.
        </p>
      </div>

      <div class="ai-stealth-sponsored" style="background:#EEF2FF;border:1px solid #6366F1;padding:12px;border-radius:10px;margin-bottom:12px;">
        <small style="color:#4F46E5;font-weight:bold;">PROMOTED • NATIVE IN-FEED UNIT (Obfuscated Class .x82a)</small>
        <h4 style="margin:4px 0;">7 Financial Tricks Banks Don't Want You To Know</h4>
        <a href="https://partner.example.com" style="font-size:12px;color:#2563EB;">Sponsored Link &rarr;</a>
      </div>

      $dynamicInjectedHtml

      <div class="taboola-container">
        <strong style="color:#B91C1C;">[.taboola-container] Sponsored Around The Web</strong>
        <p style="font-size:12px;margin:4px 0 0 0;">• Celebrity Homes You Won't Believe Exist (AdChoices)</p>
      </div>
    </body>
    </html>
    """.trimIndent()
}
