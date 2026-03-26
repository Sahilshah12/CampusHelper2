# 🚀 Deployment Guide for Render

## Prerequisites
- GitHub account with your backend repository
- Render account (free tier available at https://render.com)
- MongoDB Atlas account (free tier available)

## Step 1: Set Up MongoDB Atlas

1. Go to https://www.mongodb.com/cloud/atlas
2. Sign up or log in
3. Create a **FREE** cluster:
   - Click "Build a Database"
   - Choose "FREE" shared cluster
   - Select a cloud provider and region (choose closest to you)
   - Click "Create Cluster"

4. Create Database User:
   - Go to "Database Access" in left menu
   - Click "Add New Database User"
   - Choose "Password" authentication
   - Set username and password (save these!)
   - Set user privileges to "Read and write to any database"
   - Click "Add User"

5. Allow Network Access:
   - Go to "Network Access" in left menu
   - Click "Add IP Address"
   - Click "Allow Access from Anywhere" (0.0.0.0/0)
   - Click "Confirm"

6. Get Connection String:
   - Go back to "Database" tab
   - Click "Connect" on your cluster
   - Choose "Connect your application"
   - Copy the connection string
   - Replace `<password>` with your database user password
   - Replace `<dbname>` with your database name (e.g., "campushelper")
   
   Example: `mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/campushelper?retryWrites=true&w=majority`

## Step 2: Deploy to Render

1. **Sign Up/Login to Render**
   - Go to https://dashboard.render.com/
   - Sign up with GitHub (recommended for easy deployment)

2. **Create New Web Service**
   - Click "New +" button
   - Select "Web Service"
   - Click "Connect" next to your GitHub repository
   - Or paste your repo URL: `https://github.com/Sahilshah12/Campushelper_BE`

3. **Configure Service Settings**
   
   **Basic Settings:**
   - **Name:** `campushelper-backend` (or any name you prefer)
   - **Region:** Choose closest to your users
   - **Branch:** `main`
   - **Root Directory:** Leave empty (or specify if backend is in subfolder)
   - **Runtime:** `Node`
   
   **Build & Deploy:**
   - **Build Command:** `npm install`
   - **Start Command:** `npm start`
   
   **Instance Type:**
   - Choose **Free** plan (sufficient for testing/small projects)

4. **Add Environment Variables**
   
   Scroll down to "Environment Variables" section and add:
   
   | Key | Value |
   |-----|-------|
   | `PORT` | `5002` |
   | `MONGODB_URI` | Your MongoDB Atlas connection string from Step 1 |
   | `JWT_SECRET` | A random secure string (e.g., `your-super-secret-jwt-key-2024`) |
   | `GEMINI_API_KEY` | Your Google Gemini API key |
   | `NODE_ENV` | `production` |
   
   **To add each variable:**
   - Click "Add Environment Variable"
   - Enter Key and Value
   - Repeat for all variables

5. **Create Web Service**
   - Click "Create Web Service" button at the bottom
   - Render will start building and deploying your app
   - Wait for deployment to complete (usually 2-5 minutes)

## Step 3: Verify Deployment

1. Once deployed, you'll see:
   - **Status:** "Live" with green indicator
   - **URL:** Something like `https://campushelper-backend.onrender.com`

2. Test your API:
   - Open the URL in browser
   - You should see your API response (if you have a root route)
   - Test endpoints: `https://your-url.onrender.com/api/subjects`

## Step 4: Update Android App

Update your Android app's base URL to use the Render URL:

```kotlin
// In your RetrofitClient or API configuration
const val BASE_URL = "https://campushelper-backend.onrender.com/api/"
```

## Important Notes

### Free Tier Limitations
- **Sleep after inactivity:** Free instances sleep after 15 minutes of inactivity
- **First request slow:** Takes ~30 seconds to wake up from sleep
- **Monthly hours:** 750 hours/month (enough for one service)

### Keep Service Active (Optional)
To prevent sleeping, you can:
1. Use a free uptime monitoring service (e.g., UptimeRobot, Cron-job.org)
2. Ping your URL every 10 minutes
3. Upgrade to paid plan ($7/month) for always-on service

### Auto-Deploy
- Render automatically redeploys when you push to GitHub
- Every `git push` will trigger a new deployment
- Check deployment logs in Render dashboard

## Troubleshooting

### Build Failed
- Check "Logs" tab in Render dashboard
- Verify all dependencies are in `package.json`
- Ensure `npm install` works locally

### Database Connection Error
- Verify MongoDB Atlas connection string is correct
- Check IP whitelist includes 0.0.0.0/0
- Ensure database user has correct permissions

### Environment Variables Not Working
- Check spelling and case sensitivity
- Restart service after adding/changing variables
- View logs to see error messages

### API Returns 404
- Verify your routes are correctly set up
- Check that Express app is listening on correct port
- Test endpoints with the full URL path

## Getting Gemini API Key

1. Go to https://makersuite.google.com/app/apikey
2. Sign in with Google account
3. Click "Create API Key"
4. Copy the API key
5. Add to Render environment variables

## Monitoring Your App

1. **Render Dashboard:**
   - View deployment logs
   - Monitor CPU/Memory usage
   - Check request metrics

2. **MongoDB Atlas:**
   - Monitor database connections
   - Check storage usage
   - View query performance

## Next Steps

1. Set up custom domain (optional)
2. Configure CORS for production
3. Add rate limiting
4. Set up error monitoring (e.g., Sentry)
5. Add API documentation (Swagger/Postman)

## Support

- **Render Docs:** https://render.com/docs
- **MongoDB Atlas Docs:** https://docs.atlas.mongodb.com/
- **Issues:** Create issue on GitHub repository

---

🎉 **Congratulations!** Your Campus Helper Backend is now live and accessible worldwide!

Your API URL: `https://your-service-name.onrender.com`
