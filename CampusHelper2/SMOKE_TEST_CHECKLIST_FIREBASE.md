# Firebase Smoke Test Checklist (CampusHelper2)

Date: 2026-04-20
Scope: Firebase-only architecture (Auth, Realtime Database, Storage), Cloud Function AI endpoint, competitive exams hidden.

## 1) Preconditions

- Build works from project root `CampusHelper2`:
  - `./gradlew.bat :app:compileDebugKotlin --no-daemon`
  - `./gradlew.bat :app:assembleDebug --no-daemon`
- `google-services.json` exists at `app/google-services.json`
- Firebase Realtime Database rules permit required read/write for authenticated user flows
- Optional AI setup:
  - Set `AI_FUNCTION_URL` in `app/build.gradle.kts`
  - Endpoint accepts JSON: `{ subjectId, topic, question }`
  - If protected, accepts Firebase ID token via `Authorization: Bearer <token>`

## 2) Install + Launch (Device)

- Connect device/emulator and verify:
  - `adb devices`
- Install debug build:
  - `./gradlew.bat :app:installDebug --no-daemon`
- Launch app:
  - `adb shell am start -n com.campushelper.app/.ui.auth.SplashActivity`

## 3) Student Flow Smoke

### A. Login

- Open login screen and sign in with a valid account
- Expected:
  - Login succeeds
  - Student dashboard opens
  - Session persists on app relaunch

### B. Subjects (Home)

- Open Home tab
- Expected:
  - Subject list loads from Firebase Realtime DB
  - Tapping a subject opens Subject Detail

### C. Materials (Subject Detail)

- In Subject Detail, verify materials list
- Test each type if available:
  - YouTube: opens app/browser
  - Link: opens browser
  - PDF: opens browser/download path
  - Notes: content visible
- Expected:
  - No backend API calls required
  - Data loads from Firebase paths

### D. Practice Test + Submit

- In subject detail, tap Practice Test
- Choose subject/topic and start test
- Submit answers
- Expected:
  - Test generated and stored under Firebase tests path
  - Result screen shows score/correct/total
  - Test history includes submitted test

### E. Progress UI

- Open Progress tab
- Expected:
  - Aggregated values reflect submitted tests
  - Subject-wise progress updates
  - Streak updates based on completion dates

### F. AI Chat (if configured)

- Open AI chat from subject
- Ask a question
- Expected when `AI_FUNCTION_URL` is configured:
  - Request hits Cloud Function endpoint
  - Response appears in chat
- Expected when not configured:
  - User-friendly error that endpoint is not configured

## 4) Admin Flow Smoke

### A. Subject management

- Add subject
- Edit subject
- Delete subject
- Expected:
  - CRUD works against Firebase Realtime DB

### B. Material management

- Add URL material
- Add PDF material
- Edit/delete material
- Expected:
  - Metadata in Realtime DB
  - PDF in Firebase Storage

### C. Users listing

- Open View Users
- Expected:
  - User list loads from Firebase users node

### D. Competitive exams hidden

- Student bottom navigation: no Exams tab
- Admin dashboard: exams card hidden
- Direct open of Manage Exams activity exits with migration notice

## 5) Regression Checks

- App relaunch keeps session
- Logout clears session and returns to login
- No crash in:
  - Subject detail load
  - Material open handlers
  - Test submit/result
  - Progress tab render

## 6) Known Environment Constraints

- If `adb` is not on PATH, use SDK-local path:
  - `<sdk.dir>/platform-tools/adb.exe`
- If Gradle fails due JDK mismatch, run with temporary `JAVA_HOME=C:\Program Files\Java\jdk-24`

## 7) Pass/Fail Template

- Login: PASS/FAIL
- Subjects: PASS/FAIL
- Materials: PASS/FAIL
- Test submit: PASS/FAIL
- Progress UI: PASS/FAIL
- AI chat: PASS/FAIL/NOT CONFIGURED
- Admin subjects/materials: PASS/FAIL
- Competitive hidden behavior: PASS/FAIL
- Overall: PASS/FAIL
