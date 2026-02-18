import { getDb } from '../db/database.js';
import { AppError } from '../errors.js';

/**
 * Premium auth middleware.
 * Checks for a valid purchase token in the Authorization header.
 * Purchase tokens are stored in the premium_users table after Google Play purchase verification.
 *
 * Flow:
 * 1. Android app makes purchase via Google Play Billing
 * 2. App sends purchase token to POST /api/premium/verify
 * 3. Backend verifies with Google Play Developer API (or stores trusted token)
 * 4. Backend stores device_id + purchase_token in premium_users table
 * 5. Subsequent premium API calls include Authorization: Bearer <purchase_token>
 */
/**
 * Express middleware that verifies a premium subscription via the Authorization header.
 * Attaches the premium user record to req.premiumUser on success.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 * @throws {AppError} UNAUTHORIZED when the header is missing or empty
 * @throws {AppError} FORBIDDEN when no active subscription matches the token
 */
export function requirePremium(req, res, next) {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new AppError('UNAUTHORIZED', 'Premium subscription required', 401);
  }

  const token = authHeader.slice(7);

  if (!token) {
    throw new AppError('UNAUTHORIZED', 'Invalid authorization token', 401);
  }

  const db = getDb();
  const stmt = db.prepare(
    'SELECT id, device_id, product_id, purchase_time FROM premium_users WHERE purchase_token = ? AND active = 1',
  );
  const user = stmt.get(token);

  if (!user) {
    throw new AppError('FORBIDDEN', 'No active premium subscription found', 403);
  }

  req.premiumUser = user;
  next();
}

/**
 * No-op — premium_users table is now created via database migrations.
 * @deprecated Use migrateDb() instead.
 */
export function initPremiumTable() {
  // Table creation moved to src/db/migrations/002_premium_users.js
}

export default { requirePremium, initPremiumTable };
