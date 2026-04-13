const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const morgan = require('morgan');
const path = require('path');
const fs = require('fs');
const config = require('./config');

// Import routes
const authRoutes = require('./routes/authRoutes');
const subjectRoutes = require('./routes/subjectRoutes');
const materialRoutes = require('./routes/materialRoutes');
const testRoutes = require('./routes/testRoutes');
const progressRoutes = require('./routes/progressRoutes');
const competitiveExamRoutes = require('./routes/competitiveExamRoutes');
const aiRoutes = require('./routes/aiRoutes');

const app = express();

// Create uploads directory if it doesn't exist
const uploadsDir = path.join(__dirname, '..', 'uploads');
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir, { recursive: true });
  console.log('📁 Created uploads directory');
}

// Middleware
app.use(helmet());
app.use(cors({ origin: config.corsOrigin }));
app.use(compression());
app.use(morgan(config.nodeEnv === 'development' ? 'dev' : 'combined'));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Static files for uploads with error handling
app.use('/uploads', express.static(uploadsDir, {
  fallthrough: true,
  setHeaders: (res, filePath) => {
    res.set('Cache-Control', 'public, max-age=86400');
  }
}));

// Handle missing upload files
app.use('/uploads/*', (req, res) => {
  res.status(404).json({ 
    error: 'File not found',
    message: 'The requested file does not exist on the server. Files need to be re-uploaded.'
  });
});

// Health check
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    environment: config.nodeEnv 
  });
});

// Privacy policy page for Google Play Console
app.get('/privacy-policy', (req, res) => {
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(`
    <!doctype html>
    <html lang="en">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>Privacy Policy - Campus Helper</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            max-width: 900px;
            margin: 0 auto;
            padding: 32px 20px;
            color: #1f2937;
            background: #ffffff;
          }
          h1, h2 { color: #111827; }
          p, li { font-size: 16px; }
          ul { padding-left: 20px; }
          .updated { color: #6b7280; font-size: 14px; }
        </style>
      </head>
      <body>
        <h1>Privacy Policy for Campus Helper</h1>
        <p class="updated">Last updated: ${new Date().toISOString().split('T')[0]}</p>

        <p>Campus Helper is an educational application that helps students manage subjects, study materials, tests, progress tracking, and AI-assisted learning.</p>

        <h2>Information We Collect</h2>
        <ul>
          <li>Account details such as name, email address, and password when you register.</li>
          <li>Learning activity such as test history, progress data, and subject preferences.</li>
          <li>Uploaded content such as study materials you submit through the app.</li>
          <li>Basic device and app usage data used for security, diagnostics, and performance.</li>
        </ul>

        <h2>How We Use Information</h2>
        <ul>
          <li>To create and manage user accounts.</li>
          <li>To provide learning features, including materials, tests, and progress tracking.</li>
          <li>To improve app performance and fix technical issues.</li>
          <li>To protect the app from misuse and unauthorized access.</li>
        </ul>

        <h2>Data Sharing</h2>
        <p>We do not sell your personal information. We may share data only with trusted service providers that help operate the app, such as hosting, database, and analytics services, or when required by law.</p>

        <h2>Data Security</h2>
        <p>We use reasonable technical and organizational measures to protect your data, but no online service can guarantee absolute security.</p>

        <h2>Children's Privacy</h2>
        <p>The app is intended for students and educational use. If you believe a child has provided personal information without consent, contact us so we can review and remove it where appropriate.</p>

        <h2>Changes to This Policy</h2>
        <p>We may update this policy from time to time. The latest version will always be available at this page.</p>

        <h2>Contact</h2>
        <p>If you have questions about this privacy policy, contact the app owner or developer through the support channel associated with the app release.</p>
      </body>
    </html>
  `);
});

// Account deletion instructions page for Google Play Console
app.get('/delete-account', (req, res) => {
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(`
    <!doctype html>
    <html lang="en">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>Delete Account - Campus Helper</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            max-width: 900px;
            margin: 0 auto;
            padding: 32px 20px;
            color: #1f2937;
            background: #ffffff;
          }
          h1, h2 { color: #111827; }
          p, li { font-size: 16px; }
          ul, ol { padding-left: 20px; }
          .updated { color: #6b7280; font-size: 14px; }
          .box {
            border: 1px solid #d1d5db;
            border-radius: 8px;
            padding: 12px 14px;
            background: #f9fafb;
          }
        </style>
      </head>
      <body>
        <h1>Campus Helper - Account Deletion</h1>
        <p class="updated">Last updated: ${new Date().toISOString().split('T')[0]}</p>

        <p>This page explains how users of <strong>Campus Helper</strong> can request account deletion and what data is deleted or retained.</p>

        <h2>How to Request Account Deletion</h2>
        <ol>
          <li>Open the Campus Helper app and sign in.</li>
          <li>Go to <strong>Profile</strong>.</li>
          <li>Tap <strong>Delete Account</strong>.</li>
          <li>Enter your current password and confirm deletion.</li>
        </ol>

        <h2>Data Deleted Immediately</h2>
        <ul>
          <li>User account profile (name, email, role).</li>
          <li>Learning progress records linked to the account.</li>
          <li>Test history linked to the account.</li>
          <li>Materials uploaded by that student account.</li>
        </ul>

        <h2>Data Retention</h2>
        <div class="box">
          <p><strong>Authentication and profile data:</strong> deleted immediately after confirmation.</p>
          <p><strong>Server logs and security logs:</strong> may be retained for up to 30 days for fraud prevention, legal compliance, and troubleshooting, then automatically removed.</p>
        </div>

        <h2>Need Help?</h2>
        <p>If you cannot access your account, contact the Campus Helper developer support channel listed on the Google Play listing.</p>
      </body>
    </html>
  `);
});

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/subjects', subjectRoutes);
app.use('/api/materials', materialRoutes);
app.use('/api/tests', testRoutes);
app.use('/api/progress', progressRoutes);
app.use('/api/competitive-exams', competitiveExamRoutes);
app.use('/api/ai', aiRoutes);

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(err.status || 500).json({
    error: err.message || 'Internal server error',
    ...(config.nodeEnv === 'development' && { stack: err.stack })
  });
});

module.exports = app;
