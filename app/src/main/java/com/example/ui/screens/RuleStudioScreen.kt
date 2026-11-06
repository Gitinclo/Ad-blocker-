package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Css
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CosmeticRuleEntity
import com.example.data.FilterRuleEntity
import com.example.data.GeneratedRuleResult
import com.example.ui.AiTaskState
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ShieldEmerald

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RuleStudioScreen(
    filterRules: List<FilterRuleEntity>,
    cosmeticRules: List<CosmeticRuleEntity>,
    ruleGenState: AiTaskState<GeneratedRuleResult>,
    onGenerateRuleFromPrompt: (String) -> Unit,
    onSaveGeneratedRule: (GeneratedRuleResult) -> Unit,
    onAddManualFilterRule: (String, String, String) -> Unit,
    onToggleFilterRule: (FilterRuleEntity) -> Unit,
    onDeleteFilterRule: (Int) -> Unit,
    onAddManualCosmeticRule: (String, String) -> Unit,
    onToggleCosmeticRule: (CosmeticRuleEntity) -> Unit,
    onDeleteCosmeticRule: (Int) -> Unit,
    onGetMv3Files: () -> Map<String, String>
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var aiPrompt by remember { mutableStateOf("") }

    var showManualDialog by remember { mutableStateOf(false) }
    var manualPattern by remember { mutableStateOf("||ads.example.net^") }
    var manualTypes by remember { mutableStateOf("script,image,sub_frame") }
    var manualCssSelector by remember { mutableStateOf(".sponsored-banner-box") }
    var manualCssDesc by remember { mutableStateOf("Hide custom sponsored banner container") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Network (${filterRules.size})") },
                icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                modifier = Modifier.testTag("tab_network_rules")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Cosmetic (${cosmeticRules.size})") },
                icon = { Icon(Icons.Default.Css, contentDescription = null) },
                modifier = Modifier.testTag("tab_cosmetic_rules")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("MV3 Kit") },
                icon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                modifier = Modifier.testTag("tab_mv3_kit")
            )
        }

        if (selectedTab == 2) {
            ManifestV3KitExporterView(mv3Files = onGetMv3Files())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. AI Filter Rule & CSS Selector Generator Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Filter Rule & Selector Generator",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Describe an ad server, tracker domain, or paste an HTML snippet to generate Manifest V3 declarativeNetRequest + CSS rules.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Prompt Presets
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Block RevContent & MGID widgets" to "Block native ad widgets from revcontent.com and hide .rc-wc-container elements",
                                    "Block TikTok & Snap Pixels" to "Block tracking scripts from analytics.tiktok.com and sc-static.net",
                                    "Collapse Sticky Video Overlays" to "Block video ad serverimasdk.googleapis.com and collapse div.sticky-video-ad-overlay"
                                ).forEach { (chipLabel, fullPrompt) ->
                                    AssistChip(
                                        onClick = {
                                            aiPrompt = fullPrompt
                                            onGenerateRuleFromPrompt(fullPrompt)
                                        },
                                        label = { Text(chipLabel, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = aiPrompt,
                                onValueChange = { aiPrompt = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.ai_rule_prompt_hint),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_rule_prompt_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { showManualDialog = !showManualDialog },
                                    modifier = Modifier.testTag("toggle_manual_rule_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (showManualDialog) "Hide Manual Form" else "Add Manually")
                                }

                                Button(
                                    onClick = { onGenerateRuleFromPrompt(aiPrompt) },
                                    modifier = Modifier.testTag("generate_rule_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.generate_rule_cta))
                                }
                            }

                            // AI Rule Generation Output State
                            when (ruleGenState) {
                                is AiTaskState.Idle -> {}
                                is AiTaskState.Loading -> {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Synthesizing declarativeNetRequest JSON & CSS selector…",
                                            style = MaterialTheme.typography.bodySmall
                                        )
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
                                                text = ruleGenState.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AlertCoral
                                            )
                                        }
                                    }
                                }
                                is AiTaskState.Success -> {
                                    val generated = ruleGenState.data
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ShieldEmerald.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, ShieldEmerald.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "${generated.ruleName} (${generated.category})",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Button(
                                                    onClick = { onSaveGeneratedRule(generated) },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier
                                                        .height(34.dp)
                                                        .testTag("save_generated_rule_btn")
                                                ) {
                                                    Text("Save to Shield", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "urlFilter: ${generated.urlFilter}\nresourceType: [${generated.resourceTypes.joinToString(", ")}]\ncssSelector: ${generated.cssSelector}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = generated.explanation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Optional Manual Rule Addition Card
                if (showManualDialog) {
                    item {
                        Card(
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(14.dp)
                            ) {
                                if (selectedTab == 0) {
                                    Text("Add Custom Network Filter Rule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = manualPattern,
                                        onValueChange = { manualPattern = it },
                                        label = { Text("urlFilter Pattern (e.g. ||adserver.com^)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = manualTypes,
                                        onValueChange = { manualTypes = it },
                                        label = { Text("resourceType (comma-separated)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            onAddManualFilterRule(manualPattern, manualTypes, "Custom")
                                            showManualDialog = false
                                        }
                                    ) {
                                        Text("Save Network Rule")
                                    }
                                } else {
                                    Text("Add Custom Cosmetic CSS Selector", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = manualCssSelector,
                                        onValueChange = { manualCssSelector = it },
                                        label = { Text("CSS Selector (e.g. .ad-banner, [data-ad])") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = manualCssDesc,
                                        onValueChange = { manualCssDesc = it },
                                        label = { Text("Description") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            onAddManualCosmeticRule(manualCssSelector, manualCssDesc)
                                            showManualDialog = false
                                        }
                                    ) {
                                        Text("Save Cosmetic Selector")
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Active Rules List (Network vs Cosmetic)
                if (selectedTab == 0) {
                    items(filterRules, key = { it.id }) { rule ->
                        NetworkRuleItemCard(
                            rule = rule,
                            onToggle = { onToggleFilterRule(rule) },
                            onDelete = { onDeleteFilterRule(rule.id) }
                        )
                    }
                } else {
                    items(cosmeticRules, key = { it.id }) { rule ->
                        CosmeticRuleItemCard(
                            rule = rule,
                            onToggle = { onToggleCosmeticRule(rule) },
                            onDelete = { onDeleteCosmeticRule(rule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkRuleItemCard(
    rule: FilterRuleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = rule.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${rule.hitCount} hits • ${rule.source}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rule.urlFilter,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "resourceType: [${rule.resourceTypes}]",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() }
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete Rule",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CosmeticRuleItemCard(
    rule: CosmeticRuleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${rule.cssSelector} { display: none !important; }",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${rule.description} • ${rule.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() }
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete Cosmetic Rule",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ManifestV3KitExporterView(mv3Files: Map<String, String>) {
    val fileNames = mv3Files.keys.toList()
    var selectedFile by remember(fileNames) { mutableStateOf(fileNames.firstOrNull() ?: "manifest.json") }
    val currentCode = mv3Files[selectedFile].orEmpty()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Chrome Manifest V3 Extension Bundle (clean-browse/)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Live-generated from your active network rules, cosmetic CSS selectors, and ONNX threshold.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            fileNames.forEach { name ->
                FilterChip(
                    selected = selectedFile == name,
                    onClick = { selectedFile = name },
                    label = { Text(name, fontFamily = JetBrainsMonoFontFamily) },
                    leadingIcon = {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "clean-browse/$selectedFile",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(selectedFile, currentCode))
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy file content")
                        }
                        IconButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CleanBrowse AI - $selectedFile")
                                    putExtra(Intent.EXTRA_TEXT, currentCode)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export $selectedFile"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share extension file")
                        }
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = currentCode,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
