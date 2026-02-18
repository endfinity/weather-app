import { readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath, pathToFileURL } from 'url';
import logger from '../logger.js';

const __dirname = dirname(fileURLToPath(import.meta.url));
const MIGRATIONS_DIR = join(__dirname, 'migrations');

function ensureMigrationsTable(db) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS schema_migrations (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL UNIQUE,
      applied_at TEXT NOT NULL DEFAULT (datetime('now'))
    )
  `);
}

function getAppliedMigrations(db) {
  return new Set(
    db
      .prepare('SELECT name FROM schema_migrations ORDER BY name')
      .all()
      .map((r) => r.name),
  );
}

function getPendingFiles(applied) {
  const files = readdirSync(MIGRATIONS_DIR)
    .filter((f) => f.endsWith('.js'))
    .sort();
  return files.filter((f) => !applied.has(f));
}

/**
 * Run all pending database migrations inside a transaction.
 * @param {import('better-sqlite3').Database} db
 */
export async function runMigrations(db) {
  ensureMigrationsTable(db);

  const applied = getAppliedMigrations(db);
  const pending = getPendingFiles(applied);

  if (pending.length === 0) {
    logger.debug('No pending migrations');
    return;
  }

  logger.info({ count: pending.length }, 'Running database migrations');

  for (const file of pending) {
    const filePath = pathToFileURL(join(MIGRATIONS_DIR, file)).href;
    const mod = await import(filePath);

    db.transaction(() => {
      mod.up(db);
      db.prepare('INSERT INTO schema_migrations (name) VALUES (?)').run(file);
    })();

    logger.info({ migration: file }, 'Applied migration');
  }
}
