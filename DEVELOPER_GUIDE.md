# DriveIt - Developer Guide

## How Credentials Work

### Overview

DriveIt uses OAuth 2.0 for Google Drive authentication. The credential handling is split into two parts:

1. **OAuth Client Credentials** (`credentials.json`) - Set up by YOU (developer), bundled with app
2. **User OAuth Tokens** (`StoredCredential`) - Generated when end user authorizes the app

### For You (The Developer)

#### Initial Setup (One Time Only)

1. **Get OAuth Credentials from Google:**
   - Follow steps in README.md to create OAuth 2.0 credentials
   - Download the JSON file from Google Cloud Console

2. **Add Credentials to Project:**
   ```bash
   # Copy to BOTH locations:
   cp ~/Downloads/client_secret_*.json app/credentials/credentials.json
   cp ~/Downloads/client_secret_*.json app/src/main/resources/credentials.json
   ```

   Why two locations?
   - `app/credentials/credentials.json` - Used during development (`./gradlew run`)
   - `app/src/main/resources/credentials.json` - Bundled into packaged app (DMG, MSI, etc.)

3. **Build and Package:**
   ```bash
   ./gradlew packageDmg    # for macOS
   ./gradlew packageMsi    # for Windows (on Windows machine)
   ```

The credentials.json is now **embedded inside** the packaged app!

#### What Gets Committed to Git?

- ✅ Source code
- ✅ Build scripts
- ❌ `credentials.json` (excluded by .gitignore)
- ❌ `StoredCredential` (excluded by .gitignore)

This protects your OAuth client secret from being exposed in version control.

#### Distributing the App

Just share the packaged files:
- macOS: `app/build/compose/binaries/main/dmg/DriveIt-1.0.0.dmg`
- Windows: `app/build/compose/binaries/main/msi/DriveIt-1.0.0.msi` (or .exe)

The credentials are already inside! End users don't need any setup.

---

### For End Users

#### Installation

1. Download and install DriveIt (DMG, MSI, or EXE)
2. Launch the app
3. Click "Upload to Google Drive"
4. **First time only**: Browser opens for authorization
   - Sign in with Google account
   - Click "Continue" (even if you see "Google hasn't verified this app" warning)
   - Click "Allow" to grant permissions
   - Close browser when done
5. Done! Future uploads won't need authorization

#### What Happens Behind the Scenes?

- **credentials.json**: Already bundled in the app (you don't see it)
- **StoredCredential**: Created in `~/.driveit/` after first authorization
  - Contains refresh token for future logins
  - Stays on their computer (never shared)

#### No Manual Setup Required!

End users NEVER need to:
- Download credentials from Google Cloud Console
- Copy files to directories
- Run setup scripts
- Anything except install and authorize once

---

## File Locations

### Development (Running from source)

```
DriveIt/
├── app/
│   ├── credentials/
│   │   ├── credentials.json        # OAuth client credentials (developer adds)
│   │   └── StoredCredential        # Your personal OAuth token (auto-generated)
│   └── src/main/resources/
│       └── credentials.json        # Copy of credentials (bundled into app)
```

### Production (Installed app on end user's computer)

```
~/.driveit/
├── credentials.json    # Extracted from app resources on first run
└── StoredCredential    # User's OAuth refresh token (after authorization)
```

---

## Security Notes

### Safe to Share

- ✅ Packaged DMG/MSI/EXE files - Credentials are embedded, but that's OK
- ✅ Source code (without credentials.json)

### Never Share

- ❌ Raw `credentials.json` file - Contains OAuth client secret
- ❌ `StoredCredential` files - Personal refresh tokens

### Why OAuth Client Credentials in App is OK

Desktop OAuth apps use a different flow than server apps:
- Client secret isn't truly secret (can be extracted from app)
- Google knows this and has different security model
- Real security comes from: user must authorize in browser
- Each user gets their own tokens (StoredCredential)

However, still keep credentials.json out of public Git repos to follow best practices.

---

## Common Tasks

### Update OAuth Credentials

If you need to change OAuth client:

```bash
# 1. Get new credentials.json from Google Cloud Console
# 2. Replace both files:
cp ~/Downloads/new_credentials.json app/credentials/credentials.json
cp ~/Downloads/new_credentials.json app/src/main/resources/credentials.json

# 3. Rebuild
./gradlew clean build packageDmg
```

### Test Locally

```bash
./gradlew run
```

Uses credentials from `app/credentials/credentials.json`.

### Reset Authorization

If something goes wrong with OAuth:

```bash
# Delete stored tokens
rm -rf ~/.driveit/StoredCredential

# Next run will ask for authorization again
```

---

## Troubleshooting

### "App not properly configured" Error

This means `credentials.json` is missing from resources.

**Fix:**
```bash
cp app/credentials/credentials.json app/src/main/resources/credentials.json
./gradlew clean build packageDmg
```

### Build Fails on Windows Packaging

Windows installers (MSI/EXE) must be built on Windows machines. Use:
- A Windows PC
- GitHub Actions (see PACKAGING.md)
- Virtual machine

### End User Can't Authorize

Make sure:
1. You added them as test user in Google Cloud Console OAuth consent screen
2. OAuth consent screen is configured
3. Google Drive API is enabled in your project

---

## Summary

**Developer (You):**
- Set up credentials.json once
- Copy to app/credentials/ and app/src/main/resources/
- Build and package
- Distribute DMG/MSI/EXE

**End User:**
- Install app
- Launch and click upload
- Authorize in browser (first time only)
- Use the app!

No manual file copying for end users. No setup scripts. Just install and go! 🚀
