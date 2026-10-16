# DriveIt - Packaging Guide

## ✅ Successfully Created Packages

### macOS DMG (Ready to use!)
**Location**: `app/build/compose/binaries/main/dmg/DriveIt-1.0.0.dmg`

This DMG file can be distributed to macOS users. They can:
1. Open the DMG file
2. Drag DriveIt.app to Applications folder
3. Run the app

### macOS App Bundle
**Location**: `app/build/compose/binaries/main/app/DriveIt.app`

This is a standalone macOS application that can be run directly.

---

## 🪟 Windows Packaging

Windows installers (MSI/EXE) can only be built on Windows machines. Here's how to create them:

### Option 1: Build on Windows Machine

1. **Install Prerequisites on Windows:**
   - Install JDK 17 or higher
   - Install WiX Toolset (for MSI) from https://wixtoolset.org/

2. **Clone your repository on Windows:**
   ```bash
   git clone <your-repo>
   cd DriveIt
   ```

3. **Build Windows MSI:**
   ```bash
   gradlew.bat packageMsi
   ```
   Output: `app/build/compose/binaries/main/msi/DriveIt-1.0.0.msi`

4. **Or build Windows EXE:**
   ```bash
   gradlew.bat packageExe
   ```
   Output: `app/build/compose/binaries/main/exe/DriveIt-1.0.0.exe`

### Option 2: GitHub Actions (Automated)

Create `.github/workflows/package.yml`:

```yaml
name: Package App

on:
  push:
    tags:
      - 'v*'

jobs:
  package-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build Windows MSI
        run: ./gradlew packageMsi
      - name: Upload MSI
        uses: actions/upload-artifact@v3
        with:
          name: windows-msi
          path: app/build/compose/binaries/main/msi/*.msi

  package-macos:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build macOS DMG
        run: ./gradlew packageDmg
      - name: Upload DMG
        uses: actions/upload-artifact@v3
        with:
          name: macos-dmg
          path: app/build/compose/binaries/main/dmg/*.dmg
```

### Option 3: Cross-platform JAR (All Platforms)

Build a universal JAR that works on Windows, macOS, and Linux:

```bash
./gradlew jar
```

Users can run it with:
```bash
java -jar app/build/libs/app-1.0.0.jar
```

---

## 🐧 Linux Packaging

Build on Linux or macOS:

```bash
./gradlew packageDeb
```

Output: `app/build/compose/binaries/main/deb/driveit_1.0.0_amd64.deb`

---

## 🎨 Adding Custom Icons

I've created an SVG icon at `app/icon.svg`. To use it:

### Convert SVG to required formats:

**Online (Easiest):**
1. Go to https://cloudconvert.com/svg-to-ico or https://convertio.co
2. Upload `app/icon.svg`
3. Convert to:
   - `icon.ico` (Windows)
   - `icon.png` (Linux, 512x512)
   - `icon.icns` (macOS)

**Or use ImageMagick (if installed):**
```bash
# Install ImageMagick first
brew install imagemagick

# Convert SVG to PNG (512x512)
convert -background none -resize 512x512 app/icon.svg app/icon.png

# Create ICO for Windows
convert app/icon.png -define icon:auto-resize=256,128,64,48,32,16 app/icon.ico

# Create ICNS for macOS (requires iconutil)
mkdir icon.iconset
sips -z 16 16     app/icon.png --out icon.iconset/icon_16x16.png
sips -z 32 32     app/icon.png --out icon.iconset/icon_16x16@2x.png
sips -z 32 32     app/icon.png --out icon.iconset/icon_32x32.png
sips -z 64 64     app/icon.png --out icon.iconset/icon_32x32@2x.png
sips -z 128 128   app/icon.png --out icon.iconset/icon_128x128.png
sips -z 256 256   app/icon.png --out icon.iconset/icon_128x128@2x.png
sips -z 256 256   app/icon.png --out icon.iconset/icon_256x256.png
sips -z 512 512   app/icon.png --out icon.iconset/icon_256x256@2x.png
iconutil -c icns icon.iconset
mv icon.icns app/icon.icns
rm -rf icon.iconset
```

After creating icons, uncomment the icon lines in `app/build.gradle.kts` and rebuild.

---

## 📦 Quick Commands Reference

| Platform | Command | Output |
|----------|---------|--------|
| macOS DMG | `./gradlew packageDmg` | `app/build/compose/binaries/main/dmg/*.dmg` |
| Windows MSI | `gradlew.bat packageMsi` (on Windows) | `app/build/compose/binaries/main/msi/*.msi` |
| Windows EXE | `gradlew.bat packageExe` (on Windows) | `app/build/compose/binaries/main/exe/*.exe` |
| Linux DEB | `./gradlew packageDeb` | `app/build/compose/binaries/main/deb/*.deb` |
| All formats | `./gradlew package` | All binaries |
| Current OS | `./gradlew packageDistributionForCurrentOS` | Binaries for current OS |

---

## 🚀 Distribution

After building, you can distribute:

1. **macOS users**: Share the `.dmg` file
2. **Windows users**: Share the `.msi` or `.exe` file (build on Windows)
3. **Linux users**: Share the `.deb` file
4. **All platforms**: Share the JAR file (requires Java 17+)

---

## 📋 Requirements for Users

- **macOS**: macOS 10.14 or higher
- **Windows**: Windows 10 or higher
- **Linux**: Ubuntu 20.04+ or equivalent
- **All platforms**: JDK 17 or higher (if using JAR)

The packaged installers include a bundled JRE, so end users don't need Java installed!
