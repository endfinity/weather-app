import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import Database from 'better-sqlite3';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { mkdirSync, unlinkSync, existsSync } from 'fs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const TEST_DB_PATH = join(__dirname, '..', '..', 'data', 'test_scheduler.db');

let db;
let alertScheduler;

async function loadMigration(name) {
  const migPath = join(__dirname, '..', '..', 'src', 'db', 'migrations', name);
  const mod = await import(`file://${migPath.replace(/\\/g, '/')}`);
  mod.up(db);
}

describe('alertScheduler', () => {
  before(async () => {
    mkdirSync(dirname(TEST_DB_PATH), { recursive: true });
    db = new Database(TEST_DB_PATH);
    db.pragma('journal_mode = WAL');
    db.pragma('foreign_keys = ON');

    await loadMigration('001_initial_schema.js');
    await loadMigration('003_alert_scheduler.js');

    // Mock getDb to return our test database
    alertScheduler = await import('../../src/services/alertScheduler.js');
  });

  after(() => {
    if (db) db.close();
    if (existsSync(TEST_DB_PATH)) {
      try {
        unlinkSync(TEST_DB_PATH);
      } catch {
        // Might be locked
      }
    }
  });

  describe('filterAlertsByPreferences', () => {
    const alerts = [
      { type: 'extreme_heat', severity: 'extreme', title: 'Extreme Heat' },
      { type: 'high_wind', severity: 'severe', title: 'High Wind' },
      { type: 'fog', severity: 'moderate', title: 'Fog' },
      { type: 'heavy_precip', severity: 'moderate', title: 'Precipitation' },
    ];

    it('returns all alerts when preferences are null', () => {
      const result = alertScheduler.filterAlertsByPreferences(alerts, null);
      assert.strictEqual(result.length, 4);
    });

    it('filters by min severity — severe', () => {
      const prefs = { min_severity: 'severe', enabled_types: null };
      const result = alertScheduler.filterAlertsByPreferences(alerts, prefs);
      assert.strictEqual(result.length, 2);
      assert.ok(result.every((a) => a.severity === 'severe' || a.severity === 'extreme'));
    });

    it('filters by min severity — extreme', () => {
      const prefs = { min_severity: 'extreme', enabled_types: null };
      const result = alertScheduler.filterAlertsByPreferences(alerts, prefs);
      assert.strictEqual(result.length, 1);
      assert.strictEqual(result[0].type, 'extreme_heat');
    });

    it('filters by enabled types', () => {
      const prefs = {
        min_severity: 'moderate',
        enabled_types: JSON.stringify(['extreme_heat', 'fog']),
      };
      const result = alertScheduler.filterAlertsByPreferences(alerts, prefs);
      assert.strictEqual(result.length, 2);
      assert.ok(result.some((a) => a.type === 'extreme_heat'));
      assert.ok(result.some((a) => a.type === 'fog'));
    });

    it('combines severity and type filters', () => {
      const prefs = {
        min_severity: 'severe',
        enabled_types: JSON.stringify(['extreme_heat', 'fog']),
      };
      const result = alertScheduler.filterAlertsByPreferences(alerts, prefs);
      assert.strictEqual(result.length, 1);
      assert.strictEqual(result[0].type, 'extreme_heat');
    });

    it('handles invalid JSON in enabled_types gracefully', () => {
      const prefs = { min_severity: 'moderate', enabled_types: 'not-json' };
      const result = alertScheduler.filterAlertsByPreferences(alerts, prefs);
      assert.strictEqual(result.length, 4);
    });

    it('handles empty enabled_types array', () => {
      const prefs = { min_severity: 'moderate', enabled_types: JSON.stringify([]) };
      const result = alertScheduler.filterAlertsByPreferences(alerts, prefs);
      assert.strictEqual(result.length, 4);
    });
  });

  describe('isQuietHours', () => {
    it('returns false when start/end are null', () => {
      assert.strictEqual(alertScheduler.isQuietHours(null, null), false);
      assert.strictEqual(alertScheduler.isQuietHours(null, 6), false);
      assert.strictEqual(alertScheduler.isQuietHours(22, null), false);
    });

    it('detects quiet hours in same-day range', () => {
      assert.strictEqual(alertScheduler.isQuietHours(8, 17, 10), true);
      assert.strictEqual(alertScheduler.isQuietHours(8, 17, 8), true);
      assert.strictEqual(alertScheduler.isQuietHours(8, 17, 17), false);
      assert.strictEqual(alertScheduler.isQuietHours(8, 17, 7), false);
      assert.strictEqual(alertScheduler.isQuietHours(8, 17, 20), false);
    });

    it('detects quiet hours wrapping midnight', () => {
      assert.strictEqual(alertScheduler.isQuietHours(22, 6, 23), true);
      assert.strictEqual(alertScheduler.isQuietHours(22, 6, 0), true);
      assert.strictEqual(alertScheduler.isQuietHours(22, 6, 3), true);
      assert.strictEqual(alertScheduler.isQuietHours(22, 6, 5), true);
      assert.strictEqual(alertScheduler.isQuietHours(22, 6, 6), false);
      assert.strictEqual(alertScheduler.isQuietHours(22, 6, 12), false);
      assert.strictEqual(alertScheduler.isQuietHours(22, 6, 21), false);
    });

    it('handles undefined values', () => {
      assert.strictEqual(alertScheduler.isQuietHours(undefined, undefined), false);
    });
  });

  describe('getSchedulerStatus', () => {
    it('returns status object with expected fields', () => {
      alertScheduler.resetSchedulerState();
      const status = alertScheduler.getSchedulerStatus();
      assert.strictEqual(status.lastRunAt, null);
      assert.strictEqual(status.runCount, 0);
      assert.strictEqual(status.totalAlertsSent, 0);
      assert.ok(status.config);
      assert.ok(status.config.checkIntervalMs > 0);
      assert.ok(status.config.cooldownMs > 0);
      assert.ok(status.config.historyRetentionDays > 0);
    });

    it('tracks run count and alerts sent', () => {
      alertScheduler.resetSchedulerState();
      alertScheduler.recordRun(5);
      alertScheduler.recordRun(3);
      const status = alertScheduler.getSchedulerStatus();
      assert.strictEqual(status.runCount, 2);
      assert.strictEqual(status.totalAlertsSent, 8);
      assert.ok(status.lastRunAt);
    });
  });
});
