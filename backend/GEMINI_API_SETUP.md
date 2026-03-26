# Fix AI Chatbot - Gemini API Setup

## Problem
The AI chatbot is returning 500 errors because the `GEMINI_API_KEY` environment variable is not set on Render.

## Solution

### Step 1: Get a Gemini API Key (FREE)

1. **Go to Google AI Studio**: https://makersuite.google.com/app/apikey
2. **Click "Create API Key"**
3. **Select your Google Cloud project** (or create a new one)
4. **Copy the API key** (starts with `AIza...`)

**Note**: The free tier includes:
- 60 requests per minute
- 1,500 requests per day
- Perfect for development and small-scale use

### Step 2: Add API Key to Render

1. **Go to Render Dashboard**: https://dashboard.render.com/
2. **Select your `campushelper-be` service**
3. **Click "Environment" tab** (left sidebar)
4. **Click "Add Environment Variable"**
5. **Add the following:**
   - **Key**: `GEMINI_API_KEY`
   - **Value**: Your API key from Step 1 (paste the entire key)
6. **Click "Save Changes"**

**Render will automatically redeploy your service** (takes 2-3 minutes)

### Step 3: Verify Setup

After redeployment completes, test the AI chatbot:

```bash
# Test from PowerShell (replace TOKEN with your login token)
curl -X POST https://campushelper-be.onrender.com/api/ai/chat `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_TOKEN_HERE" `
  -d '{"subjectId":"68f9fc68b5d69534c99ee7ae","topic":"General Discussion","question":"What is sorting?"}'
```

**Expected Response:**
```json
{
  "success": true,
  "response": "Sorting is the process of arranging data...",
  "context": {
    "subject": "Data Structures",
    "topic": "General Discussion",
    "question": "What is sorting?"
  }
}
```

### Step 4: Test in Android App

1. **Open the app**
2. **Navigate to a subject**
3. **Open AI Chat**
4. **Send a message**
5. **Should receive AI response** (no more 500 errors!)

## Alternative: Local Testing

If you want to test locally first:

1. **Create `.env` file** in `backend/` folder:
   ```env
   GEMINI_API_KEY=AIza...your-key-here
   MONGODB_URI=mongodb+srv://...your-atlas-uri
   JWT_SECRET=your_secret_key
   ```

2. **Run backend locally**:
   ```bash
   cd backend
   npm install
   npm start
   ```

3. **Update Android app** to use local backend:
   ```kotlin
   // In Constants.kt (temporarily)
   const val BASE_URL = "http://10.0.2.2:5001/api/"  // For emulator
   // or
   const val BASE_URL = "http://YOUR_IP:5001/api/"  // For physical device
   ```

## Improved Error Messages

I've updated the backend to show better error messages:

**Before:**
```json
{"error": "Failed to get AI response"}
```

**After:**
```json
{
  "error": "Gemini API key is not configured. Please set GEMINI_API_KEY environment variable",
  "hint": "Please configure GEMINI_API_KEY environment variable"
}
```

This will help you debug issues faster!

## Free Tier Limits

**Google Gemini API (Free Tier):**
- ✅ 60 requests per minute
- ✅ 1,500 requests per day
- ✅ No credit card required
- ✅ Perfect for student projects

**If you exceed limits:**
- Wait 1 minute for rate limit reset
- Wait 24 hours for daily quota reset
- Or upgrade to paid tier (pay-as-you-go)

## Troubleshooting

### "Invalid API key" error
- Double-check you copied the entire key
- Make sure there are no extra spaces
- Verify the key is active in Google Cloud Console

### "Quota exceeded" error
- You've hit the free tier limit
- Wait 24 hours or upgrade

### Still getting 500 errors
- Check Render logs: Dashboard → Service → Logs
- Look for "Gemini API Error" messages
- Verify environment variable is saved

## Security Note

**NEVER commit API keys to Git!** Always use environment variables:

✅ **Good**: `GEMINI_API_KEY=AIza...` (in Render environment variables)
❌ **Bad**: Hardcoding in code `const apiKey = "AIza..."`

The `.env` file is already in `.gitignore`, so local keys are safe.
