package com.example.data

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "CleanBrowseAiService"

class CleanBrowseAiService {

    // 1. Schema for AI Filter Rule & CSS Selector Generation
    private val ruleGeneratorSchema = Schema.obj(
        properties = mapOf(
            "ruleName" to Schema.string(description = "Short descriptive name for the filter rule"),
            "urlFilter" to Schema.string(description = "Chrome declarativeNetRequest urlFilter pattern using ||domain^ syntax"),
            "resourceTypes" to Schema.array(
                items = Schema.string(),
                description = "Allowed resourceType values such as script, image, sub_frame, xmlhttprequest, ping"
            ),
            "cssSelector" to Schema.string(description = "CSS selector for collapsing leftover DOM containers"),
            "category" to Schema.enumeration(
                values = listOf("Ad Broker", "Analytics Tracker", "Sponsored Widget", "Social Pixel", "AI Generated"),
                description = "Category of the blocked tracker or ad unit"
            ),
            "explanation" to Schema.string(description = "Clear explanation of how the network rule and CSS selector block the ad"),
            "confidence" to Schema.double(description = "Confidence score between 0.50 and 0.99")
        )
    )

    // 2. Schema for Multimodal Webpage Screenshot Ad Detection
    private val visualScanSchema = Schema.obj(
        properties = mapOf(
            "summary" to Schema.string(description = "Executive summary of the visual webpage ad analysis"),
            "overallRiskLevel" to Schema.enumeration(
                values = listOf("Clean", "Low Clutter", "Moderate Ad Load", "High Ad Intrusion"),
                description = "Overall ad and dark-pattern intrusion rating"
            ),
            "detectedAds" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "elementTitle" to Schema.string(description = "Name or headline of the detected ad or sponsored unit"),
                        "adType" to Schema.string(description = "Type of ad: IAB Banner, Native Sponsored Post, Sticky Overlay, Fake Button, or Widget Grid"),
                        "locationRegion" to Schema.string(description = "Where on the screen the ad is located (e.g. Top Leaderboard, In-Feed Middle, Sidebar)"),
                        "confidence" to Schema.double(description = "Detection confidence from 0.50 to 0.99"),
                        "suggestedCssSelector" to Schema.string(description = "Recommended CSS selector to hide this container"),
                        "suggestedUrlPattern" to Schema.string(description = "Recommended ||domain^ filter pattern"),
                        "reasoning" to Schema.string(description = "Visual and semantic cues that identify this region as an advertisement")
                    )
                ),
                description = "List of detected ads, sponsored cards, or dark-pattern elements"
            )
        )
    )

    // 3. Schema for Deep Semantic DOM Element Inspection
    private val domInspectionSchema = Schema.obj(
        properties = mapOf(
            "classification" to Schema.enumeration(
                values = listOf("AD", "CLEAN"),
                description = "Whether the DOM element is an advertisement or legitimate content"
            ),
            "confidence" to Schema.double(description = "Classifier confidence score from 0.0 to 1.0"),
            "reasoning" to Schema.string(description = "Why the structural features and text semantics indicate an ad or clean content"),
            "recommendedCssSelector" to Schema.string(description = "Precise CSS selector to hide this element if it is an ad, or N/A if clean"),
            "featureAnalysis" to Schema.string(description = "Breakdown of aspect ratio, iframe presence, outbound links, and keywords")
        )
    )

    // Lazy initialization per Firebase AI Logic guidelines
    private val ruleGeneratorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = ruleGeneratorSchema
            }
        )
    }

    private val visualScannerModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = visualScanSchema
            }
        )
    }

    private val domInspectorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = domInspectionSchema
            }
        )
    }

    suspend fun generateFilterRule(userPrompt: String): Result<GeneratedRuleResult> {
        return try {
            val prompt = """
                You are CleanBrowse AI, an expert Chrome Manifest V3 declarativeNetRequest and CSS cosmetic filter engineer.
                Convert the following user request, URL, or HTML snippet into both a high-precision network urlFilter rule (using ||domain^ syntax)
                and a cosmetic CSS element-hiding selector:
                
                User Input:
                $userPrompt
            """.trimIndent()

            val response = withTimeout(60_000L) {
                ruleGeneratorModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Model returned an empty response.")
            val json = JSONObject(jsonText)

            val typesArray = json.optJSONArray("resourceTypes")
            val resourceTypes = mutableListOf<String>()
            if (typesArray != null) {
                for (i in 0 until typesArray.length()) {
                    resourceTypes.add(typesArray.getString(i))
                }
            }
            if (resourceTypes.isEmpty()) {
                resourceTypes.addAll(listOf("script", "image", "sub_frame"))
            }

            Result.success(
                GeneratedRuleResult(
                    ruleName = json.optString("ruleName", "AI Custom Filter"),
                    urlFilter = json.optString("urlFilter", "||ads.example.com^"),
                    resourceTypes = resourceTypes,
                    cssSelector = json.optString("cssSelector", ".sponsored-unit"),
                    category = json.optString("category", "AI Generated"),
                    explanation = json.optString("explanation", "Generated by CleanBrowse AI."),
                    confidence = json.optDouble("confidence", 0.92)
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Rule generation timed out", e)
            Result.failure(Exception("AI rule generation timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate filter rule via Firebase AI", e)
            Result.failure(Exception(e.localizedMessage ?: "Failed to generate filter rule."))
        }
    }

    suspend fun analyzeWebpageScreenshot(bitmap: Bitmap, contextNote: String = ""): Result<VisualScanReport> {
        return try {
            val response = withTimeout(60_000L) {
                visualScannerModel.generateContent(
                    content {
                        image(bitmap)
                        text(
                            """
                            Analyze this webpage screenshot as CleanBrowse AI's perceptual ad detector.
                            Identify all visible banner ads (e.g. 728x90 leaderboards, 300x250 boxes), native "Sponsored" or "Promoted" feed cards,
                            content recommendation widgets (Taboola/Outbrain style), or deceptive overlay banners.
                            For each detected ad unit, provide its title, ad type, screen location, confidence score, a recommended CSS selector to collapse it,
                            and a recommended ||domain^ network rule pattern.
                            Additional context: $contextNote
                            """.trimIndent()
                        )
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("Model returned an empty response.")
            val json = JSONObject(jsonText)
            val adsArray = json.optJSONArray("detectedAds")
            val findings = mutableListOf<VisualAdFinding>()
            if (adsArray != null) {
                for (i in 0 until adsArray.length()) {
                    val item = adsArray.getJSONObject(i)
                    findings.add(
                        VisualAdFinding(
                            elementTitle = item.optString("elementTitle", "Detected Ad Unit"),
                            adType = item.optString("adType", "Native / Banner Ad"),
                            locationRegion = item.optString("locationRegion", "Viewport"),
                            confidence = item.optDouble("confidence", 0.90),
                            suggestedCssSelector = item.optString("suggestedCssSelector", ".ad-container"),
                            suggestedUrlPattern = item.optString("suggestedUrlPattern", "||doubleclick.net^"),
                            reasoning = item.optString("reasoning", "Visual layout and sponsored labeling match ad profile.")
                        )
                    )
                }
            }
            Result.success(
                VisualScanReport(
                    summary = json.optString("summary", "Completed visual perceptual ad scan."),
                    overallRiskLevel = json.optString("overallRiskLevel", "Moderate Ad Load"),
                    detectedAds = findings
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Visual ad scan timed out", e)
            Result.failure(Exception("Visual screenshot analysis timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to analyze webpage screenshot via Firebase AI", e)
            Result.failure(Exception(e.localizedMessage ?: "Visual screenshot analysis failed."))
        }
    }

    suspend fun inspectDomElementSemantics(candidate: DomElementCandidate): Result<SemanticDomReport> {
        return try {
            val prompt = """
                Analyze this candidate DOM element and its 6-feature structural vector for CleanBrowse AI:
                - Tag / Classes: ${candidate.tagSummary}
                - Dimensions: ${candidate.widthPx}x${candidate.heightPx}px (Ratio: ${String.format(java.util.Locale.US, "%.2f", candidate.ratio)})
                - 6-Feature Vector [ratio, hasIframe, externalLinks, hasKeyword, childCount, depth]: ${candidate.featureVectorFormatted}
                - Inner Text Snippet: "${candidate.textSnippet}"
                - Local Random Forest Score: ${String.format(java.util.Locale.US, "%.2f", candidate.localConfidence)}
                
                Determine whether this DOM subtree is an AD or CLEAN content, explain the structural/semantic signals, and provide a CSS selector if it should be collapsed.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                domInspectorModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Model returned an empty response.")
            val json = JSONObject(jsonText)

            Result.success(
                SemanticDomReport(
                    classification = json.optString("classification", "AD"),
                    confidence = json.optDouble("confidence", 0.92),
                    reasoning = json.optString("reasoning", "Analyzed structural geometry and semantic tokens."),
                    recommendedCssSelector = json.optString("recommendedCssSelector", ".ad-banner"),
                    featureAnalysis = json.optString("featureAnalysis", "Evaluated 6-feature DOM vector.")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "DOM semantic analysis timed out", e)
            Result.failure(Exception("DOM semantic analysis timed out after 60 seconds."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "DOM semantic analysis failed", e)
            Result.failure(Exception(e.localizedMessage ?: "DOM semantic analysis failed."))
        }
    }
}
