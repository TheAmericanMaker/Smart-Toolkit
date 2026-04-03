# Releasing Smart Toolkit

This project supports two primary build paths:

- `assembleDebug` for local testing and APK artifacts
- `bundleRelease` for Google Play publishing

## Prerequisites

- JDK 17
- Android SDK Platform 35
- a release signing key
- optional AdMob and Google Play Billing configuration if you want monetization enabled in release builds

## Optional monetization configuration

Release builds only enable monetization when all required Gradle properties are present:

```properties
SMART_TOOLKIT_ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
SMART_TOOLKIT_ADMOB_BANNER_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
SMART_TOOLKIT_REMOVE_ADS_PRODUCT_ID=remove_ads
```

Recommended locations:

- `~/.gradle/gradle.properties`
- CI secrets exposed as environment variables

If these values are absent, release builds still compile, but ads and Google Play Billing are disabled.

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
- Confirm AdMob and billing product IDs point at production configuration if monetization is enabled.
- Run the verification commands above.
- Draft GitHub release notes and attach the release artifacts you want to distribute.
- Upload the `.aab` to Google Play for store distribution.
