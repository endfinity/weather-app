import crypto from 'node:crypto';
import { getDb } from '../db/database.js';
import config from '../config.js';

const VALID_TIERS = ['standard', 'premium'];

/**
 * Get the rate limit for a given tier.
 * @param {string} tier - 'anonymous', 'standard', or 'premium'
 * @returns {number}
 */
export function getLimitForTier(tier) {
  switch (tier) {
    case 'premium':
      return config.rateLimit.premiumMax;
    case 'standard':
      return config.rateLimit.standardMax;
    default:
      return config.rateLimit.maxRequests;
  }
}

/**
 * Generate a new API key string (prefix + random hex).
 * @param {string} tier
 * @returns {string}
 */
function generateKey(tier) {
  const prefix = tier === 'premium' ? 'csk_prm_' : 'csk_std_';
  return prefix + crypto.randomBytes(24).toString('hex');
}

/**
 * Create a new API key.
 * @param {string} name - Descriptive name for the key
 * @param {string} [tier='standard'] - 'standard' or 'premium'
 * @returns {{ id: number, key: string, name: string, tier: string }}
 */
export function createApiKey(name, tier = 'standard') {
  if (!VALID_TIERS.includes(tier)) {
    throw new Error(`Invalid tier: ${tier}. Must be one of: ${VALID_TIERS.join(', ')}`);
  }

  const db = getDb();
  const key = generateKey(tier);

  const result = db
    .prepare('INSERT INTO api_keys (key, name, tier) VALUES (?, ?, ?) RETURNING id, key, name, tier')
    .get(key, name, tier);

  return result;
}

/**
 * Look up an API key and return its record if active.
 * Updates last_used_at timestamp.
 * @param {string} key
 * @returns {{ id: number, key: string, name: string, tier: string } | null}
 */
export function validateApiKey(key) {
  if (!key) return null;

  const db = getDb();
  const row = db.prepare('SELECT id, key, name, tier FROM api_keys WHERE key = ? AND active = 1').get(key);

  if (row) {
    db.prepare("UPDATE api_keys SET last_used_at = datetime('now') WHERE id = ?").run(row.id);
  }

  return row ?? null;
}

/**
 * List all API keys (optionally filtered by tier).
 * @param {{ tier?: string, includeInactive?: boolean }} [options]
 * @returns {object[]}
 */
export function listApiKeys({ tier, includeInactive = false } = {}) {
  const db = getDb();

  let where = includeInactive ? '1=1' : 'active = 1';
  const params = [];

  if (tier) {
    where += ' AND tier = ?';
    params.push(tier);
  }

  return db
    .prepare(
      `SELECT id, key, name, tier, active, created_at, last_used_at FROM api_keys WHERE ${where} ORDER BY created_at DESC`,
    )
    .all(...params);
}

/**
 * Revoke (deactivate) an API key.
 * @param {number} id
 * @returns {boolean} true if the key was found and revoked
 */
export function revokeApiKey(id) {
  const db = getDb();
  const result = db.prepare('UPDATE api_keys SET active = 0 WHERE id = ? AND active = 1').run(id);
  return result.changes > 0;
}
