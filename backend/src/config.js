require('dotenv').config();

module.exports = {
  port: process.env.PORT || 5001,
  nodeEnv: process.env.NODE_ENV || 'development',
  mongoUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/campus_helper',
  jwtSecret: process.env.JWT_SECRET || 'your_jwt_secret',
  jwtExpire: process.env.JWT_EXPIRE || '7d',
  geminiApiKey: process.env.GEMINI_API_KEY,
  geminiApiUrl: process.env.GEMINI_API_URL || 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent',
  corsOrigin: process.env.CORS_ORIGIN ? process.env.CORS_ORIGIN.split(',') : ['http://localhost:3000'],
  firebase: {
    apiKey: process.env.FIREBASE_API_KEY,
    projectId: process.env.FIREBASE_PROJECT_ID,
    storageBucket: process.env.FIREBASE_STORAGE_BUCKET
  }
};
