const axios = require('axios');
const config = require('../config');

class GeminiService {
  constructor() {
    this.apiKey = config.geminiApiKey;
    this.apiUrl = config.geminiApiUrl;
  }

  async generateContent(prompt, retries = 2) {
    // Check if API key is configured
    if (!this.apiKey) {
      throw new Error('Gemini API key is not configured. Please set GEMINI_API_KEY environment variable.');
    }

    let lastError;

    for (let attempt = 0; attempt <= retries; attempt++) {
      try {
        if (attempt > 0) {
          // Wait before retry (exponential backoff: 1s, 2s, 4s)
          const delay = Math.pow(2, attempt) * 1000;
          console.log(`Retry attempt ${attempt} after ${delay}ms delay...`);
          await new Promise(resolve => setTimeout(resolve, delay));
        }

        const response = await axios.post(
          `${this.apiUrl}?key=${this.apiKey}`,
          {
            contents: [{
              parts: [{
                text: prompt
              }]
            }]
          },
          {
            headers: {
              'Content-Type': 'application/json'
            },
            timeout: 30000 // 30 second timeout
          }
        );

        // Validate response structure
        if (!response.data?.candidates?.[0]?.content?.parts?.[0]?.text) {
          throw new Error('Invalid response from Gemini API');
        }

        return response.data.candidates[0].content.parts[0].text;
      } catch (error) {
        lastError = error;
        console.error(`Gemini API Error (attempt ${attempt + 1}):`, error.response?.data || error.message);
        
        // Check if we should retry
        const status = error.response?.status || error.response?.data?.error?.code;
        
        // Don't retry on client errors (400, 401, 403)
        if (status === 400 || status === 401 || status === 403) {
          break;
        }
        
        // Retry on server errors (500, 503) and timeouts
        if (attempt < retries && (status === 500 || status === 503 || error.code === 'ECONNABORTED')) {
          continue;
        }
        
        // Don't retry on other errors
        break;
      }
    }

    // All retries failed, throw appropriate error
    const status = lastError.response?.status || lastError.response?.data?.error?.code;
    
    if (status === 400) {
      throw new Error('Invalid API key or request format');
    } else if (status === 429) {
      throw new Error('API quota exceeded. Please try again later.');
    } else if (status === 503) {
      throw new Error('AI service is temporarily overloaded. Please try again in a few moments.');
    } else if (lastError.code === 'ECONNABORTED') {
      throw new Error('Request timeout. Please try again.');
    } else if (lastError.message.includes('API key')) {
      throw lastError; // Preserve API key error message
    } else {
      throw new Error('Failed to generate content from AI. Please try again.');
    }
  }

  async summarizeTopic(subject, topic) {
    const prompt = `You are an educational AI assistant. Provide a clear, concise, and comprehensive summary of the following topic for college students:

Subject: ${subject}
Topic: ${topic}

Please explain the key concepts, important points, and practical applications. Keep the language simple and structured.`;

    return await this.generateContent(prompt);
  }

  async explainConcept(subject, topic, specificQuestion) {
    const prompt = `You are an educational AI assistant helping a college student understand a concept.

Subject: ${subject}
Topic: ${topic}
Student's Question: ${specificQuestion}

Provide a detailed explanation that:
1. Breaks down the concept into simple terms
2. Uses examples and analogies
3. Explains practical applications
4. Highlights common misconceptions

Keep it educational and easy to understand.`;

    return await this.generateContent(prompt);
  }

  async generatePracticeQuestions(subject, topic, count = 5, difficulty = 'medium') {
    const prompt = `Generate ${count} multiple-choice questions for a practice test.

Subject: ${subject}
Topic: ${topic}
Difficulty: ${difficulty}

Requirements:
- Each question must have exactly 4 options (A, B, C, D)
- Indicate the correct answer index (0-3)
- Provide a brief explanation for the correct answer
- Make questions relevant to college-level students

Return ONLY a valid JSON array with this exact structure:
[
  {
    "question": "Question text here?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctAnswer": 0,
    "explanation": "Explanation of why this is correct"
  }
]

IMPORTANT: Return ONLY the JSON array, no additional text or markdown formatting.`;

    try {
      const response = await this.generateContent(prompt);
      
      // Clean the response to extract JSON
      let jsonStr = response.trim();
      
      // Remove markdown code blocks if present
      jsonStr = jsonStr.replace(/```json\n?/g, '').replace(/```\n?/g, '');
      
      // Parse JSON
      const questions = JSON.parse(jsonStr);
      
      // Validate structure
      if (!Array.isArray(questions)) {
        throw new Error('Invalid response format');
      }

      return questions.slice(0, count).map(q => ({
        question: q.question,
        options: q.options.slice(0, 4),
        correctAnswer: Math.max(0, Math.min(3, q.correctAnswer)),
        explanation: q.explanation || 'No explanation provided'
      }));
    } catch (error) {
      console.error('Error parsing AI response:', error);
      // Return fallback questions
      return this.generateFallbackQuestions(subject, topic, count);
    }
  }

  generateFallbackQuestions(subject, topic, count) {
    const questions = [];
    for (let i = 0; i < count; i++) {
      questions.push({
        question: `Sample question ${i + 1} about ${topic} in ${subject}?`,
        options: [
          'Option A',
          'Option B',
          'Option C',
          'Option D'
        ],
        correctAnswer: Math.floor(Math.random() * 4),
        explanation: 'This is a sample question. AI generation temporarily unavailable.'
      });
    }
    return questions;
  }

  async generateCompetitiveExamQuestions(examName, subjects, count = 20, difficulty = 'mixed') {
    const prompt = `Generate ${count} multiple-choice questions for ${examName} competitive exam preparation.

Subjects to cover: ${subjects.join(', ')}
Difficulty: ${difficulty}
Distribute questions evenly across all subjects.

Requirements:
- Create realistic exam-level questions
- Each question must have exactly 4 options
- Indicate the correct answer index (0-3)
- Provide detailed explanations
- Match the actual exam pattern and difficulty

Return ONLY a valid JSON array with this structure:
[
  {
    "question": "Question text?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctAnswer": 0,
    "explanation": "Detailed explanation",
    "subject": "Subject name"
  }
]

IMPORTANT: Return ONLY the JSON array, no additional text.`;

    try {
      const response = await this.generateContent(prompt);
      let jsonStr = response.trim().replace(/```json\n?/g, '').replace(/```\n?/g, '');
      const questions = JSON.parse(jsonStr);
      
      return questions.slice(0, count).map(q => ({
        question: q.question,
        options: q.options.slice(0, 4),
        correctAnswer: Math.max(0, Math.min(3, q.correctAnswer)),
        explanation: q.explanation || 'No explanation provided',
        subject: q.subject || subjects[0]
      }));
    } catch (error) {
      console.error('Error generating competitive exam questions:', error);
      return this.generateFallbackQuestions(examName, subjects.join(', '), count);
    }
  }
}

module.exports = new GeminiService();
