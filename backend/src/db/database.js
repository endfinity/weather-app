import Database from 'better-sqlite3';
import { mkdirSync } from 'fs';
import { dirname, join } from 'path';
import { fileURLToPath } from 'url';
import { runMigrations } from './migrator.js';
import logger from '../logger.js';

const __dirname = dirname(fileURLToPath(import.meta.url));
const DB_PATH = join(__dirname, '..', '..', 'data', 'clearsky.db');

let db;
let migrated = false;

/**
 * Get (or lazily create) the singleton SQLite database connection.
 * @returns {import('better-sqlite3').Database}
 */
export function getDb() {
  if (!db) {
    mkdirSync(dirname(DB_PATH), { recursive: true });
    db = new Database(DB_PATH);
    db.pragma('journal_mode = WAL');
    db.pragma('foreign_keys = ON');
  }
  return db;
}

/**
 * Run pending database migrations. Safe to call multiple times.
 */
export async function migrateDb() {
  if (migrated) return;
  const conn = getDb();
  await runMigrations(conn);
  migrated = true;
  logger.info('Database migrations complete');
}

/**
 * Check that the database connection is alive.
 * @returns {{ ok: boolean, responseMs: number }}
 */
export function pingDb() {
  const start = Date.now();
  try {
    const conn = getDb();
    conn.prepare('SELECT 1').get();
    return { ok: true, responseMs: Date.now() - start };
  } catch {
    return { ok: false, responseMs: Date.now() - start };
  }
}

/**
 * Close the database connection if open.
 */
export function closeDb() {
  if (db) {
    db.close();
    db = null;
    migrated = false;
  }
}
