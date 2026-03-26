const Test = require('../models/testModel');
const Progress = require('../models/progressModel');
const Subject = require('../models/subjectModel');
const geminiService = require('../services/geminiService');

// @desc    Generate new test
// @route   POST /api/tests/generate
// @access  Private
exports.generateTest = async (req, res) => {
  try {
    const { subjectId, topic, questionCount = 10, difficulty = 'medium' } = req.body;

    console.log('Generate test request:', { subjectId, topic, questionCount, difficulty });

    if (!topic) {
      return res.status(400).json({ 
        error: 'Please provide a topic',
        received: { subjectId, topic }
      });
    }

    let subjectName = 'General Knowledge';
    let actualSubjectId = null;

    // If subjectId is provided and not 'general', look it up
    if (subjectId && subjectId.trim() !== '' && subjectId !== 'general') {
      const subject = await Subject.findById(subjectId);
      if (!subject) {
        return res.status(404).json({ error: 'Subject not found' });
      }
      subjectName = subject.name;
      actualSubjectId = subjectId;
    }

    // Generate questions using AI
    const questions = await geminiService.generatePracticeQuestions(
      subjectName,
      topic,
      questionCount,
      difficulty
    );

    // Create test
    const test = await Test.create({
      userId: req.user._id,
      subjectId: actualSubjectId,
      title: `${topic} - Practice Test`,
      topic,
      questions,
      totalQuestions: questions.length,
      userAnswers: new Array(questions.length).fill(-1)
    });

    // Return test without correct answers, format options as map
    const testForStudent = {
      ...test.toObject(),
      questions: test.questions.map((q, index) => ({
        id: `${test._id}_${index}`,
        question: q.question,
        options: {
          A: q.options[0] || 'Option A',
          B: q.options[1] || 'Option B',
          C: q.options[2] || 'Option C',
          D: q.options[3] || 'Option D'
        }
      }))
    };

    res.status(201).json({
      success: true,
      test: testForStudent
    });
  } catch (error) {
    console.error('Generate test error:', error);
    res.status(500).json({ error: 'Failed to generate test', details: error.message });
  }
};

// @desc    Get user's tests
// @route   GET /api/tests
// @access  Private
exports.getTests = async (req, res) => {
  try {
    const { status, subjectId } = req.query;

    let filter = { userId: req.user._id };
    if (status) filter.status = status;
    if (subjectId) filter.subjectId = subjectId;

    const tests = await Test.find(filter)
      .populate('subjectId', 'name code')
      .populate('competitiveExamId', 'name shortName')
      .sort({ createdAt: -1 });

    // Format tests with proper structure
    const formattedTests = tests.map(test => {
      const testObj = test.toObject();
      
      // Format questions if they exist
      if (testObj.questions && testObj.questions.length > 0) {
        testObj.questions = testObj.questions.map((q, index) => ({
          id: `${test._id}_${index}`,
          question: q.question,
          options: {
            A: q.options[0] || 'Option A',
            B: q.options[1] || 'Option B',
            C: q.options[2] || 'Option C',
            D: q.options[3] || 'Option D'
          },
          correctAnswer: test.status === 'completed' ? (['A', 'B', 'C', 'D'][q.correctAnswer] || 'A') : undefined,
          explanation: test.status === 'completed' ? q.explanation : undefined
        }));
      }
      
      return testObj;
    });

    res.json({
      success: true,
      count: formattedTests.length,
      tests: formattedTests
    });
  } catch (error) {
    console.error('Get tests error:', error);
    res.status(500).json({ error: 'Failed to fetch tests' });
  }
};

// @desc    Get single test
// @route   GET /api/tests/:id
// @access  Private
exports.getTest = async (req, res) => {
  try {
    const test = await Test.findById(req.params.id)
      .populate('subjectId', 'name code')
      .populate('competitiveExamId', 'name shortName');

    if (!test) {
      return res.status(404).json({ error: 'Test not found' });
    }

    // Only owner can view
    if (test.userId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Not authorized to view this test' });
    }

    // Format questions properly for Android app
    let testData = test.toObject();
    if (test.status !== 'completed') {
      // Hide correct answers and explanations for incomplete tests
      testData.questions = test.questions.map((q, index) => ({
        id: `${test._id}_${index}`,
        question: q.question,
        options: {
          A: q.options[0] || 'Option A',
          B: q.options[1] || 'Option B',
          C: q.options[2] || 'Option C',
          D: q.options[3] || 'Option D'
        }
      }));
    } else {
      // Show correct answers and explanations for completed tests
      testData.questions = test.questions.map((q, index) => ({
        id: `${test._id}_${index}`,
        question: q.question,
        options: {
          A: q.options[0] || 'Option A',
          B: q.options[1] || 'Option B',
          C: q.options[2] || 'Option C',
          D: q.options[3] || 'Option D'
        },
        correctAnswer: ['A', 'B', 'C', 'D'][q.correctAnswer] || 'A',
        explanation: q.explanation
      }));
    }

    res.json({
      success: true,
      test: testData
    });
  } catch (error) {
    console.error('Get test error:', error);
    res.status(500).json({ error: 'Failed to fetch test' });
  }
};

