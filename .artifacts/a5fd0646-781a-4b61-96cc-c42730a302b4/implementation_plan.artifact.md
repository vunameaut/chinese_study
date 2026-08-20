# Fix Missing Supabase Anon Key

The application fails to start because `SupabaseClientProvider` expects `BuildConfig.SUPABASE_ANON_KEY` to be non-blank, but the Gradle build script is only looking for it in Gradle properties (e.g., `gradle.properties`), while it is currently defined in `local.properties`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///D:/app/study_chines/app/build.gradle.kts)
- Add logic to load `local.properties`.
- Update `SUPABASE_URL` and `SUPABASE_ANON_KEY` build config fields to check both Gradle properties and `local.properties`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project builds and `BuildConfig` is generated correctly.

### Manual Verification
- Deploy the app to a device/emulator and verify that the "Error creating session" message no longer appears.
