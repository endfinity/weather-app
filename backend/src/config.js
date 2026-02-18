import 'dotenv/config';
import { parseEnv } from './envSchema.js';

const env = parseEnv(process.env);

const config = {
  port: env.PORT,
  nodeEnv: env.NODE_ENV,

  server: {
    trustProxy: env.TRUST_PROXY,
    bodyLimit: env.BODY_LIMIT,
    shutdownTimeoutMs: env.SHUTDOWN_TIMEOUT_MS,
    requestTimeoutMs: env.REQUEST_TIMEOUT_MS,
  },

  cors: {
    allowedOrigins: env.CORS_ALLOWED_ORIGINS.split(',').map((o) => o.trim()),
  },

  openMeteo: {
    baseUrl: env.OPEN_METEO_BASE_URL,
    aqBaseUrl: env.OPEN_METEO_AQ_BASE_URL,
    geoBaseUrl: env.OPEN_METEO_GEO_BASE_URL,
    archiveBaseUrl: env.OPEN_METEO_ARCHIVE_BASE_URL,
  },

  cache: {
    ttlSeconds: env.CACHE_TTL_SECONDS,
  },

  rateLimit: {
    windowMs: env.RATE_LIMIT_WINDOW_MS,
    maxRequests: env.RATE_LIMIT_MAX_REQUESTS,
    standardMax: env.RATE_LIMIT_STANDARD_MAX,
    premiumMax: env.RATE_LIMIT_PREMIUM_MAX,
  },

  firebase: {
    serviceAccountPath: env.FIREBASE_SERVICE_ACCOUNT_PATH,
  },

  weatherMonitor: {
    intervalMs: env.WEATHER_MONITOR_INTERVAL_MS,
  },

  alertScheduler: {
    cooldownMs: env.ALERT_COOLDOWN_MS,
    cleanupIntervalMs: env.ALERT_CLEANUP_INTERVAL_MS,
    historyRetentionDays: env.ALERT_HISTORY_RETENTION_DAYS,
  },

  batch: {
    maxLocations: env.BATCH_MAX_LOCATIONS,
  },

  logLevel: env.LOG_LEVEL,
};

export default config;