// @desc    Submit test answers
// @route   POST /api/tests/:id/submit
// @access  Private
exports.submitTest = async (req, res) => {
  try {
    const { userAnswers } = req.body;

    const test = await Test.findById(req.params.id)
      .populate('subjectId', 'name code');

    if (!test) {
      return res.status(404).json({ error: 'Test not found' });
    }

    if (test.userId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Not authorized' });
    }

    if (test.status === 'completed') {
      return res.status(400).json({ error: 'Test already completed' });
    }

    // Calculate score
    let correctAnswers = 0;
    userAnswers.forEach((answer, index) => {
      if (answer === test.questions[index].correctAnswer) {
        correctAnswers++;
      }
    });

    const score = (correctAnswers / test.totalQuestions) * 100;

    // Update test
    test.userAnswers = userAnswers;
    test.correctAnswers = correctAnswers;
    test.score = score;
    test.status = 'completed';
    test.completedAt = new Date();
    test.duration = Math.floor((test.completedAt - test.startedAt) / 1000); // in seconds

    await test.save();

    // Update progress
    await updateUserProgress(req.user._id, test);

    // Format test response with proper structure
    const formattedTest = {
      ...test.toObject(),
      questions: test.questions.map((q, index) => ({
        id: `${test._id}_${index}`,
        question: q.question,
        options: {
          A: q.options[0] || 'Option A',
          B: q.options[1] || 'Option B',
          C: q.options[2] || 'Option C',
          D: q.options[3] || 'Option D'
        },
        correctAnswer: ['A', 'B', 'C', 'D'][q.correctAnswer] || 'A',
        explanation: q.explanation
      }))
    };

    res.json({
      success: true,
      test: formattedTest,
      results: {
        score,
        correctAnswers,
        totalQuestions: test.totalQuestions,
        percentage: score.toFixed(2)
      }
    });
  } catch (error) {
    console.error('Submit test error:', error);
    res.status(500).json({ error: 'Failed to submit test' });
  }
};

// Helper function to update progress
async function updateUserProgress(userId, test) {
  try {
    let progress = await Progress.findOne({ userId });

    if (!progress) {
      progress = await Progress.create({ userId });
    }

    // Update overall stats
    progress.totalTests += 1;
    progress.totalQuestionsAttempted += test.totalQuestions;
    progress.totalCorrectAnswers += test.correctAnswers;
    progress.averageScore = (progress.totalCorrectAnswers / progress.totalQuestionsAttempted) * 100;

    // Update subject-wise progress
    if (test.subjectId) {
      const subjectProgress = progress.subjectWiseProgress.find(
        sp => sp.subjectId.toString() === test.subjectId.toString()
      );

      if (subjectProgress) {
        subjectProgress.testsAttempted += 1;
        subjectProgress.totalQuestions += test.totalQuestions;
        subjectProgress.correctAnswers += test.correctAnswers;
        subjectProgress.accuracy = (subjectProgress.correctAnswers / subjectProgress.totalQuestions) * 100;
        subjectProgress.lastAttempted = new Date();
      } else {
        progress.subjectWiseProgress.push({
          subjectId: test.subjectId,
          testsAttempted: 1,
          totalQuestions: test.totalQuestions,
          correctAnswers: test.correctAnswers,
          accuracy: (test.correctAnswers / test.totalQuestions) * 100,
          lastAttempted: new Date()
        });
      }
    }

    // Update competitive exam progress
    if (test.competitiveExamId) {
      const examProgress = progress.competitiveExamProgress.find(
        ep => ep.examId.toString() === test.competitiveExamId.toString()
      );

      if (examProgress) {
        examProgress.testsAttempted += 1;
        examProgress.totalQuestions += test.totalQuestions;
        examProgress.correctAnswers += test.correctAnswers;
        examProgress.accuracy = (examProgress.correctAnswers / examProgress.totalQuestions) * 100;
        examProgress.lastAttempted = new Date();
      } else {
        progress.competitiveExamProgress.push({
          examId: test.competitiveExamId,
          testsAttempted: 1,
          totalQuestions: test.totalQuestions,
          correctAnswers: test.correctAnswers,
          accuracy: (test.correctAnswers / test.totalQuestions) * 100,
          lastAttempted: new Date()
        });
      }
    }

    // Update streak
    const today = new Date();
    const lastActivity = progress.streak.lastActivity;
    
    if (lastActivity) {
      const daysDiff = Math.floor((today - lastActivity) / (1000 * 60 * 60 * 24));
      
      if (daysDiff === 0) {
        // Same day, no change
      } else if (daysDiff === 1) {
        // Consecutive day
        progress.streak.current += 1;
        if (progress.streak.current > progress.streak.longest) {
          progress.streak.longest = progress.streak.current;
        }
      } else {
        // Streak broken
        progress.streak.current = 1;
      }
    } else {
      progress.streak.current = 1;
      progress.streak.longest = 1;
    }
    
    progress.streak.lastActivity = today;

    await progress.save();
  } catch (error) {
    console.error('Update progress error:', error);
  }
}

