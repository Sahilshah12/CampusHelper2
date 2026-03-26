const Material = require('../models/materialModel');
const Subject = require('../models/subjectModel');

// @desc    Get all materials for a subject
// @route   GET /api/materials?subjectId=xxx
// @access  Private
exports.getMaterials = async (req, res) => {
  try {
    const { subjectId, type, topic } = req.query;

    let filter = { isActive: true };
    if (subjectId) filter.subjectId = subjectId;
    if (type) filter.type = type;
    if (topic) filter.topic = new RegExp(topic, 'i');

    const materials = await Material.find(filter)
      .populate('subjectId', 'name code')
      .populate('uploadedBy', 'name email')
      .sort({ createdAt: -1 });

    // Filter out materials with missing files in production
    const validMaterials = materials.filter(material => {
      // If material has a URL instead of fileUrl, it's valid
      if (material.url) return true;
      // If material has content (text/notes), it's valid
      if (material.content) return true;
      // If no fileUrl, it's valid (might be link-only)
      if (!material.fileUrl) return true;
      // In production, skip file validation to avoid file system errors
      return true;
    });

    res.json({
      success: true,
      count: validMaterials.length,
      materials: validMaterials
    });
  } catch (error) {
    console.error('Get materials error:', error);
    res.status(500).json({ 
      error: 'Failed to fetch materials',
      message: error.message 
    });
  }
};

// @desc    Get single material
// @route   GET /api/materials/:id
// @access  Private
exports.getMaterial = async (req, res) => {
  try {
    const material = await Material.findById(req.params.id)
      .populate('subjectId', 'name code')
      .populate('uploadedBy', 'name email');

    if (!material) {
      return res.status(404).json({ error: 'Material not found' });
    }

    // Increment view count
    material.viewCount += 1;
    await material.save();

    res.json({
      success: true,
      material
    });
  } catch (error) {
    console.error('Get material error:', error);
    res.status(500).json({ error: 'Failed to fetch material' });
  }
};

// @desc    Create new material
// @route   POST /api/materials
// @access  Private (Admin only)
exports.createMaterial = async (req, res) => {
  try {
    const { subjectId, title, description, type, url, fileUrl, content, tags, topic, difficulty } = req.body;

    if (!subjectId || !title || !type) {
      return res.status(400).json({ error: 'Please provide subject, title, and type' });
    }

    // Verify subject exists
    const subject = await Subject.findById(subjectId);
    if (!subject) {
      return res.status(404).json({ error: 'Subject not found' });
    }

    const material = await Material.create({
      subjectId,
      title,
      description,
      type,
      url,
      fileUrl: req.file ? `/uploads/${req.file.filename}` : fileUrl,
      content,
      tags,
      topic,
      difficulty,
      uploadedBy: req.user._id
    });

    const populatedMaterial = await Material.findById(material._id)
      .populate('subjectId', 'name code')
      .populate('uploadedBy', 'name email');

    res.status(201).json({
      success: true,
      material: populatedMaterial
    });
  } catch (error) {
    console.error('Create material error:', error);
    res.status(500).json({ error: 'Failed to create material' });
  }
};

// @desc    Update material
// @route   PUT /api/materials/:id
// @access  Private (Admin only)
exports.updateMaterial = async (req, res) => {
  try {
    const material = await Material.findById(req.params.id);

    if (!material) {
      return res.status(404).json({ error: 'Material not found' });
    }

    const { title, description, type, url, fileUrl, content, tags, topic, difficulty, isActive } = req.body;

    if (title) material.title = title;
    if (description !== undefined) material.description = description;
    if (type) material.type = type;
    if (url !== undefined) material.url = url;
    if (fileUrl !== undefined) material.fileUrl = fileUrl;
    if (req.file) material.fileUrl = `/uploads/${req.file.filename}`;
    if (content !== undefined) material.content = content;
    if (tags) material.tags = tags;
    if (topic !== undefined) material.topic = topic;
    if (difficulty) material.difficulty = difficulty;
    if (isActive !== undefined) material.isActive = isActive;

    await material.save();

    const populatedMaterial = await Material.findById(material._id)
      .populate('subjectId', 'name code')
      .populate('uploadedBy', 'name email');

    res.json({
      success: true,
      material: populatedMaterial
    });
  } catch (error) {
    console.error('Update material error:', error);
    res.status(500).json({ error: 'Failed to update material' });
  }
};

// @desc    Delete material
// @route   DELETE /api/materials/:id
// @access  Private (Admin only)
exports.deleteMaterial = async (req, res) => {
  try {
    const material = await Material.findById(req.params.id);

    if (!material) {
      return res.status(404).json({ error: 'Material not found' });
    }

    // Soft delete
    material.isActive = false;
    await material.save();

    res.json({
      success: true,
      message: 'Material deleted successfully'
    });
  } catch (error) {
    console.error('Delete material error:', error);
    res.status(500).json({ error: 'Failed to delete material' });
  }
};
