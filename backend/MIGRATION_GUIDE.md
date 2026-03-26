# 📦 Data Migration Guide: Local MongoDB → MongoDB Atlas

This guide will help you migrate your local MongoDB data to MongoDB Atlas cloud database.

## Prerequisites

✅ Local MongoDB running with data in `campusHelper2` database  
✅ MongoDB Atlas account (free tier)  
✅ All dependencies installed (`npm install`)

## Step 1: Set Up MongoDB Atlas

### 1.1 Create Free Cluster

1. Go to https://www.mongodb.com/cloud/atlas
2. Sign up or log in
3. Click **"Build a Database"**
4. Choose **"M0 FREE"** tier
5. Select cloud provider and region (choose closest to you)
6. Cluster Name: `Cluster0` (default is fine)
7. Click **"Create"**

### 1.2 Create Database User

1. Click **"Database Access"** in left menu
2. Click **"Add New Database User"**
3. Choose **"Password"** authentication method
4. Set **Username** (e.g., `campushelper`)
5. Set **Password** (save this securely!)
6. Under "Database User Privileges", select **"Read and write to any database"**
7. Click **"Add User"**

### 1.3 Configure Network Access

1. Click **"Network Access"** in left menu
2. Click **"Add IP Address"**
3. Click **"Allow Access from Anywhere"** (or add your IP)
   - IP: `0.0.0.0/0` (allows all IPs - needed for Render deployment)
4. Click **"Confirm"**

### 1.4 Get Connection String

1. Go back to **"Database"** tab
2. Click **"Connect"** button on your cluster
3. Choose **"Connect your application"**
4. Select **Driver: Node.js** and **Version: 5.5 or later**
5. Copy the connection string, it looks like:
   ```
   mongodb+srv://campushelper:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
6. **Replace** `<password>` with your actual database user password
7. **Add** database name after `.net/`: `/campusHelper`

**Final connection string should look like:**
```
mongodb+srv://campushelper:YourPassword123@cluster0.abc123.mongodb.net/campusHelper?retryWrites=true&w=majority
```

## Step 2: Configure Migration

### 2.1 Update .env File

Open your `.env` file and update the `MONGODB_ATLAS_URI`:

```env
# Replace with your actual Atlas connection string
MONGODB_ATLAS_URI=mongodb+srv://campushelper:YourPassword123@cluster0.abc123.mongodb.net/campusHelper?retryWrites=true&w=majority
```

**Important:** 
- Replace `campushelper` with your database username
- Replace `YourPassword123` with your database password
- Replace `cluster0.abc123` with your actual cluster address
- Keep `/campusHelper` as the database name

### 2.2 Verify Local Data

Check what data you have locally:

```bash
# Option 1: Using MongoDB Compass (GUI)
# Open MongoDB Compass and connect to: mongodb://localhost:27017/campusHelper2

# Option 2: Using mongo shell
mongo
use campusHelper2
show collections
db.users.count()
db.subjects.count()
db.materials.count()
```

## Step 3: Run Migration

### 3.1 Start Migration Script

Make sure your local MongoDB is running, then execute:

```bash
node migrate-to-atlas.js
```

### 3.2 Expected Output

You should see:
```
🔄 Starting data migration...

📡 Connecting to local MongoDB...
✅ Connected to local MongoDB

📦 Fetching data from local database...
📊 Found:
   - Users: 5
   - Subjects: 10
   - Materials: 15
   - Tests: 8
   - Progress: 5
   - Competitive Exams: 3

✅ Local connection closed

📡 Connecting to MongoDB Atlas...
✅ Connected to MongoDB Atlas

🗑️  Clearing existing data in Atlas...
✅ Existing data cleared

📤 Uploading data to MongoDB Atlas...
✅ Uploaded 5 users
✅ Uploaded 10 subjects
✅ Uploaded 15 materials
✅ Uploaded 8 tests
✅ Uploaded 5 progress records
✅ Uploaded 3 competitive exams

🎉 Migration completed successfully!

📝 Summary:
   Total documents migrated: 46

✅ Atlas connection closed
```

## Step 4: Verify Migration

### 4.1 Check Data in Atlas

1. Go to MongoDB Atlas dashboard
2. Click **"Browse Collections"** on your cluster
3. Select database: `campusHelper`
4. Check collections: `users`, `subjects`, `materials`, etc.
5. Verify document counts match the migration output

### 4.2 Test Connection from App

Update your `.env` to use Atlas:

```env
# Comment out local MongoDB
# MONGODB_URI=mongodb://localhost:27017/campusHelper2

