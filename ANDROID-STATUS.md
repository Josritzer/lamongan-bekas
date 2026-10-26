# Lamongan Bekas Android

## Current phase
Native Android application scaffold using Kotlin and Jetpack Compose. The app is built independently from the existing React website.

## Build
GitHub Actions workflow: `.github/workflows/android-apk.yml`
- Run manually from Actions, or push changes to `feat/android-native-apk`.
- Download artifact `lamongan-bekas-debug-apk` from a successful run.

## Current limitations
- The UI is a native first-pass shell.
- No production API URL is configured because the website/backend has not been deployed successfully.
- No real listings are shown yet; no sample listing data is fabricated.
- Authentication, posting ads, photo uploads, messaging, and reporting are not implemented.
- Neon credentials must remain server-side and must never be embedded in the APK.

## Next steps
1. Build and verify the APK with GitHub Actions.
2. Decide and deploy a backend API that can connect to Neon.
3. Configure the public API base URL and implement network loading, search, and category filtering.
4. Add account and listing creation only after server-side identity and authorization rules are ready.
