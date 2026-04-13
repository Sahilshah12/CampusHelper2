const geminiService = require('../services/geminiService');
const Subject = require('../models/subjectModel');

// @desc    AI Chat - Topic explanation
// @route   POST /api/ai/chat
// @access  Private
exports.chat = async (req, res) => {
  try {
    const { subjectId, topic, question } = req.body;

    if (!subjectId || !topic) {
      return res.status(400).json({ error: 'Please provide subject and topic' });
    }

    let subjectName = 'General';
    
    // Handle "general" or actual subject ID
    if (subjectId !== 'general') {
      const subject = await Subject.findById(subjectId);
      if (!subject) {
        return res.status(404).json({ error: 'Subject not found' });
      }
      subjectName = subject.name;
    }

    let response;

    if (question) {
      // Specific question about the topic
      response = await geminiService.explainConcept(subjectName, topic, question);
    } else {
      // General summary
      response = await geminiService.summarizeTopic(subjectName, topic);
    }

    res.json({
      success: true,
      response,
      context: {
        subject: subjectName,
        topic,
        question
      }
    });
  } catch (error) {
    console.error('AI chat error:', error);

    const { subjectId, topic, question } = req.body;
    let subjectName = 'General';

    if (subjectId && subjectId !== 'general') {
      const subject = await Subject.findById(subjectId).catch(() => null);
      subjectName = subject?.name || 'General';
    }

    const fallbackResponse = geminiService.generateFallbackChatResponse(subjectName, topic || 'the topic', question);

    const isTransientAiIssue =
      error.message?.includes('temporarily overloaded') ||
      error.message?.includes('quota exceeded') ||
      error.message?.includes('timeout') ||
      error.message?.includes('Failed to generate content from AI');

    if (isTransientAiIssue) {
      return res.status(200).json({
        success: true,
        fallback: true,
        response: fallbackResponse,
        context: {
          subject: subjectName,
          topic,
          question
        },
        note: 'AI service is temporarily unavailable. Showing a fallback explanation.'
      });
    }

    const errorMessage = error.message || 'Failed to get AI response';
    res.status(500).json({
      error: errorMessage,
      hint: error.message?.includes('API key')
        ? 'Please configure GEMINI_API_KEY environment variable'
        : undefined
    });
  }
};

// @desc    Generate practice questions
// @route   POST /api/ai/generate-questions
// @access  Private
exports.generateQuestions = async (req, res) => {
  try {
    const { subjectId, topic, count = 5, difficulty = 'medium' } = req.body;

    if (!subjectId || !topic) {
      return res.status(400).json({ error: 'Please provide subject and topic' });
    }

    let subjectName = 'General';
    
    // Handle "general" or actual subject ID
    if (subjectId !== 'general') {
      const subject = await Subject.findById(subjectId);
      if (!subject) {
        return res.status(404).json({ error: 'Subject not found' });
      }
      subjectName = subject.name;
    }

    const questions = await geminiService.generatePracticeQuestions(
      subjectName,
      topic,
      count,
      difficulty
    );

    res.json({
      success: true,
      questions: questions.map(q => ({
        question: q.question,
        options: q.options
        // Hide correct answer
      })),
      metadata: {
        subject: subjectName,
        topic,
        count: questions.length,
        difficulty
      }
    });
  } catch (error) {
    console.error('Generate questions error:', error);
    res.status(500).json({ error: 'Failed to generate questions' });
  }
};

// @desc    Get AI study tips
// @route   POST /api/ai/study-tips
// @access  Private
exports.getStudyTips = async (req, res) => {
  try {
    const { subjectId, topic } = req.body;

    if (!subjectId || !topic) {
      return res.status(400).json({ error: 'Please provide subject and topic' });
    }

    let subjectName = 'General';
    
    // Handle "general" or actual subject ID
    if (subjectId !== 'general') {
      const subject = await Subject.findById(subjectId);
      if (!subject) {
        return res.status(404).json({ error: 'Subject not found' });
      }
      subjectName = subject.name;
    }

    const prompt = `Provide effective study tips and strategies for learning the following topic:

Subject: ${subjectName}
Topic: ${topic}

Include:
1. Key focus areas
2. Learning approach
3. Common mistakes to avoid
4. Practice recommendations
5. Time management tips

Make it practical and actionable for college students.`;

    const response = await geminiService.generateContent(prompt);

    res.json({
      success: true,
      tips: response,
      subject: subjectName,
      topic
    });
  } catch (error) {
    console.error('Study tips error:', error);
    res.status(500).json({ error: 'Failed to get study tips' });
  }
};
