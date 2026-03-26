const express = require('express');
const router = express.Router();
const competitiveExamController = require('../controllers/competitiveExamController');
const { protect } = require('../middlewares/authMiddleware');
const { isAdmin, isStudent } = require('../middlewares/roleMiddleware');

router.get('/', protect, competitiveExamController.getAllExams);
router.get('/:id', protect, competitiveExamController.getExam);
router.post('/', protect, isAdmin, competitiveExamController.createExam);
router.put('/:id', protect, isAdmin, competitiveExamController.updateExam);
router.delete('/:id', protect, isAdmin, competitiveExamController.deleteExam);
router.post('/:id/enroll', protect, competitiveExamController.enrollExam);
router.post('/:id/generate-test', protect, competitiveExamController.generateCompetitiveTest);

module.exports = router;