// @desc    Delete test
// @route   DELETE /api/tests/:id
// @access  Private
exports.deleteTest = async (req, res) => {
  try {
    const test = await Test.findById(req.params.id);

    if (!test) {
      return res.status(404).json({ error: 'Test not found' });
    }

    if (test.userId.toString() !== req.user._id.toString() && req.user.role !== 'admin') {
      return res.status(403).json({ error: 'Not authorized' });
    }

    await test.deleteOne();

    res.json({
      success: true,
      message: 'Test deleted successfully'
    });
  } catch (error) {
    console.error('Delete test error:', error);
    res.status(500).json({ error: 'Failed to delete test' });
  }
};

// @desc    Get detailed test analysis with AI insights
// @route   GET /api/tests/:id/analysis
// @access  Private
exports.getTestAnalysis = async (req, res) => {
  try {
    const test = await Test.findById(req.params.id)
      .populate('subjectId', 'name code');

    if (!test) {
      return res.status(404).json({ error: 'Test not found' });
    }

    if (test.userId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Not authorized to view this analysis' });
    }

    if (test.status !== 'completed') {
      return res.status(400).json({ error: 'Test not completed yet' });
    }

    // Calculate difficulty breakdown
    const difficultyBreakdown = {
      easy: { correct: 0, total: 0 },
      medium: { correct: 0, total: 0 },
      hard: { correct: 0, total: 0 }
    };

    test.questions.forEach((question, index) => {
      const difficulty = question.difficulty || 'medium';
      const isCorrect = test.userAnswers[index] === question.correctAnswer;
      
      if (difficultyBreakdown[difficulty]) {
        difficultyBreakdown[difficulty].total += 1;
        if (isCorrect) {
          difficultyBreakdown[difficulty].correct += 1;
        }
      }
    });

    // Prepare questions review
    const questionsReview = test.questions.map((q, index) => ({
      question: q.question,
      options: q.options,
      correctAnswer: q.correctAnswer,
      userAnswer: test.userAnswers[index] !== -1 ? test.userAnswers[index] : null,
      difficulty: q.difficulty || 'medium'
    }));

    // Generate AI analysis
    let aiAnalysis = null;
    try {
      aiAnalysis = await generateAIAnalysis(test, difficultyBreakdown);
    } catch (error) {
      console.error('AI analysis error:', error);
      // Continue without AI analysis if it fails
    }

    const analysis = {
      _id: test._id,
      subjectName: test.subjectId ? test.subjectId.name : 'General Knowledge',
      topic: test.topic,
      scorePercentage: Math.round(test.score),
      correctAnswers: test.correctAnswers,
      totalQuestions: test.totalQuestions,
      totalTimeSeconds: test.duration || 0,
      difficultyBreakdown,
      aiAnalysis,
      questions: questionsReview
    };

    res.json({
      success: true,
      analysis
    });
  } catch (error) {
    console.error('Get test analysis error:', error);
    res.status(500).json({ error: 'Failed to fetch test analysis' });
  }
};

