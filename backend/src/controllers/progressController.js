const Progress = require('../models/progressModel');

// @desc    Get user progress
// @route   GET /api/progress
// @access  Private
exports.getProgress = async (req, res) => {
  try {
    const progress = await Progress.findOne({ userId: req.user._id })
      .populate('subjectWiseProgress.subjectId', 'name code')
      .populate('competitiveExamProgress.examId', 'name shortName');

    if (!progress) {
      // Create new progress if doesn't exist
      const newProgress = await Progress.create({ userId: req.user._id });
      return res.json({
        success: true,
        progress: newProgress
      });
    }

    res.json({
      success: true,
      progress
    });
  } catch (error) {
    console.error('Get progress error:', error);
    res.status(500).json({ error: 'Failed to fetch progress' });
  }
};

// @desc    Get subject-wise progress
// @route   GET /api/progress/subject/:subjectId
// @access  Private
exports.getSubjectProgress = async (req, res) => {
  try {
    const progress = await Progress.findOne({ userId: req.user._id })
      .populate('subjectWiseProgress.subjectId', 'name code');

    if (!progress) {
      return res.status(404).json({ error: 'Progress not found' });
    }

    const subjectProgress = progress.subjectWiseProgress.find(
      sp => sp.subjectId._id.toString() === req.params.subjectId
    );

    if (!subjectProgress) {
      return res.json({
        success: true,
        progress: {
          subjectId: req.params.subjectId,
          testsAttempted: 0,
          totalQuestions: 0,
          correctAnswers: 0,
          accuracy: 0
        }
      });
    }

    res.json({
      success: true,
      progress: subjectProgress
    });
  } catch (error) {
    console.error('Get subject progress error:', error);
    res.status(500).json({ error: 'Failed to fetch subject progress' });
  }
};

// @desc    Get competitive exam progress
// @route   GET /api/progress/competitive-exam/:examId
// @access  Private
exports.getCompetitiveExamProgress = async (req, res) => {
  try {
    const progress = await Progress.findOne({ userId: req.user._id })
      .populate('competitiveExamProgress.examId', 'name shortName');

    if (!progress) {
      return res.status(404).json({ error: 'Progress not found' });
    }

    const examProgress = progress.competitiveExamProgress.find(
      ep => ep.examId._id.toString() === req.params.examId
    );

    if (!examProgress) {
      return res.json({
        success: true,
        progress: {
          examId: req.params.examId,
          testsAttempted: 0,
          totalQuestions: 0,
          correctAnswers: 0,
          accuracy: 0
        }
      });
    }

    res.json({
      success: true,
      progress: examProgress
    });
  } catch (error) {
    console.error('Get competitive exam progress error:', error);
    res.status(500).json({ error: 'Failed to fetch exam progress' });
  }
};

// @desc    Get analytics (Admin)
// @route   GET /api/progress/analytics
// @access  Private (Admin)
exports.getAnalytics = async (req, res) => {
  try {
    const allProgress = await Progress.find()
      .populate('userId', 'name email')
      .populate('subjectWiseProgress.subjectId', 'name code')
      .populate('competitiveExamProgress.examId', 'name shortName');

    const analytics = {
      totalStudents: allProgress.length,
      totalTests: allProgress.reduce((sum, p) => sum + p.totalTests, 0),
      totalQuestions: allProgress.reduce((sum, p) => sum + p.totalQuestionsAttempted, 0),
      averageScore: allProgress.reduce((sum, p) => sum + p.averageScore, 0) / (allProgress.length || 1),
      topPerformers: allProgress
        .sort((a, b) => b.averageScore - a.averageScore)
        .slice(0, 10)
        .map(p => ({
          user: p.userId,
          averageScore: p.averageScore,
          totalTests: p.totalTests,
          streak: p.streak.current
        }))
    };

    res.json({
      success: true,
      analytics,
      allProgress
    });
  } catch (error) {
    console.error('Get analytics error:', error);
    res.status(500).json({ error: 'Failed to fetch analytics' });
  }
};
