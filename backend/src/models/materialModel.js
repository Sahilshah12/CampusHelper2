const mongoose = require('mongoose');

const materialSchema = new mongoose.Schema({
  subjectId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Subject',
    required: true
  },
  title: {
    type: String,
    required: [true, 'Title is required'],
    trim: true
  },
  description: {
    type: String,
    trim: true
  },
  type: {
    type: String,
    enum: ['pdf', 'youtube', 'notes', 'link'],
    required: true
  },
  url: {
    type: String,
    required: function() {
      return this.type === 'youtube' || this.type === 'link';
    }
  },
  fileUrl: {
    type: String,
    required: function() {
      return this.type === 'pdf';
    }
  },
  content: {
    type: String,
    required: function() {
      return this.type === 'notes';
    }
  },
  tags: [{
    type: String,
    trim: true
  }],
  topic: {
    type: String,
    trim: true
  },
  difficulty: {
    type: String,
    enum: ['easy', 'medium', 'hard'],
    default: 'medium'
  },
  viewCount: {
    type: Number,
    default: 0
  },
  uploadedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  isActive: {
    type: Boolean,
    default: true
  }
}, {
  timestamps: true
});

// Index for faster queries
materialSchema.index({ subjectId: 1, type: 1 });

module.exports = mongoose.model('Material', materialSchema);
