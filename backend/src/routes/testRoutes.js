const express = require('express');
const router = express.Router();
const testController = require('../controllers/testController');
const { protect } = require('../middlewares/authMiddleware');

router.post('/generate', protect, testController.generateTest);
router.get('/', protect, testController.getTests);
router.get('/:id/analysis', protect, testController.getTestAnalysis);
router.get('/:id', protect, testController.getTest);
router.post('/:id/submit', protect, testController.submitTest);
router.delete('/:id', protect, testController.deleteTest);

module.exports = router;
