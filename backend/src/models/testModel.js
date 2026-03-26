const mongoose = require('mongoose');

const questionSchema = new mongoose.Schema({
  question: {
    type: String,
    required: true
  },
  options: [{
    type: String,
    required: true
  }],
  correctAnswer: {
    type: Number,
    required: true,
    min: 0,
    max: 3
  },
  explanation: {
    type: String
  }
});

const testSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  subjectId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Subject',
    required: false
  },
  competitiveExamId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'CompetitiveExam',
    required: false
  },
  title: {
    type: String,
    required: true,
    trim: true
  },
  topic: {
    type: String,
    trim: true
  },
  questions: [questionSchema],
  userAnswers: [{
    type: Number,
    default: -1
  }],
  score: {
    type: Number,
    default: 0
  },
  totalQuestions: {
    type: Number,
    required: true
  },
  correctAnswers: {
    type: Number,
    default: 0
  },
  status: {
    type: String,
    enum: ['pending', 'completed', 'expired'],
    default: 'pending'
  },
  startedAt: {
    type: Date,
    default: Date.now
  },
  completedAt: {
    type: Date
  },
  duration: {
    type: Number,
    default: 0
  }
}, {
  timestamps: true
});

// Index for faster queries
testSchema.index({ userId: 1, status: 1 });
testSchema.index({ subjectId: 1 });

module.exports = mongoose.model('Test', testSchema);
