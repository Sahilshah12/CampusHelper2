const mongoose = require('mongoose');
require('dotenv').config();

// Import all models
const User = require('./src/models/userModel');
const Subject = require('./src/models/subjectModel');
const Material = require('./src/models/materialModel');
const Test = require('./src/models/testModel');
const Progress = require('./src/models/progressModel');
const CompetitiveExam = require('./src/models/competitiveExamModel');

// Local and Atlas connection strings
const LOCAL_URI = 'mongodb://localhost:27017/campusHelper2';
const ATLAS_URI = process.env.MONGODB_ATLAS_URI; // Add this to your .env file

async function migrateData() {
    try {
        console.log('🔄 Starting data migration...\n');

        // Connect to local MongoDB
        console.log('📡 Connecting to local MongoDB...');
        const localConnection = await mongoose.createConnection(LOCAL_URI);
        console.log('✅ Connected to local MongoDB\n');

        // Get all data from local database
        console.log('📦 Fetching data from local database...');
        const users = await localConnection.model('User', User.schema).find({}).lean();
        const subjects = await localConnection.model('Subject', Subject.schema).find({}).lean();
        const materials = await localConnection.model('Material', Material.schema).find({}).lean();
        const tests = await localConnection.model('Test', Test.schema).find({}).lean();
        const progress = await localConnection.model('Progress', Progress.schema).find({}).lean();
        const exams = await localConnection.model('CompetitiveExam', CompetitiveExam.schema).find({}).lean();

        console.log(`📊 Found:`);
        console.log(`   - Users: ${users.length}`);
        console.log(`   - Subjects: ${subjects.length}`);
        console.log(`   - Materials: ${materials.length}`);
        console.log(`   - Tests: ${tests.length}`);
        console.log(`   - Progress: ${progress.length}`);
        console.log(`   - Competitive Exams: ${exams.length}\n`);

        // Close local connection
        await localConnection.close();
        console.log('✅ Local connection closed\n');

        // Connect to MongoDB Atlas
        console.log('📡 Connecting to MongoDB Atlas...');
        if (!ATLAS_URI) {
            throw new Error('❌ MONGODB_ATLAS_URI not found in .env file');
        }
        const atlasConnection = await mongoose.createConnection(ATLAS_URI);
        console.log('✅ Connected to MongoDB Atlas\n');

        // Clear existing data in Atlas (optional - comment out if you want to keep existing data)
        console.log('🗑️  Clearing existing data in Atlas...');
        await atlasConnection.model('User', User.schema).deleteMany({});
        await atlasConnection.model('Subject', Subject.schema).deleteMany({});
        await atlasConnection.model('Material', Material.schema).deleteMany({});
        await atlasConnection.model('Test', Test.schema).deleteMany({});
        await atlasConnection.model('Progress', Progress.schema).deleteMany({});
        await atlasConnection.model('CompetitiveExam', CompetitiveExam.schema).deleteMany({});
        console.log('✅ Existing data cleared\n');

        // Insert data into Atlas
        console.log('📤 Uploading data to MongoDB Atlas...');
        
        if (users.length > 0) {
            await atlasConnection.model('User', User.schema).collection.insertMany(users);
            console.log(`✅ Uploaded ${users.length} users`);
        }
        
        if (subjects.length > 0) {
            await atlasConnection.model('Subject', Subject.schema).collection.insertMany(subjects);
            console.log(`✅ Uploaded ${subjects.length} subjects`);
        }
        
        if (materials.length > 0) {
            await atlasConnection.model('Material', Material.schema).collection.insertMany(materials);
            console.log(`✅ Uploaded ${materials.length} materials`);
        }
        
        if (tests.length > 0) {
            await atlasConnection.model('Test', Test.schema).collection.insertMany(tests);
            console.log(`✅ Uploaded ${tests.length} tests`);
        }
        
        if (progress.length > 0) {
            await atlasConnection.model('Progress', Progress.schema).collection.insertMany(progress);
            console.log(`✅ Uploaded ${progress.length} progress records`);
        }
        
        if (exams.length > 0) {
            await atlasConnection.model('CompetitiveExam', CompetitiveExam.schema).collection.insertMany(exams);
            console.log(`✅ Uploaded ${exams.length} competitive exams`);
        }

        console.log('\n🎉 Migration completed successfully!');
        console.log('\n📝 Summary:');
        console.log(`   Total documents migrated: ${users.length + subjects.length + materials.length + tests.length + progress.length + exams.length}`);

        // Close Atlas connection
        await atlasConnection.close();
        console.log('\n✅ Atlas connection closed');

    } catch (error) {
        console.error('❌ Migration failed:', error.message);
        console.error(error);
    } finally {
        process.exit();
    }
}

// Run migration
migrateData();
