const mongoose = require('mongoose');

const progressSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    unique: true,
    index: true // Explicit index declaration
  },
  totalTests: {
    type: Number,
    default: 0
  },
  totalQuestionsAttempted: {
    type: Number,
    default: 0
  },
  totalCorrectAnswers: {
    type: Number,
    default: 0
  },
  averageScore: {
    type: Number,
    default: 0
  },
  subjectWiseProgress: [{
    subjectId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Subject'
    },
    testsAttempted: {
      type: Number,
      default: 0
    },
    totalQuestions: {
      type: Number,
      default: 0
    },
    correctAnswers: {
      type: Number,
      default: 0
    },
    accuracy: {
      type: Number,
      default: 0
    },
    lastAttempted: Date
  }],
  competitiveExamProgress: [{
    examId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'CompetitiveExam'
    },
    testsAttempted: {
      type: Number,
      default: 0
    },
    totalQuestions: {
      type: Number,
      default: 0
    },
    correctAnswers: {
      type: Number,
      default: 0
    },
    accuracy: {
      type: Number,
      default: 0
    },
    lastAttempted: Date
  }],
  streak: {
    current: {
      type: Number,
      default: 0
    },
    longest: {
      type: Number,
      default: 0
    },
    lastActivity: Date
  },
  monthlyStats: [{
    month: String,
    year: Number,
    testsCompleted: Number,
    averageScore: Number
  }]
}, {
  timestamps: true
});

// Note: userId already has unique index from schema definition
// No need for explicit index creation

module.exports = mongoose.model('Progress', progressSchema);
