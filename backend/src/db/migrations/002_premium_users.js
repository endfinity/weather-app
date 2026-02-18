/**
 * Premium users table for in-app purchase verification.
 * @param {import('better-sqlite3').Database} db
 */
export function up(db) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS premium_users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      device_id TEXT NOT NULL,
      purchase_token TEXT NOT NULL UNIQUE,
      product_id TEXT NOT NULL DEFAULT 'clearsky_premium',
      purchase_time INTEGER NOT NULL,
      active INTEGER NOT NULL DEFAULT 1,
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    )
  `);
}
