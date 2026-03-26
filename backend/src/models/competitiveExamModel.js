const mongoose = require('mongoose');

const competitiveExamSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Exam name is required'],
    unique: true,
    trim: true
  },
  shortName: {
    type: String,
    required: true,
    uppercase: true,
    trim: true
  },
  description: {
    type: String,
    trim: true
  },
  category: {
    type: String,
    enum: ['Engineering', 'Medical', 'Civil Services', 'Management', 'Banking', 'Defense', 'Other'],
    default: 'Other'
  },
  icon: {
    type: String,
    default: null
  },
  subjects: [{
    type: String,
    trim: true
  }],
  questionPattern: {
    totalQuestions: {
      type: Number,
      default: 20
    },
    duration: {
      type: Number,
      default: 30
    },
    difficulty: {
      type: String,
      enum: ['easy', 'medium', 'hard', 'mixed'],
      default: 'mixed'
    }
  },
  isActive: {
    type: Boolean,
    default: true
  },
  enrolledStudents: {
    type: Number,
    default: 0
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('CompetitiveExam', competitiveExamSchema);
