const mongoose = require('mongoose');
const app = require('./app');
const config = require('./config');

// Database connection
// Connect to MongoDB (no deprecated options passed to mongoose.connect)
mongoose.connect(config.mongoUri)
  .then(() => {
    console.log('✅ MongoDB connected successfully');

    // Start server with robust error handling for EADDRINUSE
    const startServer = (port, tryNext = true) => {
      const server = app.listen(port, () => {
        console.log(`🚀 Server running on port ${port} in ${config.nodeEnv} mode`);
      });

      server.on('error', (err) => {
        if (err && err.code === 'EADDRINUSE') {
          console.error(`Port ${port} is already in use.`);
          if (tryNext) {
            const nextPort = Number(port) + 1;
            console.log(`Attempting to start on port ${nextPort}...`);
            // try once on the next port
            startServer(nextPort, false);
            return;
          }
          console.error('Failed to start server due to port conflict.\nYou can either stop the process using the port or set PORT env var to a free port.');
          console.error('To find the process using the port (macOS / Linux): lsof -i :<port>');
          console.error('To kill it: kill -9 <pid>');
          process.exit(1);
        } else {
          console.error('Server error:', err);
          process.exit(1);
        }
      });
    };

    const port = process.env.PORT || config.port || 5000;
    startServer(port);
  })
  .catch(err => {
    console.error('❌ MongoDB connection error:', err);
    process.exit(1);
  });

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  console.error('Unhandled Rejection:', err);
  process.exit(1);
});
