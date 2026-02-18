import NodeCache from 'node-cache';
import config from '../config.js';

const cache = new NodeCache({
  stdTTL: config.cache.ttlSeconds,
  checkperiod: Math.floor(config.cache.ttlSeconds / 2),
  useClones: true,
});

/**
 * Retrieve a cached entry by key.
 * @param {string} key
 * @returns {{ data: object, cachedAt: string } | null}
 */
export function getCached(key) {
  const value = cache.get(key);
  if (value === undefined) return null;
  return value;
}

/**
 * Store a value in the cache with an auto-generated cachedAt timestamp.
 * @param {string} key
 * @param {object} value
 * @param {number} [ttl] - Override TTL in seconds
 */
export function setCached(key, value, ttl) {
  cache.set(key, { data: value, cachedAt: new Date().toISOString() }, ttl);
}

/**
 * Build a deterministic cache key from a prefix and sorted parameters.
 * @param {string} prefix
 * @param {Record<string, string | number | null | undefined>} params
 * @returns {string}
 */
export function buildCacheKey(prefix, params) {
  const sorted = Object.entries(params)
    .filter(([, v]) => v !== null && v !== undefined)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([k, v]) => `${k}=${v}`)
    .join('&');
  return `${prefix}:${sorted}`;
}

/**
 * Get cache hit/miss statistics.
 * @returns {{ hits: number, misses: number, keys: number, ksize: number, vsize: number }}
 */
export function getStats() {
  return cache.getStats();
}

/**
 * Flush all cache entries.
 */
export function flushAll() {
  cache.flushAll();
}

export default { getCached, setCached, buildCacheKey, getStats, flushAll };
