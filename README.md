<div align="center">

# 🎓 CampusHelper

### Your Complete Academic Companion

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**An intelligent educational platform for students featuring AI-powered practice tests, comprehensive progress tracking, and competitive exam preparation.**

[Features](#-features) • [Screenshots](#-screenshots) • [Tech Stack](#-tech-stack) • [Architecture](#-architecture) • [Installation](#-installation) • [API Documentation](#-api-documentation)

</div>

---

## 📱 Features

### 🎯 For Students

<table>
<tr>
<td width="50%">

#### 📚 **Study Materials Management**
- Browse subjects by category
- Access notes, videos, books, and practice materials
- Download and view resources offline
- Organized by semester and credits

</td>
<td width="50%">

#### 🧪 **AI-Powered Practice Tests**
- Topic-based test generation using Google Gemini AI
- Multiple choice questions with 4 options
- Real-time answer tracking
- Instant results and detailed scoring
- Test history and performance analysis

</td>
</tr>
<tr>
<td width="50%">

#### 📊 **Progress Analytics**
- Visual performance dashboards
- Subject-wise performance tracking (Bar charts)
- Overall accuracy metrics (Pie charts)
- Performance trend analysis (Line graphs)
- Study streak monitoring
- Average score calculations

</td>
<td width="50%">

#### 🏆 **Competitive Exam Preparation**
- Support for major exams:
  - 📘 JEE (Joint Entrance Examination)
  - 🏥 NEET (Medical Entrance)
  - 💼 CAT (Management)
  - 🏛️ UPSC (Civil Services)
  - 🎓 GATE (Engineering)
  - ⚔️ CDS & NDA (Defense)
- Custom question patterns per exam
- Category-based organization

</td>
</tr>
</table>

### 👨‍💼 For Admins

- **Content Management**: Upload and manage study materials
- **Subject Creation**: Add new subjects with details
- **Exam Management**: Configure competitive exam patterns
- **User Management**: Monitor student activities

---

## 🎨 Screenshots

<div align="center">

| Splash Screen | Login | Student Dashboard | Profile |
|:---:|:---:|:---:|:---:|
| <img src="screenshots/splash.png" width="200"/> | <img src="screenshots/login.png" width="200"/> | <img src="screenshots/home.png" width="200"/> | <img src="screenshots/profile.png" width="200"/> |

| Subjects | Practice Test | Progress Analytics | Competitive Exams |
|:---:|:---:|:---:|:---:|
| <img src="screenshots/subjects.png" width="200"/> | <img src="screenshots/test.png" width="200"/> | <img src="screenshots/progress.png" width="200"/> | <img src="screenshots/exams.png" width="200"/> |

</div>

---

## 🏗️ Architecture

### **MVVM Pattern**

```
┌─────────────────────────────────────────────────────────┐
│                         VIEW                            │
│  (Activities, Fragments, XML Layouts)                   │
│  • LoginActivity                                        │
│  • StudentDashboardActivity                             │
│  • PracticeTestActivity                                 │
│  • Fragments: Home, Progress, Exams, Profile            │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ observes LiveData/StateFlow
                   ▼
┌─────────────────────────────────────────────────────────┐
│                      VIEWMODEL                          │
│  (Business Logic, State Management)                     │
│  • AuthViewModel                                        │
│  • SubjectViewModel                                     │
│  • TestViewModel                                        │
│  • ProgressViewModel                                    │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ calls repository methods
                   ▼
┌─────────────────────────────────────────────────────────┐
│                     REPOSITORY                          │
│  (Data Management Layer)                                │
│  • AuthRepository                                       │
│  • SubjectRepository                                    │
│  • TestRepository                                       │
│  • ProgressRepository                                   │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ uses API service
                   ▼
┌─────────────────────────────────────────────────────────┐
│                    DATA SOURCE                          │
│  (Network Layer - Retrofit)                             │
│  • ApiService Interface                                 │
│  • Retrofit Client                                      │
│  • OkHttp Interceptors                                  │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ REST API calls
                   ▼
┌─────────────────────────────────────────────────────────┐
│                   BACKEND SERVER                        │
│  (Node.js + Express + MongoDB)                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 Data Flow Diagram

```mermaid
graph TB
    subgraph "Android App (Frontend)"
        A[User Interface] -->|User Actions| B[ViewModel]
        B -->|LiveData/StateFlow| A
        B -->|Request Data| C[Repository]
        C -->|Response| B
        C -->|API Calls| D[Retrofit Service]
        D -->|HTTP Response| C
    end
    
    subgraph "Backend Server"
        D -->|REST API| E[Express Routes]
        E -->|Auth Check| F[Middleware]
        F -->|Valid Token| G[Controller]
        G -->|CRUD Operations| H[MongoDB]
        H -->|Data| G
        G -->|AI Request| I[Gemini AI Service]
        I -->|Generated Questions| G
        G -->|JSON Response| E
    end
    
    E -->|JSON Data| D
    
    style A fill:#E3F2FD
    style B fill:#BBDEFB
    style C fill:#90CAF9
    style D fill:#64B5F6
    style E fill:#FFE0B2
    style F fill:#FFCC80
    style G fill:#FFB74D
    style H fill:#81C784
    style I fill:#BA68C8
```

---

## 🛠️ Tech Stack

### **Frontend (Android)**

| Technology | Purpose | Version |
|------------|---------|---------|
| ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white) | Primary Language | Latest |
| ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) | Platform | API 24+ |
| ![Jetpack](https://img.shields.io/badge/Jetpack-4285F4?style=flat&logo=android&logoColor=white) | Architecture Components | Latest |
| ![Material Design](https://img.shields.io/badge/Material_Design_3-757575?style=flat&logo=material-design&logoColor=white) | UI Framework | 1.10.0 |
| ![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=flat) | REST API Client | 2.9.0 |
| ![Hilt](https://img.shields.io/badge/Hilt-FF6F00?style=flat) | Dependency Injection | 2.48 |
| ![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=flat) | Async Operations | 1.7.3 |
| ![MPAndroidChart](https://img.shields.io/badge/MPAndroidChart-00C853?style=flat) | Data Visualization | 3.1.0 |
| ![Glide](https://img.shields.io/badge/Glide-4285F4?style=flat) | Image Loading | 4.15.1 |

### **Backend**

| Technology | Purpose |
|------------|---------|
| ![Node.js](https://img.shields.io/badge/Node.js-339933?style=flat&logo=node.js&logoColor=white) | Runtime Environment |
| ![Express](https://img.shields.io/badge/Express-000000?style=flat&logo=express&logoColor=white) | Web Framework |
| ![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat&logo=mongodb&logoColor=white) | NoSQL Database |
| ![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=json-web-tokens&logoColor=white) | Authentication |
| ![Gemini AI](https://img.shields.io/badge/Gemini_AI-8E75B2?style=flat&logo=google&logoColor=white) | AI Test Generation |

---

## 📦 Project Structure

```
CampusHelper2/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/campushelper/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/          # Data classes (User, Subject, Test, etc.)
│   │   │   │   │   ├── remote/         # API service interfaces
│   │   │   │   │   └── repository/     # Repository implementations
│   │   │   │   ├── di/                 # Hilt modules
│   │   │   │   ├── ui/
│   │   │   │   │   ├── activity/       # Activities
│   │   │   │   │   ├── adapter/        # RecyclerView adapters
│   │   │   │   │   ├── auth/           # Login, Register
│   │   │   │   │   ├── student/        # Student dashboard & fragments
│   │   │   │   │   ├── admin/          # Admin panel
│   │   │   │   │   └── viewmodel/      # ViewModels
│   │   │   │   └── utils/              # Helper classes, constants
│   │   │   └── res/
│   │   │       ├── layout/             # XML layouts
│   │   │       ├── drawable/           # Images, vectors, gradients
│   │   │       ├── values/             # Colors, strings, themes
│   │   │       └── navigation/         # Navigation graphs
│   │   └── build.gradle                # App dependencies
│   └── build.gradle                    # Project configuration
└── gradle/                             # Gradle wrapper
```

---

## 🗄️ Database Schema

### **MongoDB Collections**

#### **Users Collection**
```json
{
  "_id": "ObjectId",
  "name": "String",
  "email": "String (unique)",
  "password": "String (hashed)",
  "role": "String (student/admin)",
  "profileImage": "String (URL)",
  "createdAt": "Date",
  "updatedAt": "Date"
}
```

#### **Subjects Collection**
```json
{
  "_id": "ObjectId",
  "name": "String",
  "code": "String (unique)",
  "description": "String",
  "category": "String",
  "semester": "Number",
  "credits": "Number",
  "createdBy": "ObjectId (ref: User)",
  "createdAt": "Date"
}
```

#### **Tests Collection**
```json
{
  "_id": "ObjectId",
  "userId": "ObjectId (ref: User)",
  "subjectId": "ObjectId (ref: Subject)",
  "topic": "String",
  "questions": [
    {
      "question": "String",
      "options": {
        "A": "String",
        "B": "String",
        "C": "String",
        "D": "String"
      },
      "correctAnswer": "String",
      "explanation": "String"
    }
  ],
  "userAnswers": ["Number"],
  "score": "Number",
  "totalQuestions": "Number",
  "completedAt": "Date"
}
```

#### **Progress Collection**
```json
{
  "_id": "ObjectId",
  "userId": "ObjectId (ref: User)",
  "totalTests": "Number",
  "averageScore": "Number",
  "subjectProgress": [
    {
      "subjectId": "ObjectId",
      "subjectName": "String",
      "testsAttempted": "Number",
      "averageScore": "Number"
    }
  ],
  "studyStreak": {
    "current": "Number",
    "longest": "Number"
  }
}
```

#### **Competitive Exams Collection**
```json
{
  "_id": "ObjectId",
  "name": "String",
  "shortName": "String",
  "description": "String",
  "category": "String",
  "subjects": ["String"],
  "questionPattern": {
    "totalQuestions": "Number",
    "duration": "Number",
    "difficulty": "String"
  }
}
```

---

## 🚀 Installation

### **Prerequisites**

- Android Studio Arctic Fox or newer
- JDK 11 or higher
- Android SDK API 24+
- Git

### **Steps**

1. **Clone the Repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/CampusHelper-Android.git
   cd CampusHelper-Android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select `File > Open`
   - Navigate to the cloned directory
   - Click `OK`

3. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Install any missing dependencies

4. **Configure Backend URL**
   - Open `app/src/main/java/com/campushelper/app/utils/Constants.kt`
   - Update `BASE_URL` with your backend server URL:
   ```kotlin
   const val BASE_URL = "https://your-backend-url.com/api/"
   ```

5. **Build & Run**
   - Connect Android device or start emulator
   - Click `Run > Run 'app'`
   - Or press `Shift + F10`

---

## 🔌 API Documentation

### **Base URL**
```
https://campushelper-be.onrender.com/api
```

### **Authentication Endpoints**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | ❌ |
| POST | `/auth/login` | User login | ❌ |
| GET | `/auth/profile` | Get user profile | ✅ |
| PUT | `/auth/profile` | Update profile | ✅ |
| PUT | `/auth/change-password` | Change password | ✅ |

### **Subject Endpoints**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/subjects` | Get all subjects | ✅ |
| GET | `/subjects/:id` | Get subject by ID | ✅ |
| POST | `/subjects` | Create subject (Admin) | ✅ |
| PUT | `/subjects/:id` | Update subject (Admin) | ✅ |
| DELETE | `/subjects/:id` | Delete subject (Admin) | ✅ |

### **Test Endpoints**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/tests/generate` | Generate AI test | ✅ |
| POST | `/tests/submit` | Submit test answers | ✅ |
| GET | `/tests/:id` | Get test details | ✅ |
| GET | `/tests/history` | Get test history | ✅ |

### **Progress Endpoints**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/progress` | Get user progress | ✅ |
| GET | `/progress/stats` | Get statistics | ✅ |

### **Competitive Exam Endpoints**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/competitive-exams` | Get all exams | ✅ |
| GET | `/competitive-exams/:id` | Get exam by ID | ✅ |
| POST | `/competitive-exams/generate` | Generate exam test | ✅ |

---

## 🎨 Design System

### **Color Palette**

```kotlin
// Primary Colors
val Primary = Color(0xFF6200EE)
val PrimaryVariant = Color(0xFF3700B3)
val Secondary = Color(0xFF03DAC6)

// Background Colors
val BackgroundCard = Color(0x30FFFFFF)      // 30% white (minimalist)
val BackgroundGradientStart = Color(0xFF667EEA)
val BackgroundGradientEnd = Color(0xFF764BA2)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xB3FFFFFF)       // 70% white

// Accent Colors
val Success = Color(0xFF4CAF50)
val Error = Color(0xFFF44336)
val Warning = Color(0xFFFF9800)
val Info = Color(0xFF2196F3)
```

### **Typography**

```kotlin
// Font Family
fontFamily = FontFamily.SansSerif.Medium

// Font Sizes
Title = 26.sp
Heading = 18.sp
Body = 16.sp
Caption = 13.sp
Small = 11.sp

// Letter Spacing
SectionTitle = 0.15.sp
```

### **Design Principles**

- **Minimalist Flat Design**: 0dp elevation, no shadows
- **Large Corner Radius**: 28dp for modern look
- **Transparent Backgrounds**: #30FFFFFF for glass effect
- **Emoji Icons**: Visual interest without heavy graphics
- **Consistent Spacing**: 16dp, 20dp, 24dp padding
- **Material Design 3**: Following latest guidelines

---

## 🧪 Testing

### **Unit Tests**
```bash
./gradlew test
```

### **Instrumentation Tests**
```bash
./gradlew connectedAndroidTest
```

### **Test Coverage**
- ViewModels: Business logic validation
- Repositories: Data layer testing
- API Services: Network call mocking
- UI Components: Espresso tests

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create your feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

### **Coding Standards**
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features
- Update documentation

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 CampusHelper

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 📞 Contact & Support

<div align="center">

### **Need Help?**

📧 **Email**: support@campushelper.com  
🌐 **Website**: [www.campushelper.com](https://www.campushelper.com)  
💬 **Discord**: [Join our community](https://discord.gg/campushelper)  
🐛 **Bug Reports**: [GitHub Issues](https://github.com/YOUR_USERNAME/CampusHelper-Android/issues)

---

### **Connect With Us**

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/YOUR_USERNAME)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/YOUR_PROFILE)
[![Twitter](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/YOUR_HANDLE)

---

**Made with ❤️ by the CampusHelper Team**

⭐ **Star this repository if you found it helpful!**

</div>

---

## 🗺️ Roadmap

### **Version 1.0** (Current)
- [x] User Authentication
- [x] Practice Test Generation
- [x] Progress Tracking
- [x] Competitive Exam Support
- [x] Material Design UI

### **Version 1.1** (Planned)
- [ ] Offline Mode
- [ ] Push Notifications
- [ ] Dark Theme
- [ ] Multi-language Support
- [ ] Social Features (Study Groups)

### **Version 2.0** (Future)
- [ ] Video Lectures Integration
- [ ] Live Doubt Sessions
- [ ] Gamification (Badges, Leaderboards)
- [ ] AI Study Planner
- [ ] Parent Dashboard

---

## 🙏 Acknowledgments

- **Google Gemini AI** - For intelligent test generation
- **MPAndroidChart** - Beautiful chart library
- **Material Design** - Design system and components
- **Retrofit** - REST API client
- **Hilt** - Dependency injection framework
- All open-source contributors

---

<div align="center">

### **⚡ Built with cutting-edge technology for modern education**

**CampusHelper** | Empowering Students, Enabling Success

</div>
