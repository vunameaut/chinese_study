# Fix Supabase RLS Policy Violation

The application fails to create sessions and vocabulary entries with a `UnauthorizedRestException: new row violates row-level security policy` error. This is likely because the app is sending default values for system-managed columns like `id` and `created_at` (e.g., `id: 0`), which may conflict with Row-Level Security (RLS) policies or constraints on the Supabase backend.

## Proposed Changes

### Data Layer

#### [MODIFY] [SupabaseDataSource.kt](file:///D:/app/study_chines/app/src/main/java/vhn/dev/study_chines/data/remote/SupabaseDataSource.kt)
- Update `createSession` to use a `Map` for insertion, sending only the `title` field. This allows Supabase to automatically handle `id` and `created_at` using its own defaults and identity logic.
- Update `insertVocabulary` to use a `Map` for insertion, omitting the `id` and `created_at` fields.

#### [MODIFY] [EntryViewModel.kt](file:///D:/app/study_chines/app/src/main/java/vhn/dev/study_chines/ui/entry/EntryViewModel.kt)
- Remove the manual setting of `createdAt` when creating a `VocabularyDto` to be inserted, letting the database handle it.

## Verification Plan

### Manual Verification
- Deploy the app to a device/emulator.
- Try to create a new session from the Home screen.
- Try to add a new vocabulary word within a session.
- Verify that the operations succeed and no "UnauthorizedRestException" is thrown.
