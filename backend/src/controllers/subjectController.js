const Subject = require('../models/subjectModel');

// @desc    Get all subjects
// @route   GET /api/subjects
// @access  Private
exports.getAllSubjects = async (req, res) => {
  try {
    const { category, semester } = req.query;
    
    let filter = { isActive: true };
    if (category) filter.category = category;
    if (semester) filter.semester = parseInt(semester);

    const subjects = await Subject.find(filter)
      .populate('createdBy', 'name email')
      .sort({ name: 1 });

    res.json({
      success: true,
      count: subjects.length,
      subjects
    });
  } catch (error) {
    console.error('Get subjects error:', error);
    res.status(500).json({ error: 'Failed to fetch subjects' });
  }
};

// @desc    Get single subject
// @route   GET /api/subjects/:id
// @access  Private
exports.getSubject = async (req, res) => {
  try {
    const subject = await Subject.findById(req.params.id)
      .populate('createdBy', 'name email');

    if (!subject) {
      return res.status(404).json({ error: 'Subject not found' });
    }

    res.json({
      success: true,
      subject
    });
  } catch (error) {
    console.error('Get subject error:', error);
    res.status(500).json({ error: 'Failed to fetch subject' });
  }
};

// @desc    Create new subject
// @route   POST /api/subjects
// @access  Private (Admin only)
exports.createSubject = async (req, res) => {
  try {
    const { name, code, description, category, semester, credits } = req.body;

    if (!name || !code) {
      return res.status(400).json({ error: 'Please provide name and code' });
    }

    // Check if subject code already exists
    const existingSubject = await Subject.findOne({ code: code.toUpperCase() });
    if (existingSubject) {
      return res.status(400).json({ error: 'Subject code already exists' });
    }

    const subject = await Subject.create({
      name,
      code: code.toUpperCase(),
      description,
      category,
      semester,
      credits,
      createdBy: req.user._id
    });

    res.status(201).json({
      success: true,
      subject
    });
  } catch (error) {
    console.error('Create subject error:', error);
    res.status(500).json({ error: 'Failed to create subject' });
  }
};

// @desc    Update subject
// @route   PUT /api/subjects/:id
// @access  Private (Admin only)
exports.updateSubject = async (req, res) => {
  try {
    const subject = await Subject.findById(req.params.id);

    if (!subject) {
      return res.status(404).json({ error: 'Subject not found' });
    }

    const { name, code, description, category, semester, credits, isActive } = req.body;

    if (name) subject.name = name;
    if (code) {
      const existingSubject = await Subject.findOne({ 
        code: code.toUpperCase(), 
        _id: { $ne: subject._id } 
      });
      if (existingSubject) {
        return res.status(400).json({ error: 'Subject code already exists' });
      }
      subject.code = code.toUpperCase();
    }
    if (description !== undefined) subject.description = description;
    if (category) subject.category = category;
    if (semester !== undefined) subject.semester = semester;
    if (credits !== undefined) subject.credits = credits;
    if (isActive !== undefined) subject.isActive = isActive;

    await subject.save();

    res.json({
      success: true,
      subject
    });
  } catch (error) {
    console.error('Update subject error:', error);
    res.status(500).json({ error: 'Failed to update subject' });
  }
};

// @desc    Delete subject
// @route   DELETE /api/subjects/:id
// @access  Private (Admin only)
exports.deleteSubject = async (req, res) => {
  try {
    const subject = await Subject.findById(req.params.id);

    if (!subject) {
      return res.status(404).json({ error: 'Subject not found' });
    }

    // Soft delete
    subject.isActive = false;
    await subject.save();

    res.json({
      success: true,
      message: 'Subject deleted successfully'
    });
  } catch (error) {
    console.error('Delete subject error:', error);
    res.status(500).json({ error: 'Failed to delete subject' });
  }
};
