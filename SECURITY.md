# Security Policy (`SECURITY.md`)

## Supported Versions

Only the latest release branch of **CleanBrowse AI** receives active security patches and filter engine hardening updates.

| Version | Supported |
| :--- | :--- |
| `1.0.x` (Current) | ✅ Yes |
| `< 1.0.0` | ❌ No |

---

## Security Architecture Highlights

1. **Zero Client-Side API Keys**:
   CleanBrowse AI uses **Firebase AI Logic** attested by **Firebase App Check** (`RecaptchaAppCheckProviderFactory` in production and `DebugAppCheckProviderFactory` in debug builds). No raw `GEMINI_API_KEY` or secret credentials are hardcoded in the APK or `.env` files.
2. **Manifest V3 `declarativeNetRequest` Compatibility**:
   Exported browser extension bundles strictly follow Chrome Manifest V3 rules, avoiding remotely hosted code execution (`eval`, remote JS injection) and limiting permissions to declarative rulesets and local DOM observation.
3. **WebView Isolation**:
   Network interception (`shouldInterceptRequest`) drops matching ad and tracker domains prior to network socket connection.

---

## Reporting a Vulnerability

If you discover a security vulnerability—such as a filter bypass vector, XSS via crafted DOM selectors, or an App Check attestation issue—please report it privately:

1. **Do NOT open a public GitHub issue** for exploitable vulnerabilities.
2. Send a detailed report including:
   - Affected component (`Hybrid Browser WebView`, `DOM Classifier Bridge`, `MV3 Exporter`, or `AI Service`).
   - Step-by-step reproduction proof-of-concept (PoC).
   - Potential impact assessment.
3. Maintainers will acknowledge receipt within **48 hours** and provide a remediation timeline within **7 business days**.
