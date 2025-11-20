# Campus Helper 2 - Android App

AI-powered student assistant app with MVVM architecture, Retrofit, Hilt DI, and Material Design 3.

# CampusHelper - Android App

Your Complete Academic Companion

## Features
- AI-Generated Practice Tests
- Progress Tracking & Analytics
- Competitive Exam Preparation (JEE, NEET, CAT, UPSC, GATE)
- Study Materials Management
- Minimalist UI/UX Design

## Tech Stack
- Kotlin
- MVVM Architecture
- Hilt Dependency Injection
- Retrofit for API calls
- Material Design 3
- MPAndroidChart for analytics

## Setup
1. Clone the repository
2. Open in Android Studio
3. Update `local.properties` with your SDK path
4. Build and run

## Project Status

✅ **Core Setup Complete**
- Gradle build files configured
- Hilt Dependency Injection setup
- Retrofit + OkHttp network layer
- Data models (User, Subject, Test, Progress, CompetitiveExam, AiChat)
- Repositories (Auth, Subject, Test)
- SessionManager for JWT token storage
- Constants and Resource helper classes

## Architecture

- **MVVM** (Model-View-ViewModel)
- **Hilt** for Dependency Injection
- **Retrofit** for REST API calls
- **Coroutines** for async operations
- **ViewBinding** for type-safe view access
- **Navigation Component** for screen navigation
- **Material Design 3** for UI

## Backend Connection

Update `Constants.kt` BASE_URL to point to your backend:
- Emulator: `http://10.0.2.2:5001/api/`
- Physical device: `http://YOUR_IP:5001/api/`

Your backend is already running at port 5001 with seeded data:
- Admin: `admin@campushelper.com` / `Admin@123`
- Student: `john@student.com` / `Student@123`

## Remaining Files to Create

### 1. ViewModels
- `ui/viewmodel/AuthViewModel.kt`
- `ui/viewmodel/SubjectViewModel.kt`
- `ui/viewmodel/TestViewModel.kt`
- `ui/viewmodel/ProgressViewModel.kt`
- `ui/viewmodel/ExamViewModel.kt`

### 2. Activities
- `ui/auth/SplashActivity.kt`
- `ui/auth/LoginActivity.kt`
- `ui/auth/RegisterActivity.kt`
- `ui/student/StudentDashboardActivity.kt`
- `ui/student/SubjectDetailActivity.kt`
- `ui/student/AiChatActivity.kt`
- `ui/student/PracticeTestActivity.kt`
- `ui/student/TestResultActivity.kt`
- `ui/student/ExamEnrollActivity.kt`
- `ui/student/ExamProgressActivity.kt`
- `ui/admin/AdminDashboardActivity.kt`

### 3. Fragments
- `ui/student/HomeFragment.kt` (subjects list)
- `ui/student/ProgressFragment.kt`
- `ui/student/CompetitiveExamsFragment.kt`
- `ui/student/ProfileFragment.kt`

### 4. Adapters
- `ui/adapter/SubjectAdapter.kt`
- `ui/adapter/ChatMessageAdapter.kt`
- `ui/adapter/TestQuestionAdapter.kt`
- `ui/adapter/CompetitiveExamAdapter.kt`
- `ui/adapter/ProgressAdapter.kt`

### 5. Layouts (XML)
All activity and fragment layouts following Material Design 3.

### 6. Additional Repositories
- `data/repository/ProgressRepository.kt`
- `data/repository/ExamRepository.kt`
- `data/repository/AiRepository.kt`

### 7. Resources
- Themes, colors, strings
- Vector icons (ic_ai, ic_book, ic_exam, ic_progress, ic_profile)

## Build Instructions

1. Open `CampusHelper2` folder in Android Studio
2. Sync Gradle (should download dependencies automatically)
3. Update `Constants.BASE_URL` if needed
4. Build: `./gradlew assembleDebug`
5. Run on emulator or device

## Dependencies Included

- AndroidX Core, AppCompat, Material Design
- Lifecycle, ViewModel, LiveData
- Navigation Component
- Retrofit, OkHttp, Gson
- Coroutines
- Hilt (DI)
- Glide (images)
- DataStore (preferences)
- MPAndroidChart (graphs)

## Next Steps

The foundation is ready! You need to:

1. Create ViewModels to handle business logic
2. Create Activities/Fragments with ViewBinding
3. Create XML layouts with Material Design
4. Create RecyclerView adapters
5. Wire everything together with Navigation Component

Would you like me to continue creating the remaining files?
