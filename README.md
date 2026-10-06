# PersonalVault

PersonalVault is a secure, private single-APK Android application designed for dual-role usage on both **Agent Devices** (Country Phone) and **Controller Devices** (Abroad Phone).

---

## Features & Architecture

- **Single APK Architecture**: Both Agent and Controller modes run from the same APK binary.
- **Privacy First**: Zero third-party analytics, ads, or telemetry. End-to-end encrypted sessions.
- **GitHub Release Distribution & In-App Update Engine**: Automatic, secure update detection and user-consented APK installation directly from GitHub Releases.

---

## GitHub Setup & Release Workflow

### 1. Create the GitHub Repository
1. Log into your GitHub account.
2. Click **New Repository**.
3. Name the repository `PersonalVault` (or matching your preferred repository name).
4. Keep it **Private** or **Public** as desired.
5. Do **not** initialize with a README if you are pushing an existing codebase.

### 2. Connect the Local Android Studio Project
In terminal or Android Studio command line:
```bash
cd "F:/06.10.26"
git init
git add .
git commit -m "Initial commit of PersonalVault with GitHub Update Engine"
git branch -M main
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/PersonalVault.git
git push -u origin main
```

Update the configuration in `app/src/main/java/com/personalvault/utils/update/UpdateConfig.kt`:
```kotlin
object UpdateConfig {
    const val GITHUB_OWNER = "YOUR_GITHUB_USERNAME" // Set your GitHub username here
    const val GITHUB_REPO = "PersonalVault"
}
```

### 3. Create a Signing Keystore
To sign release builds, generate a Keystore using `keytool` (included with JDK):
```bash
keytool -genkeypair -v -keystore release.keystore -alias personalvault -keyalg RSA -keysize 2048 -validity 10000
```
> **IMPORTANT**: Never commit `release.keystore` or passwords to Git. The `.gitignore` file automatically excludes keystore files.

### 4. Configure GitHub Repository Secrets
Convert your keystore file to Base64:
- On Linux / macOS / Git Bash:
  ```bash
  base64 -w 0 release.keystore > keystore_base64.txt
  ```
- On Windows PowerShell:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Out-File -Encoding ascii keystore_base64.txt
  ```

In your GitHub repository settings:
1. Go to **Settings** → **Secrets and variables** → **Actions**.
2. Click **New repository secret** and add:
   - `KEYSTORE_BASE64`: The full base64 encoded string from `keystore_base64.txt`.
   - `KEYSTORE_PASSWORD`: Password used when creating the keystore.
   - `KEY_ALIAS`: Alias used (e.g., `personalvault`).
   - `KEY_PASSWORD`: Key password.

### 5. Create a Release Tag & Build Automated APK
When you are ready to release a new version:
1. Open `app/build.gradle.kts` and update version numbers:
   ```kotlin
   versionCode = 2
   versionName = "1.0.1"
   ```
2. Commit and push your changes:
   ```bash
   git commit -am "Bump version to 1.0.1"
   git push origin main
   ```
3. Create and push a version tag:
   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```

### 6. GitHub Actions Automation
1. The `.github/workflows/android-release.yml` workflow triggers automatically on tag push (`v*`).
2. GitHub Actions checks out the repository, sets up Java 17, restores Gradle cache, runs Android lint checks.
3. Decodes `KEYSTORE_BASE64` secret into `app/release.keystore`.
4. Executes `./gradlew assembleRelease` to compile a signed production APK.
5. Publishes a new **GitHub Release** and uploads `PersonalVault-v1.0.1.apk` as a release asset.

### 7. App Update Detection & User Consent Flow
1. **Periodic Background Check**: WorkManager periodically polls `https://api.github.com/repos/YOUR_GITHUB_USERNAME/PersonalVault/releases/latest` over HTTPS.
2. **Manual Check**: Users can tap **Settings → Check for Updates** anytime on both Agent and Controller devices.
3. **Notification & UI Dialog**:
   - Compares installed version (`1.0.0`) with latest GitHub Release (`1.0.1`).
   - Displays new version details and release notes inside the app update dialog.
4. **Secure Download & Installation**:
   - Tapping **Update Now** downloads the signed release APK directly over HTTPS.
   - Verifies file integrity with Android Package Manager before installation.
   - Triggers Android's standard package installer requiring explicit user consent.
   - No silent installs, root access, or security bypasses are performed.

---

## License & Security
All session communication between Agent and Controller devices is private and end-to-end encrypted. Signing keys and sensitive credentials remain secure outside source control.
