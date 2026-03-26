# 🔧 Render Deployment Troubleshooting Guide

## Current Issue: 502 Bad Gateway

The 502 error means the server failed to start or crashed immediately after starting.

---

## Quick Fix Steps

### Step 1: Check Environment Variables in Render

Go to your Render dashboard and verify ALL these environment variables are set:

```
PORT=5002
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster-url>/campusHelper?retryWrites=true&w=majority
JWT_SECRET=your_super_secret_jwt_key_change_this_in_production
JWT_EXPIRE=7d
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
NODE_ENV=production
CORS_ORIGIN=*
```

### Step 2: Check Render Logs

1. Go to https://dashboard.render.com
2. Click on your `campushelper-backend` service
3. Go to **"Logs"** tab
4. Look for error messages

Common errors to look