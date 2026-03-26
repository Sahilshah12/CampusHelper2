const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
require('dotenv').config();

const ATLAS_URI = process.env.MONGODB_ATLAS_URI;

// Default passwords for migrated users
const DEFAULT_PASSWORDS = {
    'admin@campushelper.com': 'Admin123',
    'amit123@gmail.com': 'Amit123',
    // Add more users if needed
};

async function fixPasswords() {
    try {
        console.log('🔄 Fixing user passwords in MongoDB Atlas...\n');

        // Connect to Atlas
        console.log('📡 Connecting to MongoDB Atlas...');
        await mongoose.connect(ATLAS_URI);
        console.log('✅ Connected to MongoDB Atlas\n');

        const User = mongoose.model('User', new mongoose.Schema({
            name: String,
            email: String,
            password: String,
            role: String,
            isActive: Boolean
        }, { strict: false }));

        // Get all users
        const users = await User.find({});
        console.log(`📊 Found ${users.length} users\n`);

        console.log('🔧 Updating passwords...');

        for (const user of users) {
            const plainPassword = DEFAULT_PASSWORDS[user.email] || 'Password123';
            
            // Hash the password
            const salt = await bcrypt.genSalt(10);
            const hashedPassword = await bcrypt.hash(plainPassword, salt);

            // Update directly in database
            await User.updateOne(
                { _id: user._id },
                { $set: { password: hashedPassword } }
            );

            console.log(`✅ Updated password for: ${user.email} (${user.name})`);
            console.log(`   Default password: ${plainPassword}`);
        }

        console.log('\n🎉 All passwords updated successfully!');
        console.log('\n📝 Login credentials:');
        console.log('━'.repeat(50));
        
        for (const user of users) {
            const pwd = DEFAULT_PASSWORDS[user.email] || 'Password123';
            console.log(`Email: ${user.email}`);
            console.log(`Password: ${pwd}`);
            console.log(`Role: ${user.role}`);
            console.log('━'.repeat(50));
        }

        await mongoose.connection.close();
        console.log('\n✅ Connection closed');

    } catch (error) {
        console.error('❌ Error:', error.message);
        console.error(error);
    } finally {
        process.exit();
    }
}

fixPasswords();
