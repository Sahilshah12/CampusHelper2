const express = require('express');
const router = express.Router();
const progressController = require('../controllers/progressController');
const { protect } = require('../middlewares/authMiddleware');
const { isAdmin } = require('../middlewares/roleMiddleware');

router.get('/', protect, progressController.getProgress);
router.get('/subject/:subjectId', protect, progressController.getSubjectProgress);
router.get('/competitive-exam/:examId', protect, progressController.getCompetitiveExamProgress);
router.get('/analytics', protect, isAdmin, progressController.getAnalytics);

module.exports = router;
