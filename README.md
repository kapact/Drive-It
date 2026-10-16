# DriveIt - Compress & Upload to Google Drive

A cross-platform desktop application for macOS and Windows that compresses folders and uploads them to Google Drive with a single click.

## Features

- **One-Click Upload**: Select a folder and upload it to Google Drive instantly
- **Automatic Compression**: Folders are automatically compressed to ZIP format before upload
- **Progress Tracking**: Real-time progress indicators for compression and upload
- **Service Account Authentication**: Secure authentication using Google Service Account
- **Cross-Platform**: Works on macOS and Windows

## Prerequisites

Before using DriveIt, you need:

1. A Google Cloud Project with Drive API enabled
2. OAuth 2.0 credentials (Desktop app)

## Setting Up Google OAuth Credentials

### Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project dropdown at the top
3. Click "New Project"
4. Give it a name (e.g., "DriveIt")
5. Click "Create"

### Step 2: Enable Google Drive API

1. In the Cloud Console, go to **APIs & Services** → **Library**
2. Search for "Google Drive API"
3. Click on it and press **Enable**

### Step 3: Configure OAuth Consent Screen

1. Go to **APIs & Services** → **OAuth consent screen**
2. Select **External** user type
3. Click **Create**
4. Fill in the required fields:
   - **App name**: DriveIt
   - **User support email**: Your email
   - **Developer contact email**: Your email
5. Click **Save and Continue**
6. Skip "Scopes" (click **Save and Continue**)
7. Add yourself as a test user:
   - Click **Add Users**
   - Enter your Google email
   - Click **Add**
8. Click **Save and Continue**
9. Review and click **Back to Dashboard**

### Step 4: Create OAuth 2.0 Credentials

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth 2.0 Client ID**
3. Select **Application type**: **Desktop app**
4. Give it a name (e.g., "DriveIt Desktop")
5. Click **Create**
6. Click **Download JSON** (download icon)
7. Save the file securely - this is your credentials file
8. Click **OK**

### Step 5: Get Folder ID (Optional)

If you want uploads to go to a specific folder:

1. Open [Google Drive](https://drive.google.com)
2. Create a new folder or navigate to an existing one
3. Open the folder
4. Copy the Folder ID from the URL:
   - Example: `https://drive.google.com/drive/folders/1A2B3C4D5E6F7G8H9I0J`
   - Folder ID is: `1A2B3C4D5E6F7G8H9I0J`

If you leave the Folder ID empty, files will upload to your Google Drive root.

## Building the Application

### Prerequisites for Building

- JDK 17 or higher
- Gradle (included via wrapper)

### Build for macOS

```bash
./gradlew packageDmg
```

The DMG file will be located in:
```
app/build/compose/binaries/main/dmg/
```

### Build for Windows

```bash
./gradlew packageMsi
```

The MSI installer will be located in:
```
app/build/compose/binaries/main/msi/
```

### Run Without Building

For development or testing:

```bash
./gradlew run
```

## For End Users

### Installation & First Use

1. **Install DriveIt**:
   - macOS: Open the DMG file, drag DriveIt to Applications
   - Windows: Run the MSI/EXE installer

2. **Launch DriveIt** from your Applications folder

3. **Select a folder** you want to upload

4. **Click "Upload to Google Drive"**

5. **Authorize on first use** (browser will open):
   - Sign in with your Google account
   - Click **Continue** if you see "Google hasn't verified this app" warning
   - Click **Allow** to grant DriveIt access to Google Drive
   - Close the browser when you see "You may now close this window"

6. **Done!** Your folder is being uploaded

**That's it!** Future uploads won't need browser authorization.

---

## For Developers

See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) for complete setup instructions including:
- How to get and configure OAuth credentials
- How to build and package the app
- How credentials are bundled

3. **Use the app**:
   - Select a folder to upload
   - (Optional) Add a **Folder ID** if you want uploads to go to a specific folder
   - Click the big "Compress & Upload" button
   - Authorize in browser on first use
   - Done!

### Uploading a Folder

1. Go back to the main screen (click the cloud icon in top-right)
2. Click **Select Folder**
3. Choose the folder you want to upload
4. Click **Compress & Upload to Google Drive**
5. **First time only**: A browser window will open asking you to authorize the app
   - Sign in with your Google account
   - Click **Continue** when you see the "Google hasn't verified this app" warning
   - Click **Allow** to grant permissions
   - Close the browser window when you see "You may now close this window"
6. Wait for the upload to complete

The app will:
- Compress the folder to a ZIP file
- Upload it to your Google Drive (to the specified folder or root)
- Delete the temporary ZIP file
- Show you a success message

**Note**: After the first authorization, subsequent uploads won't require browser authentication.

## Project Structure

```
DriveIt/
├── app/
│   ├── src/main/kotlin/com/zuno/driveit/
│   │   ├── Main.kt                     # Application entry point
│   │   ├── DriveItApp.kt               # Main app composable
│   │   ├── ui/
│   │   │   └── MainScreen.kt           # Main upload screen
│   │   ├── service/
│   │   │   └── GoogleDriveService.kt   # Google Drive API integration
│   │   └── util/
│   │       ├── CompressionUtil.kt      # ZIP compression utilities
│   │       └── PreferencesManager.kt   # Settings storage
│   ├── credentials/                    # Credentials directory
│   │   ├── credentials.json            # Your OAuth credentials (you create this)
│   │   ├── StoredCredential            # Auto-generated after authorization
│   │   ├── .gitkeep                    # Keeps directory in Git
│   │   └── README.md                   # Setup instructions
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml              # Dependency versions
└── README.md
```

## Dependencies

- **Compose Multiplatform**: UI framework
- **Google Drive API**: Upload functionality
- **Kotlin Coroutines**: Async operations
- **Java Preferences API**: Settings storage

## Troubleshooting

### "Failed to connect" Error

- Verify your OAuth credentials JSON file path is correct
- Check that the JSON file is valid and not corrupted
- Ensure Google Drive API is enabled in your Cloud project
- Make sure you added yourself as a test user in OAuth consent screen

### "Upload failed" Error

- Verify the Folder ID is correct (if specified)
- Check that you authorized the app in your browser
- Ensure you're signed in with the Google account you added as a test user
- Try removing authorization and re-authorizing: delete `~/.driveit` folder and try again

### "Compression failed" Error

- Check that you have read permissions for the selected folder
- Ensure you have enough disk space for the temporary ZIP file
- Verify the folder is not empty

## Security Notes

- **Credentials Location**: `DriveIt/app/credentials/credentials.json`
- The `.gitignore` is configured to never commit credentials
- Authorization tokens are stored in `app/credentials/StoredCredential`
- Never share your `credentials.json` file
- No credentials are sent anywhere except Google's servers
- You can revoke access anytime from [Google Account Settings](https://myaccount.google.com/permissions)

## Quick Start Summary

1. Download OAuth credentials from Google Cloud Console
2. Save as `DriveIt/app/credentials/credentials.json`
3. Run: `./gradlew run`
4. Select folder, click upload
5. Authorize in browser (first time only)
6. Done!

## License

MIT License - Feel free to modify and distribute

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Verify your Google Cloud setup is correct
3. Ensure all prerequisites are met
