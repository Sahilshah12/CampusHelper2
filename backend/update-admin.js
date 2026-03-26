const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
require('dotenv').config();

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/campus_helper';

async function updateAdmin() {
    try {
        console.log('🔄 Updating admin credentials...\n');

        // Connect to MongoDB
        console.log('📡 Connecting to MongoDB...');
        await mongoose.connect(MONGODB_URI);
        console.log('✅ Connected to MongoDB\n');

        const User = mongoose.model('User', new mongoose.Schema({
            name: String,
            email: String,
            password: String,
            role: String,
            isActive: { type: Boolean, default: true },
            profileImage: String
        }, { strict: false }));

        // New admin credentials
        const adminEmail = 'sahil8219@gmail.com';
        const adminPassword = 'Sahil8219';
        const adminName = 'Sahil Shah';

        // Check if admin exists
        let admin = await User.findOne({ email: adminEmail });

        // Hash the password
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(adminPassword, salt);

        if (admin) {
            // Update existing admin
            await User.updateOne(
                { email: adminEmail },
                { 
                    $set: { 
                        password: hashedPassword,
                        name: adminName,
                        role: 'admin',
                        isActive: true
                    } 
                }
            );
            console.log(`✅ Updated existing admin: ${adminEmail}`);
        } else {
            // Create new admin
            admin = await User.create({
                name: adminName,
                email: adminEmail,
                password: hashedPassword,
                role: 'admin',
                isActive: true,
                profileImage: null
            });
            console.log(`✅ Created new admin: ${adminEmail}`);
        }

        console.log('\n🎉 Admin credentials updated successfully!');
        console.log('\n📝 Login credentials:');
        console.log('━'.repeat(50));
        console.log(`Email: ${adminEmail}`);
        console.log(`Password: ${adminPassword}`);
        console.log(`Role: admin`);
        console.log('━'.repeat(50));

        await mongoose.connection.close();
        console.log('\n✅ Connection closed');

    } catch (error) {
        console.error('❌ Error:', error.message);
        console.error(error);
    } finally {
        process.exit();
    }
}

updateAdmin();