# Use MongoDB Atlas
MONGODB_URI=mongodb+srv://campushelper:YourPassword123@cluster0.abc123.mongodb.net/campusHelper?retryWrites=true&w=majority
```

Restart your server:
```bash
npm start
```

You should see:
```
✅ MongoDB connected successfully
🚀 Server running on port 5002 in development mode
```

## Step 5: Update for Production

### 5.1 For Render Deployment

When deploying to Render, add environment variable:
- **Key:** `MONGODB_URI`
- **Value:** Your Atlas connection string

### 5.2 For Android App

No changes needed! The app will automatically use Atlas when backend is deployed.

## Troubleshooting

### ❌ Error: "Authentication failed"

**Solution:** Check username and password in connection string

```env
# Make sure password is URL-encoded if it contains special characters
# Example: password "Pass@123" should be "Pass%40123"
```

### ❌ Error: "Network timeout"

**Solution:** Check network access settings
1. Go to Atlas → Network Access
2. Ensure `0.0.0.0/0` is added
3. Wait 2-3 minutes for changes to take effect

### ❌ Error: "Database not found"

**Solution:** Ensure database name is in connection string
```
mongodb+srv://...mongodb.net/campusHelper?retryWrites=true
                              ^^^^^^^^^^^^
                              Add this!
```

### ❌ Error: "MONGODB_ATLAS_URI not found"

**Solution:** Make sure you added it to `.env` file
```env
MONGODB_ATLAS_URI=mongodb+srv://...
```

### ⚠️ Data doesn't appear

**Solution:** Check if migration completed successfully
- Look for "Migration completed successfully" message
- Verify document counts in Atlas dashboard
- Re-run migration if needed

## Migration Options

### Option 1: Keep Local + Atlas (Recommended for Development)

```env
# Use local for development
MONGODB_URI=mongodb://localhost:27017/campusHelper2

# Use Atlas for production (Render will use this)
MONGODB_ATLAS_URI=mongodb+srv://...
```

### Option 2: Switch to Atlas Permanently

```env
# Use Atlas everywhere
MONGODB_URI=mongodb+srv://campushelper:password@cluster0.xxxxx.mongodb.net/campusHelper?retryWrites=true&w=majority

# Can remove local URI
```

### Option 3: Re-run Migration Anytime

To update Atlas with latest local data:
```bash
node migrate-to-atlas.js
```

**Note:** This will **clear existing Atlas data** and replace with local data.

## Important Notes

### 📁 File Uploads (uploads folder)

**Migration script only migrates database data**, not uploaded files!

If you have PDFs/images in `uploads/` folder:
1. Files are stored locally on your computer
2. MongoDB only stores the file **paths** (e.g., `/uploads/file.pdf`)
3. For production, consider:
   - Upload files to cloud storage (AWS S3, Cloudinary, Firebase Storage)
   - Update file URLs in database
   - Or re-upload materials through the app

### 🔒 Security

- **Never commit** `.env` file to GitHub (it's in `.gitignore`)
- Use **strong passwords** for database users
- Regularly **rotate** credentials
- For production, **restrict IP access** instead of 0.0.0.0/0

### 💾 Backup

Before migration, backup local data:
```bash
# Using mongodump
mongodump --db campusHelper2 --out ./backup

# Restore later if needed
mongorestore --db campusHelper2 ./backup/campusHelper2
```

### 🔄 Keeping Data in Sync

If you develop locally and want to keep Atlas updated:
1. Make changes to local database
2. Run migration: `node migrate-to-atlas.js`
3. Test with Atlas connection
4. Deploy to Render

## Quick Reference

| Task | Command |
|------|---------|
| Migrate data | `node migrate-to-atlas.js` |
| Start local server | `npm start` |
| Test Atlas connection | Update MONGODB_URI in .env, then `npm start` |
| Backup local data | `mongodump --db campusHelper2 --out ./backup` |
| Restore local data | `mongorestore --db campusHelper2 ./backup/campusHelper2` |

## Next Steps

1. ✅ Migrate data to Atlas
2. ✅ Verify data in Atlas dashboard  
3. ✅ Test backend with Atlas connection
4. 🚀 Deploy to Render
5. 📱 Update Android app base URL

---

**Need Help?**
- MongoDB Atlas Docs: https://docs.atlas.mongodb.com
- Check migration script logs for detailed errors
- Ensure local MongoDB is running before migration

🎉 **Success!** Your data is now in the cloud and ready for production deployment!
