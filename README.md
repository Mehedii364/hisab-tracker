# Hisab Tracker AI

An AI-powered personal financial management system built with Kotlin and Jetpack Compose. Hisab Tracker AI integrates OpenRouter API and Google Gemini API for natural language expense/income tracking, budget management, and financial insights in both Bangla and English.

---

## GitHub Actions CI/CD Pipeline Setup

The project includes an automated GitHub Actions workflow (`.github/workflows/android-build.yml`) that performs unit testing, Android Lint checks, debug and release builds, and uploads APK and AAB build artifacts.

### 1. Configuring Required GitHub Secrets for Release Signing

To sign release APKs and App Bundles (AAB) in GitHub Actions, configure the following secrets in your repository:

#### Step 1: Encode Your Keystore File to Base64
Run the following command in your terminal to convert your Java Keystore (`.jks` or `.keystore`) into a Base64 string:

```bash
# On Linux / macOS:
base64 -w 0 my-release-key.jks > keystore_base64.txt

# On Windows (PowerShell):
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-release-key.jks")) | Out-File -Encoding ascii keystore_base64.txt
```

#### Step 2: Add Secrets to GitHub Repository
1. Open your repository on GitHub.
2. Go to **Settings** > **Secrets and variables** > **Actions**.
3. Click **New repository secret** and create the following three secrets:

| Secret Name | Description | Example / Format |
|---|---|---|
| `KEYSTORE_BASE64` | The contents of `keystore_base64.txt` generated in Step 1. | `MIIJqQIBAzCC...` |
| `STORE_PASSWORD` | The keystore password used when creating the keystore file. | `YourStorePassword123` |
| `KEY_PASSWORD` | The key alias password (often the same as store password). | `YourKeyPassword123` |

> **Note:** If `KEYSTORE_BASE64` is not configured, the workflow will automatically generate a temporary fallback keystore to verify the release build steps.

---

### 2. How to Trigger the CI/CD Pipeline

The workflow automatically runs on:
- Pushes to `main`, `master`, or `develop` branches.
- Pull requests targeting `main`, `master`, or `develop`.

#### Manual Trigger (`workflow_dispatch`)
To trigger a build manually anytime from GitHub:
1. Go to the **Actions** tab of your repository.
2. Under **Workflows** in the left sidebar, select **Android CI/CD Build & Release**.
3. Click the **Run workflow** dropdown button on the right.
4. Select the target branch (e.g., `main`) and click **Run workflow**.

---

### 3. Finding Generated APK and AAB Artifacts

Once a workflow run completes:
1. Navigate to the **Actions** tab in GitHub.
2. Click on the specific completed workflow run (e.g., *Android CI/CD Build & Release #12*).
3. Scroll down to the bottom of the summary page to the **Artifacts** section.
4. Download any of the pre-packaged build artifacts:
   - **`app-debug-apk`**: Debug build for testing and local installation.
   - **`app-release-apk`**: Production-ready signed APK file for direct installation on Android devices.
   - **`app-release-aab`**: Production Android App Bundle (`.aab`) ready for uploading to the Google Play Console.

---

### 4. Creating a GitHub Release from Build Outputs

To publish an official versioned release on GitHub:

1. **Download the Release Outputs**:
   - Download `app-release-apk` and `app-release-aab` from the **Artifacts** section of a successful workflow run and extract the `.zip` files.
2. **Draft a New Release**:
   - Go to your repository's main page on GitHub and click **Releases** (located in the right sidebar).
   - Click **Draft a new release**.
3. **Configure Release Information**:
   - Click **Choose a tag** and type a new tag version (e.g., `v1.0.0`), then select **Create new tag**.
   - Enter a **Release title** (e.g., `Hisab Tracker AI v1.0.0`).
   - Add release notes describing new features or fixes.
4. **Attach Binaries**:
   - Drag and drop the extracted `app-release.apk` and `app-release.aab` files into the **Attach binaries by dropping them here or selecting them** box.
5. **Publish**:
   - Click **Publish release**.
