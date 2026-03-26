const CompetitiveExam = require('../models/competitiveExamModel');
const User = require('../models/userModel');
const Test = require('../models/testModel');
const geminiService = require('../services/geminiService');

// @desc    Get all competitive exams
// @route   GET /api/competitive-exams
// @access  Private
exports.getAllExams = async (req, res) => {
  try {
    const exams = await CompetitiveExam.find({ isActive: true })
      .sort({ name: 1 });

    res.json({
      success: true,
      count: exams.length,
      exams
    });
  } catch (error) {
    console.error('Get exams error:', error);
    res.status(500).json({ error: 'Failed to fetch exams' });
  }
};

// @desc    Get single exam
// @route   GET /api/competitive-exams/:id
// @access  Private
exports.getExam = async (req, res) => {
  try {
    const exam = await CompetitiveExam.findById(req.params.id);

    if (!exam) {
      return res.status(404).json({ error: 'Exam not found' });
    }

    res.json({
      success: true,
      exam
    });
  } catch (error) {
    console.error('Get exam error:', error);
    res.status(500).json({ error: 'Failed to fetch exam' });
  }
};

// @desc    Create competitive exam
// @route   POST /api/competitive-exams
// @access  Private (Admin)
exports.createExam = async (req, res) => {
  try {
    const { name, shortName, description, category, subjects, questionPattern } = req.body;

    if (!name || !shortName) {
      return res.status(400).json({ error: 'Please provide name and short name' });
    }

    const exam = await CompetitiveExam.create({
      name,
      shortName: shortName.toUpperCase(),
      description,
      category,
      subjects,
      questionPattern
    });

    res.status(201).json({
      success: true,
      exam
    });
  } catch (error) {
    console.error('Create exam error:', error);
    res.status(500).json({ error: 'Failed to create exam' });
  }
};

// @desc    Update competitive exam
// @route   PUT /api/competitive-exams/:id
// @access  Private (Admin)
exports.updateExam = async (req, res) => {
  try {
    const exam = await CompetitiveExam.findById(req.params.id);

    if (!exam) {
      return res.status(404).json({ error: 'Exam not found' });
    }

    const { name, shortName, description, category, subjects, questionPattern, isActive } = req.body;

    if (name) exam.name = name;
    if (shortName) exam.shortName = shortName.toUpperCase();
    if (description !== undefined) exam.description = description;
    if (category) exam.category = category;
    if (subjects) exam.subjects = subjects;
    if (questionPattern) exam.questionPattern = questionPattern;
    if (isActive !== undefined) exam.isActive = isActive;

    await exam.save();

    res.json({
      success: true,
      exam
    });
  } catch (error) {
    console.error('Update exam error:', error);
    res.status(500).json({ error: 'Failed to update exam' });
  }
};

// @desc    Delete competitive exam
// @route   DELETE /api/competitive-exams/:id
// @access  Private (Admin)
exports.deleteExam = async (req, res) => {
  try {
    const exam = await CompetitiveExam.findById(req.params.id);

    if (!exam) {
      return res.status(404).json({ error: 'Exam not found' });
    }

    exam.isActive = false;
    await exam.save();

    res.json({
      success: true,
      message: 'Exam deleted successfully'
    });
  } catch (error) {
    console.error('Delete exam error:', error);
    res.status(500).json({ error: 'Failed to delete exam' });
  }
};

// @desc    Enroll in competitive exam
// @route   POST /api/competitive-exams/:id/enroll
// @access  Private (Student)
exports.enrollExam = async (req, res) => {
  try {
    const { dailyTestsCount = 1 } = req.body;

    if (dailyTestsCount < 1 || dailyTestsCount > 3) {
      return res.status(400).json({ error: 'Daily tests count must be between 1 and 3' });
    }

    const exam = await CompetitiveExam.findById(req.params.id);

    if (!exam) {
      return res.status(404).json({ error: 'Exam not found' });
    }

    const user = await User.findById(req.user._id);

    // Check if already enrolled
    const alreadyEnrolled = user.enrolledCompetitiveExams.find(
      e => e.examId.toString() === exam._id.toString()
    );

    if (alreadyEnrolled) {
      return res.status(400).json({ error: 'Already enrolled in this exam' });
    }

    // Enroll user
    user.enrolledCompetitiveExams.push({
      examId: exam._id,
      dailyTestsCount,
      enrolledAt: new Date()
    });

    exam.enrolledStudents += 1;

    await user.save();
    await exam.save();

    res.json({
      success: true,
      message: 'Successfully enrolled in exam',
      enrollment: user.enrolledCompetitiveExams[user.enrolledCompetitiveExams.length - 1]
    });
  } catch (error) {
    console.error('Enroll exam error:', error);
    res.status(500).json({ error: 'Failed to enroll in exam' });
  }
};

// @desc    Generate daily competitive exam test
// @route   POST /api/competitive-exams/:id/generate-test
// @access  Private (Student)
exports.generateCompetitiveTest = async (req, res) => {
  try {
    const exam = await CompetitiveExam.findById(req.params.id);

    if (!exam) {
      return res.status(404).json({ error: 'Exam not found' });
    }

    const user = await User.findById(req.user._id);
    const enrollment = user.enrolledCompetitiveExams.find(
      e => e.examId.toString() === exam._id.toString()
    );

    if (!enrollment) {
      return res.status(400).json({ error: 'Not enrolled in this exam' });
    }

    // Check today's test count
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const todaysTests = await Test.countDocuments({
      userId: req.user._id,
      competitiveExamId: exam._id,
      createdAt: { $gte: today }
    });

    if (todaysTests >= enrollment.dailyTestsCount) {
      return res.status(400).json({ 
        error: `Daily limit reached. You can attempt ${enrollment.dailyTestsCount} test(s) per day.`
      });
    }

    // Generate test using AI
    const questions = await geminiService.generateCompetitiveExamQuestions(
      exam.name,
      exam.subjects,
      exam.questionPattern.totalQuestions,
      exam.questionPattern.difficulty
    );

    const test = await Test.create({
      userId: req.user._id,
      competitiveExamId: exam._id,
      title: `${exam.shortName} - Daily Practice ${todaysTests + 1}`,
      questions,
      totalQuestions: questions.length,
      userAnswers: new Array(questions.length).fill(-1)
    });

    // Return without correct answers
    const testForStudent = {
      ...test.toObject(),
      questions: test.questions.map(q => ({
        question: q.question,
        options: q.options,
        subject: q.subject
      }))
    };

    res.status(201).json({
      success: true,
      test: testForStudent
    });
  } catch (error) {
    console.error('Generate competitive test error:', error);
    res.status(500).json({ error: 'Failed to generate test' });
  }
};
