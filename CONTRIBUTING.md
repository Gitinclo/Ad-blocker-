# Contributing to CleanBrowse AI (`CONTRIBUTING.md`)

Thank you for helping make the web cleaner, faster, and more private! Contributions of filter rules, DOM training vectors, bug fixes, and UI enhancements are warmly welcomed.

---

## 1. Ways to Contribute

### A. Submitting New Network Filter Rules (`||domain^`)
When proposing a new rule for `rules/ads.json` or the default Room seed list:
1. Prefer `||domain.tld^` syntax with explicit `resourceType` constraints (`script`, `image`, `sub_frame`, `xmlhttprequest`, `ping`) to avoid breaking first-party page functionality.
2. Test the rule inside the **CleanBrowse AI Rule Studio** and verify zero false positives on the target website.

### B. Submitting Labeled DOM Feature Vectors
To improve the 6-feature DOM classifier (`[ratio, hasIframe, externalLinks, hasKeyword, childCount, depth]`):
1. Open the **AI Scanner -> 6-Feature DOM Scanner** tab.
2. Record the element's tag summary, dimensions, and feature vector.
3. Include whether the element is an `AD` (`1`) or `CLEAN` (`0`) in your Pull Request description.

### C. Code Contributions (Kotlin / Jetpack Compose)
* Follow **MVVM** architecture (`CleanBrowseViewModel` -> `AdBlockRepository` -> `AdBlockDao`).
* Ensure all interactive Compose elements include a `Modifier.testTag("snake_case_name")` and meet the `48.dp` minimum touch target guideline.
* Keep source files modular (under 500 lines per file).

---

## 2. Development & Verification Workflow

1. Run unit and Robolectric tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```
2. Assemble the debug APK:
   ```bash
   gradle assembleDebug
   ```
3. Complete the checklist in [`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md).
