#!/bin/bash

# DriveIt Installed App Setup Script
# This script helps set up credentials for the installed DriveIt app

CREDENTIALS_DIR="$HOME/.driveit"
CREDENTIALS_FILE="$CREDENTIALS_DIR/credentials.json"

echo "🔧 DriveIt Setup (Installed App)"
echo "================================="
echo ""

# Create credentials directory if it doesn't exist
if [ ! -d "$CREDENTIALS_DIR" ]; then
    echo "Creating credentials directory..."
    mkdir -p "$CREDENTIALS_DIR"
    echo "✅ Created: $CREDENTIALS_DIR"
else
    echo "✅ Directory exists: $CREDENTIALS_DIR"
fi

echo ""

# Check if credentials file exists
if [ -f "$CREDENTIALS_FILE" ]; then
    echo "✅ Credentials file already exists at: $CREDENTIALS_FILE"
    echo ""
    read -p "Do you want to replace it? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Keeping existing credentials."
        exit 0
    fi
fi

echo ""
echo "📥 Looking for downloaded credentials in ~/Downloads..."
echo ""

# Find potential credentials files in Downloads
DOWNLOADED_FILES=$(find ~/Downloads -name "client_secret_*.json" -o -name "credentials*.json" 2>/dev/null | head -5)

if [ -z "$DOWNLOADED_FILES" ]; then
    echo "❌ No credentials files found in ~/Downloads"
    echo ""
    echo "Please:"
    echo "1. Download OAuth 2.0 credentials from Google Cloud Console"
    echo "2. Save the file to your Downloads folder"
    echo "3. Run this script again"
    echo ""
    echo "Or manually copy your credentials file:"
    echo "   cp /path/to/your/credentials.json $CREDENTIALS_FILE"
    exit 1
fi

echo "Found the following files:"
echo "$DOWNLOADED_FILES" | nl
echo ""

# If only one file, use it automatically
FILE_COUNT=$(echo "$DOWNLOADED_FILES" | wc -l | tr -d ' ')
if [ "$FILE_COUNT" -eq 1 ]; then
    SOURCE_FILE="$DOWNLOADED_FILES"
    echo "Using: $SOURCE_FILE"
else
    echo "Enter the number of the file to use (1-$FILE_COUNT):"
    read -r FILE_NUMBER
    SOURCE_FILE=$(echo "$DOWNLOADED_FILES" | sed -n "${FILE_NUMBER}p")
fi

if [ -z "$SOURCE_FILE" ] || [ ! -f "$SOURCE_FILE" ]; then
    echo "❌ Invalid selection or file not found"
    exit 1
fi

# Copy the file
echo ""
echo "Copying credentials..."
cp "$SOURCE_FILE" "$CREDENTIALS_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Success! Credentials installed at: $CREDENTIALS_FILE"
    echo ""
    echo "You can now launch DriveIt from your Applications folder!"
    echo ""
    echo "📝 On first use, you'll need to authorize the app in your browser."
else
    echo "❌ Failed to copy credentials"
    exit 1
fi
