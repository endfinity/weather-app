import { z } from 'zod';

const httpUrl = z
  .string()
  .url()
  .refine((u) => /^https?:/.test(u), 'Must be an HTTP(S) URL');

const positiveInt = z.coerce.number().int().positive();

/**
 * Zod schema that validates all environment variables consumed by the app.
 * Defaults mirror the previous hard-coded values so the app starts without a .env file.
 */
export const envSchema = z.object({
  PORT: positiveInt.default(3000),
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
  LOG_LEVEL: z.enum(['fatal', 'error', 'warn', 'info', 'debug', 'trace', 'silent']).default('info'),

  TRUST_PROXY: z.stringbool().prefault('false'),
  BODY_LIMIT: z.string().default('1mb'),
  SHUTDOWN_TIMEOUT_MS: positiveInt.default(30_000),
  REQUEST_TIMEOUT_MS: z.coerce.number().int().min(0).default(30_000),

  CORS_ALLOWED_ORIGINS: z.string().default('*'),

  OPEN_METEO_BASE_URL: httpUrl.default('https://api.open-meteo.com/v1'),
  OPEN_METEO_AQ_BASE_URL: httpUrl.default('https://air-quality-api.open-meteo.com/v1'),
  OPEN_METEO_GEO_BASE_URL: httpUrl.default('https://geocoding-api.open-meteo.com/v1'),
  OPEN_METEO_ARCHIVE_BASE_URL: httpUrl.default('https://archive-api.open-meteo.com/v1'),

  CACHE_TTL_SECONDS: positiveInt.default(900),

  RATE_LIMIT_WINDOW_MS: positiveInt.default(60_000),
  RATE_LIMIT_MAX_REQUESTS: positiveInt.default(60),
  RATE_LIMIT_STANDARD_MAX: positiveInt.default(120),
  RATE_LIMIT_PREMIUM_MAX: positiveInt.default(300),

  FIREBASE_SERVICE_ACCOUNT_PATH: z.string().default('./firebase-service-account.json'),

  WEATHER_MONITOR_INTERVAL_MS: positiveInt.default(900_000),

  ALERT_COOLDOWN_MS: positiveInt.default(14_400_000),
  ALERT_CLEANUP_INTERVAL_MS: positiveInt.default(86_400_000),
  ALERT_HISTORY_RETENTION_DAYS: positiveInt.default(30),

  BATCH_MAX_LOCATIONS: z.coerce.number().int().min(1).max(100).default(10),
});

/**
 * Parse and validate `process.env`. Throws a descriptive error on failure.
 * @param {Record<string, string | undefined>} env - Typically `process.env`
 * @returns {z.infer<typeof envSchema>}
 */
export function parseEnv(env) {
  const result = envSchema.safeParse(env);
  if (!result.success) {
    const details = result.error.issues.map((i) => `  • ${i.path.join('.')}: ${i.message}`).join('\n');
    throw new Error(`Invalid environment configuration:\n${details}`);
  }
  return result.data;
}
