package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DomElementCandidate
import com.example.data.GeneratedRuleResult
import com.example.data.SemanticDomReport
import com.example.data.VisualAdFinding
import com.example.data.VisualScanReport
import com.example.ui.AiTaskState
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ShieldEmerald
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AiScannerScreen(
    domCandidates: List<DomElementCandidate>,
    selectedDomCandidate: DomElementCandidate?,
    domSemanticState: AiTaskState<SemanticDomReport>,
    capturedBitmap: Bitmap?,
    visualScanState: AiTaskState<VisualScanReport>,
    confidenceThreshold: Float,
    onSelectDomCandidate: (DomElementCandidate) -> Unit,
    onAddCustomDomCandidate: (String, String, Int, Int, Boolean, Int) -> Unit,
    onRunDeepSemanticScan: (DomElementCandidate) -> Unit,
    onBitmapPicked: (Bitmap) -> Unit,
    onLoadSampleAdBitmapAndScan: () -> Unit,
    onAnalyzeBitmap: (Bitmap) -> Unit,
    onSaveDetectedAdAsRule: (GeneratedRuleResult) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // Zero-permission Android Photo Picker per Google Play policy guidelines
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val rawBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onBitmapPicked(rawBitmap)
            } catch (_: Exception) {
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PrimaryTabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("6-Feature DOM Scanner") },
                icon = { Icon(Icons.Default.Memory, contentDescription = null) },
                modifier = Modifier.testTag("subtab_dom_scanner")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Visual Screenshot AI") },
                icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null) },
                modifier = Modifier.testTag("subtab_visual_scanner")
            )
        }

        if (selectedSubTab == 0) {
            DomFeatureClassifierTab(
                domCandidates = domCandidates,
                selectedCandidate = selectedDomCandidate ?: domCandidates.firstOrNull(),
                domSemanticState = domSemanticState,
                confidenceThreshold = confidenceThreshold,
                onSelectCandidate = onSelectDomCandidate,
                onAddCustomCandidate = onAddCustomDomCandidate,
                onRunDeepSemanticScan = onRunDeepSemanticScan,
                onSaveSelectorRule = { selector, reason ->
                    onSaveDetectedAdAsRule(
                        GeneratedRuleResult(
                            ruleName = "AI DOM Semantic Rule",
                            urlFilter = "||sponsored-cdn.example.com^",
                            resourceTypes = listOf("script", "sub_frame"),
                            cssSelector = selector,
                            category = "AI Generated",
                            explanation = reason,
                            confidence = 0.94
                        )
                    )
                }
            )
        } else {
            VisualScreenshotDetectorTab(
                capturedBitmap = capturedBitmap,
                visualScanState = visualScanState,
                onPickScreenshot = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onLoadSampleBitmap = onLoadSampleAdBitmapAndScan,
                onReanalyzeBitmap = onAnalyzeBitmap,
                onBlockVisualFinding = { finding ->
                    onSaveDetectedAdAsRule(
                        GeneratedRuleResult(
                            ruleName = finding.elementTitle,
                            urlFilter = finding.suggestedUrlPattern.ifBlank { "||doubleclick.net^" },
                            resourceTypes = listOf("script", "image", "sub_frame"),
                            cssSelector = finding.suggestedCssSelector,
                            category = "AI Generated",
                            explanation = finding.reasoning,
                            confidence = finding.confidence
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DomFeatureClassifierTab(
    domCandidates: List<DomElementCandidate>,
    selectedCandidate: DomElementCandidate?,
    domSemanticState: AiTaskState<SemanticDomReport>,
    confidenceThreshold: Float,
    onSelectCandidate: (DomElementCandidate) -> Unit,
    onAddCustomCandidate: (String, String, Int, Int, Boolean, Int) -> Unit,
    onRunDeepSemanticScan: (DomElementCandidate) -> Unit,
    onSaveSelectorRule: (String, String) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var customTag by remember { mutableStateOf("div.native-promo-box") }
    var customText by remember { mutableStateOf("Sponsored • Recommended by Partner") }
    var customWidth by remember { mutableStateOf("300") }
    var customHeight by remember { mutableStateOf("250") }
    var customHasIframe by remember { mutableStateOf(true) }
    var customLinks by remember { mutableStateOf("2") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Formula Reference Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ONNX 6-Feature DOM Vector Classifier",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { showAddForm = !showAddForm },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("toggle_custom_dom_form_btn")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showAddForm) "Close Test Form" else "Test Custom DOM Node", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vector = [width_to_height_ratio, has_iframe, external_link_count, contains_tracking_keywords, child_node_count, depth_in_tree]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (showAddForm) {
            item {
                Card(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Simulate Candidate DOM Element",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = customTag,
                            onValueChange = { customTag = it },
                            label = { Text("Tag & CSS Classes") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            label = { Text("Inner Text Content") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customWidth,
                                onValueChange = { customWidth = it },
                                label = { Text("Width (px)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customHeight,
                                onValueChange = { customHeight = it },
                                label = { Text("Height (px)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customLinks,
                                onValueChange = { customLinks = it },
                                label = { Text("Links <a>") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = customHasIframe,
                                    onCheckedChange = { customHasIframe = it }
                                )
                                Text("Contains <iframe>", style = MaterialTheme.typography.bodySmall)
                            }
                            Button(
                                onClick = {
                                    onAddCustomCandidate(
                                        customTag,
                                        customText,
                                        customWidth.toIntOrNull() ?: 300,
                                        customHeight.toIntOrNull() ?: 250,
                                        customHasIframe,
                                        customLinks.toIntOrNull() ?: 2
                                    )
                                    showAddForm = false
                                }
                            ) {
                                Text("Extract & Score Node")
                            }
                        }
                    }
                }
            }
        }

        // Selected Candidate Detail & Deep Gemini Semantic Analyzer
        if (selectedCandidate != null) {
            item {
                SelectedDomCandidateCard(
                    candidate = selectedCandidate,
                    confidenceThreshold = confidenceThreshold,
                    domSemanticState = domSemanticState,
                    onRunDeepSemanticScan = { onRunDeepSemanticScan(selectedCandidate) },
                    onSaveSelectorRule = onSaveSelectorRule
                )
            }
        }

        item {
            Text(
                text = "Scanned Page DOM Candidates (${domCandidates.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(domCandidates, key = { it.id }) { candidate ->
            val isSelected = selectedCandidate?.id == candidate.id
            Card(
                onClick = { onSelectCandidate(candidate) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = candidate.tagSummary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = candidate.textSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "Vector: ${candidate.featureVectorFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (candidate.localConfidence >= confidenceThreshold) {
                            AlertCoral.copy(alpha = 0.16f)
                        } else {
                            ShieldEmerald.copy(alpha = 0.16f)
                        }
                    ) {
                        Text(
                            text = if (candidate.localConfidence >= confidenceThreshold) {
                                "AD ${(candidate.localConfidence * 100).toInt()}%"
                            } else {
                                "CLEAN ${(candidate.localConfidence * 100).toInt()}%"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (candidate.localConfidence >= confidenceThreshold) AlertCoral else ShieldEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDomCandidateCard(
    candidate: DomElementCandidate,
    confidenceThreshold: Float,
    domSemanticState: AiTaskState<SemanticDomReport>,
    onRunDeepSemanticScan: () -> Unit,
    onSaveSelectorRule: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
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
                        text = "Active Node Inspector",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = candidate.tagSummary,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onRunDeepSemanticScan,
                    modifier = Modifier.testTag("run_deep_dom_ai_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gemini Semantic Check")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6-Feature Breakdown Grid
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "float_input Tensor [1, 6]: ${candidate.featureVectorFormatted}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "[0] Ratio: ${String.format(Locale.US, "%.2f", candidate.ratio)} (${candidate.widthPx}x${candidate.heightPx})  •  [1] Iframe: ${candidate.hasIframe}  •  [2] Links: ${candidate.externalLinkCount}\n[3] Tracking Keyword: ${candidate.hasTrackingKeyword}  •  [4] Children: ${candidate.childNodeCount}  •  [5] Tree Depth: ${candidate.depthInTree}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { candidate.localConfidence },
                        color = if (candidate.localConfidence >= confidenceThreshold) AlertCoral else ShieldEmerald,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Local Classifier Score: ${(candidate.localConfidence * 100).toInt()}% (Threshold: ${(confidenceThreshold * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Gemini Semantic Inspection Result
            when (domSemanticState) {
                is AiTaskState.Idle -> {}
                is AiTaskState.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Running Gemini semantic & structural DOM analysis…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is AiTaskState.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AlertCoral.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AlertCoral)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = domSemanticState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = AlertCoral
                            )
                        }
                    }
                }
                is AiTaskState.Success -> {
                    val report = domSemanticState.data
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Gemini Verdict: ${report.classification} (${(report.confidence * 100).toInt()}%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (report.classification == "AD") AlertCoral else ShieldEmerald
                        )
                        if (report.recommendedCssSelector.isNotBlank() && report.recommendedCssSelector != "N/A") {
                            FilledTonalButton(
                                onClick = { onSaveSelectorRule(report.recommendedCssSelector, report.reasoning) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Add ${report.recommendedCssSelector}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = report.reasoning, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.featureAnalysis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualScreenshotDetectorTab(
    capturedBitmap: Bitmap?,
    visualScanState: AiTaskState<VisualScanReport>,
    onPickScreenshot: () -> Unit,
    onLoadSampleBitmap: () -> Unit,
    onReanalyzeBitmap: (Bitmap) -> Unit,
    onBlockVisualFinding: (VisualAdFinding) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Perceptual Webpage Screenshot Ad Detector",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Detects visually disguised native ads, IAB banners, AdChoices icons, and dark-pattern download buttons immune to DOM obfuscation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onPickScreenshot,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_screenshot_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.pick_screenshot))
                        }
                        FilledTonalButton(
                            onClick = onLoadSampleBitmap,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sample_ad_bitmap_button")
                        ) {
                            Icon(Icons.Default.ImageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Demo Page")
                        }
                    }
                }
            }
        }

        // Screenshot Preview Card
        item {
            if (capturedBitmap != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Image(
                            bitmap = capturedBitmap.asImageBitmap(),
                            contentDescription = "Captured Webpage Screenshot for AI Ad Detection",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Resolution: ${capturedBitmap.width}x${capturedBitmap.height}px",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { onReanalyzeBitmap(capturedBitmap) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-Run Vision Scan", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else {
                // Hero visual banner when no screenshot is loaded yet
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.img_shield_banner_1793973736199),
                            contentDescription = "CleanBrowse AI Visual Shield Illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        )
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Ready for Multimodal Perceptual Analysis",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap 'Scan Demo Page' above, pick a screenshot from your device, or use 'Scan Page with AI Vision' from the Browser tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Visual Scan State Results
        when (visualScanState) {
            is AiTaskState.Idle -> {}
            is AiTaskState.Loading -> {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(18.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Analyzing Webpage Screenshot with Gemini Vision…",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Detecting IAB aspect ratios, Sponsored badges, and widget grids.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            is AiTaskState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AlertCoral.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, AlertCoral),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AlertCoral)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = visualScanState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AlertCoral
                            )
                        }
                    }
                }
            }
            is AiTaskState.Success -> {
                val report = visualScanState.data
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Intrusion Rating: ${report.overallRiskLevel}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${report.detectedAds.size} Ad Regions Found",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = report.summary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                items(report.detectedAds) { finding ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = finding.elementTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${finding.adType} • ${finding.locationRegion}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AlertCoral.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${(finding.confidence * 100).toInt()}% Match",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertCoral,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = finding.reasoning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CSS: ${finding.suggestedCssSelector}\nRule: ${finding.suggestedUrlPattern}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                FilledTonalButton(
                                    onClick = { onBlockVisualFinding(finding) }
                                ) {
                                    Icon(Icons.Default.SecurityUpdateGood, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Block Pattern")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
