const express = require('express');
const router = express.Router();
const aiController = require('../controllers/aiController');
const { protect } = require('../middlewares/authMiddleware');

router.post('/chat', protect, aiController.chat);
router.post('/generate-questions', protect, aiController.generateQuestions);
router.post('/study-tips', protect, aiController.getStudyTips);

module.exports = router;
