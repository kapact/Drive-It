# GitHub Actions - Automated Builds

## What is GitHub Actions?

GitHub Actions is a free CI/CD service from GitHub that can automatically build your app for Windows, macOS, and Linux whenever you push code.

**Why use it?**
- ✅ Build Windows MSI on Windows servers (even if you're on Mac!)
- ✅ Free for public repositories
- ✅ Automatic builds on every push
- ✅ Download ready-to-distribute packages

---

## Setup (One Time)

### 1. Push Your Code to GitHub

If you haven't already:

```bash
# Initialize git (if not done)
git init

# Add all files
git add .

# Make first commit
git commit -m "Initial commit"

# Create repository on GitHub (https://github.com/new)
# Then link and push:
git remote add origin https://github.com/YOUR_USERNAME/DriveIt.git
git branch -M main
git push -u origin main
```

### 2. Verify Workflow File Exists

The workflow file is already created at:
```
.github/workflows/build-packages.yml
```

This file tells GitHub Actions how to build your app.

---

## How to Build Windows/macOS/Linux Packages

### Option 1: Push to main branch (Automatic)

Just push your code to the main branch:

```bash
git add .
git commit -m "Update app"
git push
```

GitHub Actions will automatically:
1. Build Windows MSI
2. Build macOS DMG
3. Build Linux DEB

### Option 2: Create a Release Tag

For versioned releases:

```bash
# Tag the current version
git tag v1.0.0

# Push the tag
git push origin v1.0.0
```

This will:
1. Build all platforms
2. Create a GitHub Release
3. Attach all installers to the release

### Option 3: Manual Trigger

1. Go to your GitHub repository
2. Click "Actions" tab
3. Click "Build Packages" workflow
4. Click "Run workflow" button
5. Select branch and click "Run workflow"

---

## Download Built Packages

### During Development (from Actions)

1. Go to your repository on GitHub
2. Click the **Actions** tab
3. Click on the latest workflow run
4. Scroll down to **Artifacts** section
5. Download:
   - `DriveIt-Windows` (contains .msi file)
   - `DriveIt-macOS` (contains .dmg file)
   - `DriveIt-Linux` (contains .deb file)

**Note**: Artifacts expire after 90 days

### From Releases (Recommended for distribution)

1. Go to your repository on GitHub
2. Click the **Releases** section (right sidebar)
3. Download the installers directly from the release

Releases don't expire and are perfect for sharing with users!

---

## Current Workflow

The workflow runs on:
- ✅ Every push to `main` or `master` branch
- ✅ Every pull request
- ✅ When you create a tag (like `v1.0.0`)
- ✅ Manual trigger (via GitHub UI)

### What Gets Built

| Platform | Output | Location |
|----------|--------|----------|
| Windows | MSI installer | `app/build/compose/binaries/main/msi/DriveIt-1.0.0.msi` |
| macOS | DMG disk image | `app/build/compose/binaries/main/dmg/DriveIt-1.0.0.dmg` |
| Linux | DEB package | `app/build/compose/binaries/main/deb/driveit_1.0.0_amd64.deb` |

---

## Checking Build Status

### See if Build Succeeded

1. Go to your repository
2. Look for a green ✅ or red ❌ next to your latest commit
3. Click it to see build details

### View Build Logs

1. Click **Actions** tab
2. Click the workflow run
3. Click on a job (e.g., "Build Windows MSI")
4. View detailed logs

---

## Creating a Release

### Step-by-Step

1. **Update version** in `build.gradle.kts`:
   ```kotlin
   version = "1.1.0"  // Increment version
   ```

2. **Commit changes**:
   ```bash
   git add .
   git commit -m "Release v1.1.0"
   git push
   ```

3. **Create and push tag**:
   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```

4. **GitHub Actions will**:
   - Build all platforms
   - Create release automatically
   - Attach all installers

5. **Check your release**:
   - Go to `https://github.com/YOUR_USERNAME/DriveIt/releases`
   - You'll see v1.1.0 with all installers attached!

---

## Troubleshooting

### Build Failed - What to Do?

1. Click on the failed workflow
2. Check which job failed (Windows/macOS/Linux)
3. Read the error logs
4. Common issues:
   - **Missing credentials.json**: Make sure it's in `app/src/main/resources/`
   - **Java version**: Workflow uses JDK 17 (should match your local)
   - **Gradle errors**: Check build.gradle.kts syntax

### Credentials in GitHub Actions

The workflow automatically includes credentials.json from:
```
app/src/main/resources/credentials.json
```

**Important**:
- ⚠️ Make sure this file exists before pushing
- ⚠️ The file is gitignored for security, but will be in the build
- ✅ End users won't see it (it's bundled inside the app)

### No Artifacts Generated

If build succeeds but no artifacts:
1. Check that `if-no-files-found: error` didn't trigger
2. Verify output paths in workflow match actual build paths
3. Check build logs for packaging step

---

## Cost

- **Public repositories**: FREE forever
- **Private repositories**: 2,000 minutes/month free (typically enough)

Each build takes ~5 minutes, so you can do about 400 builds/month for free on private repos.

---

## Alternative: Build Locally on Windows

If you don't want to use GitHub Actions:

1. **Get a Windows PC** (or VM)
2. **Clone repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/DriveIt.git
   cd DriveIt
   ```
3. **Build MSI**:
   ```bash
   gradlew.bat packageMsi
   ```
4. **Find installer**:
   ```
   app\build\compose\binaries\main\msi\DriveIt-1.0.0.msi
   ```

---

## Summary

**Easiest way to get Windows executable:**

1. Push code to GitHub
2. Go to Actions tab
3. Wait ~5 minutes
4. Download Windows MSI from Artifacts

**For production releases:**

1. Update version in build.gradle.kts
2. Create git tag: `git tag v1.0.0 && git push origin v1.0.0`
3. GitHub creates release automatically
4. Share the release link with users!

🎉 That's it! You never need a Windows machine to build Windows installers!
