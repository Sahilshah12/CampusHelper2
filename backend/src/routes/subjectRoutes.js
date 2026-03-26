const express = require('express');
const router = express.Router();
const subjectController = require('../controllers/subjectController');
const { protect } = require('../middlewares/authMiddleware');
const { isAdmin } = require('../middlewares/roleMiddleware');

router.get('/', protect, subjectController.getAllSubjects);
router.get('/:id', protect, subjectController.getSubject);
router.post('/', protect, isAdmin, subjectController.createSubject);
router.put('/:id', protect, isAdmin, subjectController.updateSubject);
router.delete('/:id', protect, isAdmin, subjectController.deleteSubject);

module.exports = router;
