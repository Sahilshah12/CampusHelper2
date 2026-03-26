const express = require('express');
const router = express.Router();
const materialController = require('../controllers/materialController');
const { protect } = require('../middlewares/authMiddleware');
const { isAdmin } = require('../middlewares/roleMiddleware');
const upload = require('../middlewares/uploadMiddleware');

router.get('/', protect, materialController.getMaterials);
router.get('/:id', protect, materialController.getMaterial);
router.post('/', protect, isAdmin, upload.single('file'), materialController.createMaterial);
router.put('/:id', protect, isAdmin, upload.single('file'), materialController.updateMaterial);
router.delete('/:id', protect, isAdmin, materialController.deleteMaterial);

module.exports = router;
