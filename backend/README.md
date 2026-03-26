# Campus Helper Backend 🎓

AI-Powered Student Learning Platform - Backend API

## 📋 Overview

Campus Helper is a comprehensive Node.js backend API that powers an AI-driven student learning platform. It provides features for subject management, study materials, AI-generated tests, progress tracking, and competitive exam preparation.

## ✨ Features

- **🔐 Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (Student/Admin)
  - Secure password hashing

- **📚 Subject Management**
  - CRUD operations for subjects
  - Subject code and description support
  - Admin-only subject creation/editing

- **📄 Study Materials**
  - PDF uploads (local storage)
  - YouTube video links
  - External resource links
  - Notes and text content
  - Subject-wise material organization

- **🤖 AI-Powered Features**
  - AI test generation using Google Gemini API
  - Intelligent chat assistant
  - Automated question generation from topics

- **📊 Progress Tracking**
  - Subject-wise performance analytics
  - Test history and scores
  - Accuracy calculations
  - Performance trends

- **🏆 Competitive Exam Prep**
  - Multiple competitive exam categories
  - Exam-specific study materials
  - Practice tests and assessments

## 🛠️ Tech Stack

- **Runtime:** Node.js
- **Framework:** Express.js
- **Database:** MongoDB with Mongoose ODM
- **Authentication:** JWT (JSON Web Tokens)
- **File Upload:** Multer
- **AI Integration:** Google Gemini API
- **Environment:** dotenv

## 📦 Installation

### Prerequisites

- Node.js (v14 or higher)
- MongoDB (local or Atlas)
- Google Gemini API Key

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Sahilshah12/Campushelper_BE.git
   cd Campushelper_BE
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   
   Create a `.env` file in the root directory:
   ```env
   PORT=5002
   MONGODB_URI=your_mongodb_connection_string
   JWT_SECRET=your_jwt_secret_key
   GEMINI_API_KEY=your_google_gemini_api_key
   NODE_ENV=development
   ```

4. **Create uploads directory**
   ```bash
   mkdir uploads
   ```

5. **Start the server**
   ```bash
   npm start
   ```

   For development with auto-reload:
   ```bash
   npm run dev
   ```

## 🚀 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Subjects
- `GET /api/subjects` - Get all subjects
- `GET /api/subjects/:id` - Get subject by ID
- `POST /api/subjects` - Create subject (Admin)
- `PUT /api/subjects/:id` - Update subject (Admin)
- `DELETE /api/subjects/:id` - Delete subject (Admin)

### Materials
- `GET /api/materials` - Get all materials
- `GET /api/materials/subject/:subjectId` - Get materials by subject
- `POST /api/materials` - Upload material (Admin)
- `PUT /api/materials/:id` - Update material (Admin)
- `DELETE /api/materials/:id` - Delete material (Admin)

### Tests
- `POST /api/tests/generate` - Generate AI test
- `POST /api/tests/submit` - Submit test
- `GET /api/tests/history` - Get test history
- `GET /api/tests/:id` - Get test by ID

### Progress
- `GET /api/progress` - Get user progress
- `GET /api/progress/subject/:subjectId` - Get subject-wise progress

### AI Chat
- `POST /api/ai/chat` - Chat with AI assistant

### Competitive Exams
- `GET /api/competitive-exams` - Get all exams
- `GET /api/competitive-exams/:id` - Get exam by ID
- `POST /api/competitive-exams` - Create exam (Admin)

## 📁 Project Structure

```
backend/
├── src/
│   ├── config.js           # Configuration settings
│   ├── server.js           # Server entry point
│   ├── app.js              # Express app setup
│   ├── controllers/        # Request handlers
│   │   ├── authController.js
│   │   ├── subjectController.js
│   │   ├── materialController.js
│   │   ├── testController.js
│   │   ├── progressController.js
│   │   ├── aiController.js
│   │   └── competitiveExamController.js
│   ├── models/             # Mongoose schemas
│   │   ├── userModel.js
│   │   ├── subjectModel.js
│   │   ├── materialModel.js
│   │   ├── testModel.js
│   │   ├── progressModel.js
│   │   └── competitiveExamModel.js
│   ├── routes/             # API routes
│   │   ├── authRoutes.js
│   │   ├── subjectRoutes.js
│   │   ├── materialRoutes.js
│   │   ├── testRoutes.js
│   │   ├── progressRoutes.js
│   │   ├── aiRoutes.js
│   │   └── competitiveExamRoutes.js
│   ├── middlewares/        # Custom middlewares
│   │   ├── authMiddleware.js
│   │   ├── roleMiddleware.js
│   │   └── uploadMiddleware.js
│   ├── services/           # Business logic
│   │   └── geminiService.js
│   └── seeders/            # Database seeders
│       └── seedData.js
├── uploads/                # Uploaded files storage
├── .env                    # Environment variables
├── .gitignore             # Git ignore rules
├── package.json           # Dependencies
└── README.md              # This file
```

## 🔑 Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `PORT` | Server port number | Yes |
| `MONGODB_URI` | MongoDB connection string | Yes |
| `JWT_SECRET` | Secret key for JWT tokens | Yes |
| `GEMINI_API_KEY` | Google Gemini API key | Yes |
| `NODE_ENV` | Environment (development/production) | No |

## 📝 API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message",
  "error": "Detailed error information"
}
```

## 🔒 Security Features

- Password hashing with bcrypt
- JWT token authentication
- Protected routes with middleware
- Role-based authorization
- File upload validation
- Input sanitization

## 📊 Database Models

### User
- name, email, password
- role (student/admin)
- timestamps

### Subject
- name, code, description
- creator reference
- timestamps

### Material
- subjectId, uploadedBy
- type (pdf/youtube/link/notes)
- url, fileUrl, content
- timestamps

### Test
- userId, subjectId
- questions array
- answers, score, accuracy
- timestamps

### Progress
- userId
- testsCompleted, totalQuestions
- correctAnswers, incorrectAnswers
- subjectWiseProgress
- timestamps

## 🚀 Deployment (Render)

### Steps to Deploy on Render

1. **Push code to GitHub** (follow the commands you provided)

2. **Create new Web Service on Render**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New +" → "Web Service"
   - Connect your GitHub repository

3. **Configure Build Settings**
   - **Build Command:** `npm install`
   - **Start Command:** `npm start`
   - **Environment:** Node

4. **Add Environment Variables**
   - Add all variables from `.env` file in Render dashboard
   - Set `NODE_ENV=production`

5. **Deploy**
   - Click "Create Web Service"
   - Render will automatically deploy your app

### MongoDB Atlas Setup (Required for Render)

1. Create free cluster at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Get connection string
3. Add to Render environment variables as `MONGODB_URI`

## 📱 Frontend Integration

This backend is designed to work with the Campus Helper Android app. The app uses:
- Base URL: `http://10.0.2.2:5002/api/` (for emulator)
- Base URL: `https://your-render-url.onrender.com/api/` (for production)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.

## 👨‍💻 Developer

**Sahil Shah**
- GitHub: [@Sahilshah12](https://github.com/Sahilshah12)

## 🙏 Acknowledgments

- Google Gemini AI for intelligent features
- MongoDB for database
- Express.js community
- All contributors and users

---

Made with ❤️ for students worldwide 🌍
