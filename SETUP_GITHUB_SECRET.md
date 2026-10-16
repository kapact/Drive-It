# Setting Up GitHub Secret for Credentials

GitHub blocked pushing credentials.json to the repository (which is good for security!).

Instead, we'll use **GitHub Secrets** to securely store your OAuth credentials and inject them during the build process.

## Step-by-Step Instructions

### 1. Copy your credentials.json content

```bash
cat app/src/main/resources/credentials.json
```

Copy the entire output (it should be JSON like `{"installed":{...}}`).

### 2. Add it as a GitHub Secret

1. Go to your repository: https://github.com/kapact/Drive-It
2. Click **Settings** (top menu)
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click the green **New repository secret** button
5. Fill in:
   - **Name**: `GOOGLE_CREDENTIALS_JSON`
   - **Secret**: Paste the entire contents of your credentials.json file
6. Click **Add secret**

### 3. Push the updated workflow

```bash
git add .github/workflows/build-packages.yml .gitignore
git commit -m "Use GitHub Secrets for credentials"
git push
```

### 4. Watch the build

1. Go to https://github.com/kapact/Drive-It/actions
2. The workflow will automatically start
3. This time it will:
   - Create credentials.json from the secret
   - Bundle it into the app
   - Build successfully for all platforms!

## How It Works

The updated GitHub Actions workflow now:

1. **Before building**, it runs:
   ```yaml
   - name: Create credentials file
     run: |
       mkdir -p app/src/main/resources
       echo '${{ secrets.GOOGLE_CREDENTIALS_JSON }}' > app/src/main/resources/credentials.json
   ```

2. **Builds the app** with credentials included

3. **Credentials are bundled** into the MSI/DMG/DEB files

4. **Credentials never appear in Git history** - they stay secret!

## Security Notes

✅ **Secure**: Credentials are stored encrypted in GitHub Secrets
✅ **Safe**: They're only accessible during builds, never exposed in logs
✅ **Proper**: This is the recommended way to handle secrets in CI/CD

## For Local Development

For running the app locally during development, you still have:
- `app/credentials/credentials.json` (gitignored, for local use only)

For packaged apps (DMG, MSI, DEB):
- Credentials are injected from GitHub Secrets during build
- End users get fully working apps with credentials pre-bundled

---

## Next Steps

After adding the secret:

1. Push the workflow changes (command above)
2. Wait ~5 minutes for build to complete
3. Download Windows MSI from Actions → Artifacts
4. It will work without "App not properly configured" error!