// Helper function to generate AI analysis
async function generateAIAnalysis(test, difficultyBreakdown) {
  try {
    const scorePercentage = Math.round(test.score);
    const subjectName = test.subjectId ? test.subjectId.name : 'General Knowledge';
    const topic = test.topic;

    // Calculate difficulty-wise accuracy
    const easyAccuracy = difficultyBreakdown.easy.total > 0 
      ? Math.round((difficultyBreakdown.easy.correct / difficultyBreakdown.easy.total) * 100) 
      : 0;
    const mediumAccuracy = difficultyBreakdown.medium.total > 0 
      ? Math.round((difficultyBreakdown.medium.correct / difficultyBreakdown.medium.total) * 100) 
      : 0;
    const hardAccuracy = difficultyBreakdown.hard.total > 0 
      ? Math.round((difficultyBreakdown.hard.correct / difficultyBreakdown.hard.total) * 100) 
      : 0;

    // Prepare analysis prompt
    const prompt = `Analyze this test performance and provide educational insights:

Subject: ${subjectName}
Topic: ${topic}
Overall Score: ${scorePercentage}%
Correct Answers: ${test.correctAnswers}/${test.totalQuestions}

Performance by Difficulty:
- Easy: ${easyAccuracy}% (${difficultyBreakdown.easy.correct}/${difficultyBreakdown.easy.total} correct)
- Medium: ${mediumAccuracy}% (${difficultyBreakdown.medium.correct}/${difficultyBreakdown.medium.total} correct)
- Hard: ${hardAccuracy}% (${difficultyBreakdown.hard.correct}/${difficultyBreakdown.hard.total} correct)

Provide a JSON response with the following structure (no markdown, just pure JSON):
{
  "overallAssessment": "Brief overall assessment (2-3 sentences)",
  "strengths": ["strength 1", "strength 2", "strength 3"],
  "weaknesses": ["weakness 1", "weakness 2"],
  "recommendations": ["specific recommendation 1", "specific recommendation 2", "specific recommendation 3"],
  "studyPlan": ["actionable step 1", "actionable step 2", "actionable step 3"]
}

Focus on being encouraging, specific, and actionable. Tailor recommendations to the ${topic} topic in ${subjectName}.`;

    const aiResponse = await geminiService.chat(prompt);
    
    // Try to parse JSON from response
    let analysisText = aiResponse.trim();
    
    // Remove markdown code blocks if present
    if (analysisText.startsWith('```json')) {
      analysisText = analysisText.replace(/```json\n?/g, '').replace(/```\n?/g, '');
    } else if (analysisText.startsWith('```')) {
      analysisText = analysisText.replace(/```\n?/g, '');
    }
    
    const analysis = JSON.parse(analysisText);
    
    return {
      overallAssessment: analysis.overallAssessment || "Good effort on this test!",
      strengths: analysis.strengths || ["You attempted all questions"],
      weaknesses: analysis.weaknesses || ["Keep practicing to improve"],
      recommendations: analysis.recommendations || ["Review the topics you found challenging"],
      studyPlan: analysis.studyPlan || ["Practice regularly", "Focus on weak areas", "Review concepts thoroughly"]
    };
  } catch (error) {
    console.error('Generate AI analysis error:', error);
    
    // Return fallback analysis based on score
    const scorePercentage = Math.round(test.score);
    let overallAssessment = "Good effort! ";
    let strengths = [];
    let weaknesses = [];
    let recommendations = [];
    
    if (scorePercentage >= 80) {
      overallAssessment += "Excellent performance! You have a strong understanding of the topic.";
      strengths.push("Strong overall performance");
      strengths.push("Good grasp of fundamental concepts");
      recommendations.push("Challenge yourself with more advanced topics");
    } else if (scorePercentage >= 60) {
      overallAssessment += "Solid performance with room for improvement.";
      strengths.push("Good understanding of basic concepts");
      weaknesses.push("Some areas need more practice");
      recommendations.push("Review incorrect answers carefully");
      recommendations.push("Practice similar questions");
    } else {
      overallAssessment += "Keep practicing! Focus on understanding core concepts.";
      weaknesses.push("Need to strengthen fundamental understanding");
      recommendations.push("Review the topic thoroughly");
      recommendations.push("Start with easier questions and build up");
      recommendations.push("Seek help from study materials or mentors");
    }
    
    return {
      overallAssessment,
      strengths: strengths.length > 0 ? strengths : ["You completed the test"],
      weaknesses: weaknesses.length > 0 ? weaknesses : ["Focus on consistent practice"],
      recommendations: recommendations.length > 0 ? recommendations : ["Practice regularly", "Review concepts"],
      studyPlan: ["Set aside 30 minutes daily for practice", "Focus on weak areas", "Track your progress"]
    };
  }
}
