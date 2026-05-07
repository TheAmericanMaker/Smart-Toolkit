# Releasing Smart Toolkit

This project supports two primary build paths:

- `assembleDebug` for local testing and APK artifacts
- `bundleRelease` for Google Play publishing

## Prerequisites

- JDK 17
- Android SDK Platform 35
- a release signing key

## Versioning

Before cutting a release, update these values in `app/build.gradle.kts`:

- `versionCode`
- `versionName`

Also check:

- the release notes file for the version you are publishing
- any user-facing copy that mentions a specific version

## Verification

Run the following before publishing:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew bundleRelease
```

On Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`.

Expected outputs:

- debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- release bundle: `app/build/outputs/bundle/release/app-release.aab`

If you need a signed side-loadable release APK for testing, run:

```bash
./gradlew assembleRelease
```

## Release checklist

- Confirm `targetSdk` still matches current Google Play requirements.
- Confirm the release is signed with the correct key.
- Run the verification commands above.
- Draft GitHub release notes and attach the release artifacts you want to distribute.
- Upload the `.aab` to Google Play for store distribution.
