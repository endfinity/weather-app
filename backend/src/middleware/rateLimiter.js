import config from '../config.js';
import { validateApiKey, getLimitForTier } from '../services/apiKeyService.js';

const clients = new Map();

function cleanupStaleEntries() {
  const now = Date.now();
  for (const [key, entry] of clients) {
    if (now - entry.windowStart > config.rateLimit.windowMs * 2) {
      clients.delete(key);
    }
  }
}

setInterval(cleanupStaleEntries, 60_000).unref();

/**
 * Extract API key from request headers or query parameter.
 * Supports: X-API-Key header, ?api_key query param.
 * @param {import('express').Request} req
 * @returns {string | null}
 */
function extractApiKey(req) {
  return req.headers['x-api-key'] || req.query.api_key || null;
}

/**
 * Express middleware that enforces tiered rate limiting.
 * Anonymous requests are limited by IP. Authenticated requests (via API key)
 * get higher limits based on their tier (standard or premium).
 * Sets X-RateLimit-* headers and returns 429 when the limit is exceeded.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
export default function rateLimiter(req, res, next) {
  const rawKey = extractApiKey(req);
  const apiKeyRecord = rawKey ? validateApiKey(rawKey) : null;

  const tier = apiKeyRecord?.tier ?? 'anonymous';
  const limit = getLimitForTier(tier);
  const clientId = apiKeyRecord ? `key:${apiKeyRecord.id}` : `ip:${req.ip || req.socket.remoteAddress}`;

  req.rateLimitTier = tier;
  if (apiKeyRecord) {
    req.apiKey = apiKeyRecord;
  }

  const now = Date.now();
  let entry = clients.get(clientId);

  if (!entry || now - entry.windowStart > config.rateLimit.windowMs) {
    entry = { windowStart: now, count: 0 };
    clients.set(clientId, entry);
  }

  entry.count++;

  const remaining = Math.max(0, limit - entry.count);
  const resetAt = entry.windowStart + config.rateLimit.windowMs;

  res.set('X-RateLimit-Limit', String(limit));
  res.set('X-RateLimit-Remaining', String(remaining));
  res.set('X-RateLimit-Reset', String(Math.ceil(resetAt / 1000)));
  if (tier !== 'anonymous') {
    res.set('X-RateLimit-Tier', tier);
  }

  if (entry.count > limit) {
    const retryAfter = Math.ceil((resetAt - now) / 1000);
    res.set('Retry-After', String(retryAfter));
    return res.status(429).json({
      success: false,
      error: {
        code: 'RATE_LIMITED',
        message: `Too many requests. Try again in ${retryAfter} seconds.`,
      },
    });
  }

  next();
}

/**
 * Reset all rate limiter state (used in tests).
 */
export function resetRateLimiter() {
  clients.clear();
}
