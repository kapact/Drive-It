# DriveIt Credentials

This directory stores your Google OAuth credentials.

## Setup Instructions

1. Download your OAuth 2.0 credentials from Google Cloud Console
2. Rename the downloaded file to `credentials.json`
3. Place it in this directory

## What Goes Here

- `credentials.json` - Your OAuth 2.0 client credentials (from Google Cloud Console)
- `StoredCredential` - Automatically generated after first authorization (stores refresh token)

## Security

⚠️ **DO NOT** commit `credentials.json` to version control
- This directory is already in `.gitignore`
- Keep your credentials file secure

## Getting Credentials

See the main project README.md for detailed instructions on:
1. Creating a Google Cloud Project
2. Enabling Google Drive API
3. Creating OAuth 2.0 credentials
4. Downloading the credentials JSON file

## Expected File Location

```
DriveIt/
└── credentials/
    ├── credentials.json     ← Place your downloaded file here
    ├── StoredCredential     ← Auto-generated after first login
    └── README.md            ← This file
```
