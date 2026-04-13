require('dotenv').config();
const axios = require('axios');

const url = process.env.KEEP_ALIVE_URL;
const intervalMinutes = Number(process.env.KEEP_ALIVE_INTERVAL_MINUTES || 5);
const timeoutMs = Number(process.env.KEEP_ALIVE_TIMEOUT_MS || 15000);

if (!url) {
  console.error('Missing KEEP_ALIVE_URL in environment variables.');
  process.exit(1);
}

if (!Number.isFinite(intervalMinutes) || intervalMinutes <= 0) {
  console.error('KEEP_ALIVE_INTERVAL_MINUTES must be a positive number.');
  process.exit(1);
}

const intervalMs = intervalMinutes * 60 * 1000;

async function pingBackend() {
  const startedAt = Date.now();

  try {
    const response = await axios.get(url, {
      timeout: timeoutMs,
      validateStatus: () => true
    });

    const duration = Date.now() - startedAt;
    console.log(`[${new Date().toISOString()}] Ping ${url} -> ${response.status} (${duration}ms)`);
  } catch (error) {
    const duration = Date.now() - startedAt;
    const message = error && error.message ? error.message : 'Unknown error';
    console.error(`[${new Date().toISOString()}] Ping failed after ${duration}ms: ${message}`);
  }
}

console.log(`Keep-alive started. URL: ${url}`);
console.log(`Interval: ${intervalMinutes} minute(s), Timeout: ${timeoutMs}ms`);

pingBackend();
setInterval(pingBackend, intervalMs);
