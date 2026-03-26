const mongoose = require('mongoose');

const subjectSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Subject name is required'],
    trim: true
  },
  code: {
    type: String,
    required: [true, 'Subject code is required'],
    unique: true,
    uppercase: true,
    trim: true
  },
  description: {
    type: String,
    trim: true
  },
  category: {
    type: String,
    enum: ['Engineering', 'Medical', 'Arts', 'Commerce', 'Science', 'Management', 'Other'],
    default: 'Other'
  },
  semester: {
    type: Number,
    min: 1,
    max: 10
  },
  credits: {
    type: Number,
    default: 3
  },
  icon: {
    type: String,
    default: null
  },
  isActive: {
    type: Boolean,
    default: true
  },
  createdBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  }
}, {
  timestamps: true
});

// Index for faster queries
subjectSchema.index({ name: 1, code: 1 });

module.exports = mongoose.model('Subject', subjectSchema);
