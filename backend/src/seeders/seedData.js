require('dotenv').config();
const mongoose = require('mongoose');
const config = require('../config');

// Models
const User = require('../models/userModel');
const Subject = require('../models/subjectModel');
const CompetitiveExam = require('../models/competitiveExamModel');
const Progress = require('../models/progressModel');

const seedData = async () => {
  try {
    await mongoose.connect(config.mongoUri, {
      useNewUrlParser: true,
      useUnifiedTopology: true
    });

    console.log('✅ Connected to MongoDB');

    // Clear existing data
    await User.deleteMany({});
    await Subject.deleteMany({});
    await CompetitiveExam.deleteMany({});
    await Progress.deleteMany({});

    console.log('🗑️  Cleared existing data');

    // Create Admin User
    const admin = await User.create({
      name: 'Campus Admin',
      email: 'admin@campushelper.com',
      password: 'Admin@123',
      role: 'admin',
      profileImage: null
    });

    // Create Student Users
    const student1 = await User.create({
      name: 'John Doe',
      email: 'john@student.com',
      password: 'Student@123',
      role: 'student',
      profileImage: null
    });

    const student2 = await User.create({
      name: 'Jane Smith',
      email: 'jane@student.com',
      password: 'Student@123',
      role: 'student',
      profileImage: null
    });

    const student3 = await User.create({
      name: 'Vishal Dhiman',
      email: 'vishal123@gmail.com',
      password: 'Vishal123',
      role: 'student',
      profileImage: null
    });

    const admin2 = await User.create({
      name: 'Sahil Shah',
      email: 'sahil8219@gmail.com',
      password: 'Sahil8219',
      role: 'admin',
      profileImage: null
    });

    

    console.log('✅ Created users');

    // Create Progress for students
    await Progress.create({ userId: student1._id });
    await Progress.create({ userId: student2._id });
    await Progress.create({ userId: student3._id });

    console.log('✅ Created progress records');

    // Create Subjects
    const subjects = await Subject.insertMany([
      {
        name: 'Data Structures and Algorithms',
        code: 'CS101',
        description: 'Fundamental data structures and algorithms',
        category: 'Engineering',
        semester: 3,
        credits: 4,
        createdBy: admin._id
      },
      {
        name: 'Database Management Systems',
        code: 'CS201',
        description: 'Introduction to DBMS and SQL',
        category: 'Engineering',
        semester: 4,
        credits: 3,
        createdBy: admin._id
      },
      {
        name: 'Operating Systems',
        code: 'CS301',
        description: 'Concepts of operating systems',
        category: 'Engineering',
        semester: 5,
        credits: 4,
        createdBy: admin._id
      },
      {
        name: 'Computer Networks',
        code: 'CS401',
        description: 'Networking fundamentals and protocols',
        category: 'Engineering',
        semester: 6,
        credits: 3,
        createdBy: admin._id
      },
      {
        name: 'Machine Learning',
        code: 'CS501',
        description: 'Introduction to ML algorithms',
        category: 'Engineering',
        semester: 7,
        credits: 4,
        createdBy: admin._id
      },
      {
        name: 'Physics',
        code: 'PHY101',
        description: 'Classical and Modern Physics',
        category: 'Science',
        semester: 1,
        credits: 3,
        createdBy: admin._id
      },
      {
        name: 'Mathematics',
        code: 'MATH101',
        description: 'Calculus and Linear Algebra',
        category: 'Science',
        semester: 1,
        credits: 4,
        createdBy: admin._id
      }
    ]);

    console.log('✅ Created subjects');

    // Create Competitive Exams
    const competitiveExams = await CompetitiveExam.insertMany([
      {
        name: 'Joint Entrance Examination',
        shortName: 'JEE',
        description: 'Engineering entrance exam for IITs and NITs',
        category: 'Engineering',
        subjects: ['Physics', 'Chemistry', 'Mathematics'],
        questionPattern: {
          totalQuestions: 25,
          duration: 45,
          difficulty: 'hard'
        }
      },
      {
        name: 'National Eligibility cum Entrance Test',
        shortName: 'NEET',
        description: 'Medical entrance exam',
        category: 'Medical',
        subjects: ['Physics', 'Chemistry', 'Biology'],
        questionPattern: {
          totalQuestions: 20,
          duration: 40,
          difficulty: 'hard'
        }
      },
      {
        name: 'Union Public Service Commission',
        shortName: 'UPSC',
        description: 'Civil services examination',
        category: 'Civil Services',
        subjects: ['General Studies', 'Current Affairs', 'History', 'Geography', 'Economy'],
        questionPattern: {
          totalQuestions: 30,
          duration: 60,
          difficulty: 'hard'
        }
      },
      {
        name: 'Common Admission Test',
        shortName: 'CAT',
        description: 'Management entrance exam for IIMs',
        category: 'Management',
        subjects: ['Quantitative Aptitude', 'Verbal Ability', 'Data Interpretation', 'Logical Reasoning'],
        questionPattern: {
          totalQuestions: 25,
          duration: 45,
          difficulty: 'hard'
        }
      },
      {
        name: 'Combined Defence Services',
        shortName: 'CDS',
        description: 'Defence services examination',
        category: 'Defense',
        subjects: ['Mathematics', 'English', 'General Knowledge'],
        questionPattern: {
          totalQuestions: 20,
          duration: 35,
          difficulty: 'medium'
        }
      },
      {
        name: 'National Defence Academy',
        shortName: 'NDA',
        description: 'National Defence Academy entrance',
        category: 'Defense',
        subjects: ['Mathematics', 'General Ability Test'],
        questionPattern: {
          totalQuestions: 20,
          duration: 40,
          difficulty: 'medium'
        }
      },
      {
        name: 'Graduate Aptitude Test in Engineering',
        shortName: 'GATE',
        description: 'Engineering postgraduate entrance',
        category: 'Engineering',
        subjects: ['Engineering Mathematics', 'Core Engineering Subjects'],
        questionPattern: {
          totalQuestions: 25,
          duration: 50,
          difficulty: 'hard'
        }
      }
    ]);

    console.log('✅ Created competitive exams');

    console.log('\n📊 Seed Summary:');
    console.log('\n👤 ADMIN ACCOUNTS:');
    console.log(`   1. admin@campushelper.com / Admin@123`);
    console.log(`   2. sahil8219@gmail.com / Sahil8219`);
    console.log('\n👥 STUDENT ACCOUNTS:');
    console.log(`   1. john@student.com / Student@123`);
    console.log(`   2. jane@student.com / Student@123`);
    console.log(`   3. vishal123@gmail.com / Vishal123`);
    console.log(`\n📚 Data Created:`);
    console.log(`   Subjects: ${subjects.length}`);
    console.log(`   Competitive Exams: ${competitiveExams.length}`);

    console.log('\n✅ Database seeded successfully!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Seed error:', error);
    process.exit(1);
  }
};

seedData();
