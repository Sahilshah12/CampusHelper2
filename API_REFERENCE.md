# 📡 API Reference - Campus Helper Backend

## Base URL
```
Development: http://10.0.2.2:5001/api
Production: https://your-domain.com/api
```

---

## Authentication Endpoints

### 1. Register User
**POST** `/auth/register`

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "student"
}
```

**Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "60d5ec49f1b2c72b8c8e4f5a",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "student"
  }
}
```

### 2. Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "student@test.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "60d5ec49f1b2c72b8c8e4f5a",
    "name": "Test Student",
    "email": "student@test.com",
    "role": "student"
  }
}
```

---

## Subject Endpoints

### 3. Get All Subjects
**GET** `/subjects`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
[
  {
    "id": "60d5ec49f1b2c72b8c8e4f5b",
    "name": "Physics",
    "code": "PHY101",
    "description": "Introduction to Physics",
    "instructor": "Dr. Smith",
    "semester": "Fall 2024",
    "credits": 3
  }
]
```

### 4. Get Subject by ID
**GET** `/subjects/:id`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "id": "60d5ec49f1b2c72b8c8e4f5b",
  "name": "Physics",
  "code": "PHY101",
  "description": "Introduction to Physics",
  "instructor": "Dr. Smith",
  "semester": "Fall 2024",
  "credits": 3
}
```

### 5. Create Subject (Admin only)
**POST** `/subjects`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "name": "Chemistry",
  "code": "CHEM101",
  "description": "Basic Chemistry",
  "instructor": "Dr. Jones",
  "semester": "Fall 2024",
  "credits": 3
}
```

---

## Material Endpoints

### 6. Get Materials by Subject
**GET** `/materials/subject/:subjectId`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
[
  {
    "id": "60d5ec49f1b2c72b8c8e4f5c",
    "subjectId": "60d5ec49f1b2c72b8c8e4f5b",
    "title": "Chapter 1 - Newton's Laws",
    "description": "Introduction to Newton's Laws of Motion",
    "type": "pdf",
    "url": "https://example.com/materials/physics-ch1.pdf",
    "uploadedBy": "60d5ec49f1b2c72b8c8e4f5a",
    "uploadedAt": 1634567890000
  }
]
```

### 7. Upload Material (Admin only)
**POST** `/materials`

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Form Data:**
```
subjectId: "60d5ec49f1b2c72b8c8e4f5b"
title: "Chapter 1 Notes"
description: "Detailed notes"
type: "pdf"
file: [binary file data]
```

---

## AI Chat Endpoints

### 8. Send Chat Message
**POST** `/ai/chat`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "message": "Explain Newton's first law",
  "subjectId": "60d5ec49f1b2c72b8c8e4f5b"
}
```

**Response (200):**
```json
{
  "response": "Newton's first law states that an object at rest stays at rest and an object in motion stays in motion with the same speed and in the same direction unless acted upon by an unbalanced force. This is also known as the law of inertia...",
  "timestamp": 1634567890000
}
```

---

## Test Endpoints

### 9. Generate Test
**POST** `/tests/generate`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "subjectId": "60d5ec49f1b2c72b8c8e4f5b",
  "questionCount": 10
}
```

**Response (200):**
```json
{
  "id": "60d5ec49f1b2c72b8c8e4f5d",
  "subjectId": "60d5ec49f1b2c72b8c8e4f5b",
  "questions": [
    {
      "id": "q1",
      "question": "What is Newton's first law?",
      "options": {
        "A": "Force equals mass times acceleration",
        "B": "An object at rest stays at rest",
        "C": "For every action, there is an equal and opposite reaction",
        "D": "Energy cannot be created or destroyed"
      },
      "correctAnswer": "B",
      "explanation": "Newton's first law is the law of inertia..."
    }
  ]
}
```

### 10. Submit Test
**POST** `/tests/submit`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "subjectId": "60d5ec49f1b2c72b8c8e4f5b",
  "answers": {
    "q1": "B",
    "q2": "A",
    "q3": "C",
    "q4": "D",
    "q5": "A",
    "q6": "B",
    "q7": "C",
    "q8": "A",
    "q9": "B",
    "q10": "D"
  }
}
```

**Response (200):**
```json
{
  "testId": "60d5ec49f1b2c72b8c8e4f5d",
  "score": 85.0,
  "correctAnswers": 8,
  "incorrectAnswers": 2
}
```

