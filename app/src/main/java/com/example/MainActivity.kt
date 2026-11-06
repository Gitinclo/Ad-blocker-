package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RuleFolder
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RuleFolder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AdBlockDatabase
import com.example.data.AdBlockRepository
import com.example.ui.CleanBrowseViewModel
import com.example.ui.screens.AiScannerScreen
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RuleStudioScreen
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.CleanBrowseTheme
import com.example.ui.theme.ShieldEmerald
import com.example.util.configureAppCheck

enum class CleanBrowseDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    BROWSER(
        route = "browser",
        labelRes = R.string.nav_browser,
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public,
        testTag = "nav_browser_tab"
    ),
    AI_SCANNER(
        route = "ai_scanner",
        labelRes = R.string.nav_scanner,
        selectedIcon = Icons.Filled.DocumentScanner,
        unselectedIcon = Icons.Outlined.DocumentScanner,
        testTag = "nav_scanner_tab"
    ),
    RULE_STUDIO(
        route = "rule_studio",
        labelRes = R.string.nav_rules,
        selectedIcon = Icons.Filled.RuleFolder,
        unselectedIcon = Icons.Outlined.RuleFolder,
        testTag = "nav_rules_tab"
    ),
    DASHBOARD(
        route = "dashboard",
        labelRes = R.string.nav_dashboard,
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics,
        testTag = "nav_dashboard_tab"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
            val effectiveDark = isDarkOverride ?: systemDark

            CleanBrowseTheme(darkTheme = effectiveDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CleanBrowseApp(
                        isDarkTheme = effectiveDark,
                        onToggleDarkTheme = { isDarkOverride = !effectiveDark }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanBrowseApp(
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) {
        val db = AdBlockDatabase.getInstance(context)
        AdBlockRepository(db.adBlockDao())
    }
    val viewModel: CleanBrowseViewModel = viewModel(
        factory = CleanBrowseViewModel.provideFactory(repository)
    )

    var currentDestination by rememberSaveable { mutableStateOf(CleanBrowseDestination.BROWSER) }

    // Mandatory BackHandler on secondary destinations
    BackHandler(enabled = currentDestination != CleanBrowseDestination.BROWSER) {
        currentDestination = CleanBrowseDestination.BROWSER
    }

    val shieldConfig by viewModel.shieldConfig.collectAsStateWithLifecycle()
    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val filterRules by viewModel.filterRules.collectAsStateWithLifecycle()
    val cosmeticRules by viewModel.cosmeticRules.collectAsStateWithLifecycle()
    val whitelistedSites by viewModel.whitelistedSites.collectAsStateWithLifecycle()
    val domCandidates by viewModel.domCandidates.collectAsStateWithLifecycle()
    val selectedDomCandidate by viewModel.selectedDomCandidate.collectAsStateWithLifecycle()
    val domSemanticState by viewModel.domSemanticState.collectAsStateWithLifecycle()
    val capturedBitmap by viewModel.capturedBitmap.collectAsStateWithLifecycle()
    val visualScanState by viewModel.visualScanState.collectAsStateWithLifecycle()
    val ruleGenState by viewModel.ruleGenState.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val injectedAdCount by viewModel.injectedDynamicAdCount.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (shieldConfig.isBlockingEnabled) {
                                            ShieldEmerald.copy(alpha = 0.16f)
                                        } else {
                                            AlertCoral.copy(alpha = 0.16f)
                                        }
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (shieldConfig.isBlockingEnabled) ShieldEmerald else AlertCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (shieldConfig.isBlockingEnabled) {
                                        "${shieldConfig.blockingMode.displayName} • ${filterRules.count { it.isEnabled }} Rules"
                                    } else {
                                        "Protection Paused"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleDarkTheme,
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Light or Dark Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar {
                        CleanBrowseDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        CleanBrowseDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Status Toast Banner
                    AnimatedVisibility(visible = statusBannerMessage != null) {
                        statusBannerMessage?.let { msg ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.dismissBanner() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss notification",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentDestination) {
                            CleanBrowseDestination.BROWSER -> {
                                BrowserScreen(
                                    currentUrl = currentUrl,
                                    shieldConfig = shieldConfig,
                                    filterRules = filterRules,
                                    cosmeticRules = cosmeticRules,
                                    whitelistedSites = whitelistedSites,
                                    domCandidates = domCandidates,
                                    recentLogs = recentLogs,
                                    injectedAdCount = injectedAdCount,
                                    onNavigateUrl = viewModel::navigateToUrl,
                                    onLoadSandbox = viewModel::loadSandboxPage,
                                    onInjectDynamicAd = viewModel::injectStealthDynamicAd,
                                    onToggleBlocking = viewModel::toggleBlockingEnabled,
                                    onSelectBlockingMode = viewModel::setBlockingMode,
                                    onUpdateConfidenceThreshold = viewModel::setConfidenceThreshold,
                                    onToggleCurrentSiteWhitelist = viewModel::toggleCurrentSiteWhitelist,
                                    onEvaluateNetworkRequest = viewModel::evaluateAndLogNetworkRequest,
                                    onCaptureWebViewToScanner = { bmp ->
                                        viewModel.onWebpageBitmapCaptured(bmp, autoRunScan = true)
                                        currentDestination = CleanBrowseDestination.AI_SCANNER
                                    },
                                    onInspectDomCandidateInScanner = { candidate ->
                                        viewModel.selectDomCandidate(candidate)
                                        currentDestination = CleanBrowseDestination.AI_SCANNER
                                    }
                                )
                            }
                            CleanBrowseDestination.AI_SCANNER -> {
                                AiScannerScreen(
                                    domCandidates = domCandidates,
                                    selectedDomCandidate = selectedDomCandidate,
                                    domSemanticState = domSemanticState,
                                    capturedBitmap = capturedBitmap,
                                    visualScanState = visualScanState,
                                    confidenceThreshold = shieldConfig.confidenceThreshold,
                                    onSelectDomCandidate = viewModel::selectDomCandidate,
                                    onAddCustomDomCandidate = viewModel::addCustomDomCandidate,
                                    onRunDeepSemanticScan = viewModel::runDeepSemanticDomAnalysis,
                                    onBitmapPicked = { bmp ->
                                        viewModel.onWebpageBitmapCaptured(bmp, autoRunScan = true)
                                    },
                                    onLoadSampleAdBitmapAndScan = viewModel::loadSampleAdWebpageBitmapAndScan,
                                    onAnalyzeBitmap = { bmp ->
                                        viewModel.analyzeCapturedScreenshot(bmp)
                                    },
                                    onSaveDetectedAdAsRule = viewModel::saveGeneratedRuleToDatabase
                                )
                            }
                            CleanBrowseDestination.RULE_STUDIO -> {
                                RuleStudioScreen(
                                    filterRules = filterRules,
                                    cosmeticRules = cosmeticRules,
                                    ruleGenState = ruleGenState,
                                    onGenerateRuleFromPrompt = viewModel::generateRuleFromPrompt,
                                    onSaveGeneratedRule = viewModel::saveGeneratedRuleToDatabase,
                                    onAddManualFilterRule = viewModel::addManualFilterRule,
                                    onToggleFilterRule = viewModel::toggleFilterRule,
                                    onDeleteFilterRule = viewModel::deleteFilterRule,
                                    onAddManualCosmeticRule = viewModel::addManualCosmeticSelector,
                                    onToggleCosmeticRule = viewModel::toggleCosmeticRule,
                                    onDeleteCosmeticRule = viewModel::deleteCosmeticRule,
                                    onGetMv3Files = viewModel::getExportedMv3Files
                                )
                            }
                            CleanBrowseDestination.DASHBOARD -> {
                                DashboardScreen(
                                    filterRules = filterRules,
                                    recentLogs = recentLogs,
                                    whitelistedSites = whitelistedSites,
                                    onRemoveWhitelistedDomain = viewModel::removeWhitelistedDomain,
                                    onClearLogs = viewModel::clearAllTelemetryLogs
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
