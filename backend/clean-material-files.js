const mongoose = require('mongoose');
require('dotenv').config();

const ATLAS_URI = process.env.MONGODB_ATLAS_URI;

async function cleanMaterialFiles() {
    try {
        console.log('🔄 Cleaning material file references in MongoDB Atlas...\n');

        // Connect to Atlas
        console.log('📡 Connecting to MongoDB Atlas...');
        await mongoose.connect(ATLAS_URI);
        console.log('✅ Connected to MongoDB Atlas\n');

        const Material = mongoose.model('Material', new mongoose.Schema({
            title: String,
            fileUrl: String,
            url: String,
            type: String,
            content: String
        }, { strict: false }));

        // Get all materials
        const materials = await Material.find({});
        console.log(`📊 Found ${materials.length} materials\n`);

        console.log('🔧 Cleaning file references...');

        let updated = 0;
        for (const material of materials) {
            if (material.fileUrl && !material.url) {
                // Remove fileUrl since files don't exist on Render
                // Convert to URL type instead
                console.log(`⚠️  Material: ${material.title}`);
                console.log(`   Had fileUrl: ${material.fileUrl}`);
                
                await Material.updateOne(
                    { _id: material._id },
                    { 
                        $set: { 
                            fileUrl: null,
                            url: '#',
                            type: 'link',
                            content: material.content || 'File needs to be re-uploaded. Original file was not migrated to cloud server.'
                        }
                    }
                );
                
                console.log(`   ✅ Converted to link type with note\n`);
                updated++;
            }
        }

        console.log(`\n🎉 Updated ${updated} materials`);
        console.log('\n📝 Note: Materials with file references have been converted to links.');
        console.log('   Users will need to re-upload PDF files through the admin panel.\n');

        await mongoose.connection.close();
        console.log('✅ Connection closed');

    } catch (error) {
        console.error('❌ Error:', error.message);
        console.error(error);
    } finally {
        process.exit();
    }
}

cleanMaterialFiles();