### 11. Get Test Result
**GET** `/tests/:testId/result`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "testId": "60d5ec49f1b2c72b8c8e4f5d",
  "subjectId": "60d5ec49f1b2c72b8c8e4f5b",
  "score": 85.0,
  "totalQuestions": 10,
  "correctAnswers": 8,
  "incorrectAnswers": 2,
  "completedAt": 1634567890000,
  "questions": [...],
  "userAnswers": {...}
}
```

---

## Progress Endpoints

### 12. Get User Progress
**GET** `/progress`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "totalTests": 25,
  "averageScore": 82.5,
  "streak": {
    "current": 7,
    "longest": 14
  },
  "subjectProgress": [
    {
      "subjectId": "60d5ec49f1b2c72b8c8e4f5b",
      "subjectName": "Physics",
      "testsCompleted": 10,
      "averageScore": 85.0
    }
  ]
}
```

---

## Competitive Exam Endpoints

### 13. Get All Exams
**GET** `/competitive-exams`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
[
  {
    "id": "60d5ec49f1b2c72b8c8e4f5e",
    "name": "JEE Advanced",
    "description": "Joint Entrance Examination for IITs",
    "subjects": ["60d5ec49f1b2c72b8c8e4f5b", "60d5ec49f1b2c72b8c8e4f5c"],
    "dailyTestCount": 5,
    "active": true
  }
]
```

### 14. Enroll in Exam
**POST** `/competitive-exams/:examId/enroll`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "message": "Successfully enrolled in JEE Advanced",
  "enrollment": {
    "examId": "60d5ec49f1b2c72b8c8e4f5e",
    "userId": "60d5ec49f1b2c72b8c8e4f5a",
    "enrolledAt": 1634567890000
  }
}
```

### 15. Get Exam Progress
**GET** `/competitive-exams/:examId/progress`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "examId": "60d5ec49f1b2c72b8c8e4f5e",
  "examName": "JEE Advanced",
  "testsCompleted": 45,
  "averageScore": 78.5,
  "subjectWiseProgress": [
    {
      "subjectName": "Physics",
      "testsCompleted": 15,
      "averageScore": 82.0
    }
  ],
  "dailyStreak": 12
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid input data",
  "message": "Email is required"
}
```

### 401 Unauthorized
```json
{
  "error": "Authentication failed",
  "message": "Invalid token"
}
```

### 403 Forbidden
```json
{
  "error": "Access denied",
  "message": "Admin access required"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found",
  "message": "Subject not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Server error",
  "message": "An unexpected error occurred"
}
```

---

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Structure
```json
{
  "userId": "60d5ec49f1b2c72b8c8e4f5a",
  "email": "student@test.com",
  "role": "student",
  "iat": 1634567890,
  "exp": 1634654290
}
```

### Token Expiry
- Access tokens expire after **24 hours**
- Refresh tokens are not yet implemented
- User must login again after expiry

---

## Rate Limiting

- **General endpoints:** 100 requests per 15 minutes
- **AI endpoints:** 20 requests per minute
- **Auth endpoints:** 5 requests per minute

Exceeding rate limits returns:
```json
{
  "error": "Too many requests",
  "message": "Please try again later"
}
```

---

## Pagination

List endpoints support pagination:

**Query Parameters:**
```
?page=1&limit=20
```

**Response:**
```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 50,
    "totalPages": 3
  }
}
```

---

## Filtering & Sorting

**Subjects:**
```
GET /subjects?semester=Fall%202024&sort=name
```

**Materials:**
```
GET /materials/subject/:id?type=pdf&sort=-uploadedAt
```

**Tests:**
```
GET /tests?subjectId=xxx&sort=-completedAt
```

---

## Testing with Postman

### 1. Import Collection
Create a new collection in Postman with these requests.

### 2. Set Environment Variables
```
BASE_URL: http://localhost:5001/api
TOKEN: (set after login)
```

### 3. Test Authentication Flow
1. POST `/auth/register` → Get token
2. Save token to environment
3. GET `/subjects` with token
4. Test other endpoints

---

## Android Implementation Reference

### Retrofit Interface
```kotlin
@POST("auth/login")
suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

@GET("subjects")
suspend fun getSubjects(): Response<List<Subject>>

@POST("ai/chat")
suspend fun sendMessage(@Body request: ChatRequest): Response<AiChatResponse>
```

### Repository Usage
```kotlin
val result = authRepository.login(email, password)
when (result) {
    is Resource.Success -> // Handle success
    is Resource.Error -> // Handle error
    is Resource.Loading -> // Show loading
}
```

---

## Changelog

### v1.0.0 (December 2024)
- Initial API release
- Authentication endpoints
- Subject management
- Material upload
- AI chat integration
- Test generation and submission
- Progress tracking
- Competitive exam support

---

**API Version:** 1.0.0  
**Documentation Updated:** December 2024  
**Base URL:** `http://10.0.2.2:5001/api`
