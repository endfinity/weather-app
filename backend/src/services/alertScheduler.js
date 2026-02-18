import config from '../config.js';
import { getDb } from '../db/database.js';
import logger from '../logger.js';

const log = logger.child({ component: 'alertScheduler' });

const SEVERITY_ORDER = Object.freeze({ moderate: 1, severe: 2, extreme: 3 });

let cleanupIntervalId = null;
let lastRunAt = null;
let runCount = 0;
let totalAlertsSent = 0;

/**
 * Check whether an alert of the given type was already sent for a location
 * within the cooldown period.
 * @param {string} alertType
 * @param {string} locationName
 * @returns {boolean}
 */
export function isAlertOnCooldown(alertType, locationName) {
  const db = getDb();
  const cooldownMs = config.alertScheduler.cooldownMs;
  const cutoff = new Date(Date.now() - cooldownMs).toISOString();

  const row = db
    .prepare(
      `SELECT 1 FROM alert_history
       WHERE alert_type = ? AND location_name = ? AND created_at > ? AND status = 'sent'
       LIMIT 1`,
    )
    .get(alertType, locationName, cutoff);

  return !!row;
}

/**
 * Filter alerts based on device preferences (min severity, enabled types, quiet hours).
 * @param {object[]} alerts
 * @param {object | null} preferences - Row from device_alert_preferences
 * @returns {object[]}
 */
export function filterAlertsByPreferences(alerts, preferences) {
  if (!preferences) return alerts;

  const minSeverityLevel = SEVERITY_ORDER[preferences.min_severity] ?? 0;

  let enabledSet = null;
  if (preferences.enabled_types) {
    try {
      const parsed = JSON.parse(preferences.enabled_types);
      if (Array.isArray(parsed) && parsed.length > 0) {
        enabledSet = new Set(parsed);
      }
    } catch {
      // Invalid JSON — treat as no filter
    }
  }

  return alerts.filter((alert) => {
    const severityLevel = SEVERITY_ORDER[alert.severity] ?? 0;
    if (severityLevel < minSeverityLevel) return false;

    if (enabledSet && !enabledSet.has(alert.type)) return false;

    return true;
  });
}

/**
 * Check whether current UTC hour falls within quiet hours.
 * @param {number | null} start - Quiet hours start (0-23), null means no quiet hours
 * @param {number | null} end - Quiet hours end (0-23), null means no quiet hours
 * @param {number} [currentHour] - Override current hour for testing
 * @returns {boolean}
 */
export function isQuietHours(start, end, currentHour) {
  if (start === null || start === undefined || end === null || end === undefined) return false;

  const hour = currentHour ?? new Date().getUTCHours();

  if (start <= end) {
    return hour >= start && hour < end;
  }
  // Wraps midnight (e.g., 22-06)
  return hour >= start || hour < end;
}

/**
 * Get alert preferences for a device. Returns null if none set.
 * @param {number} deviceId
 * @returns {object | null}
 */
export function getDevicePreferences(deviceId) {
  const db = getDb();
  return db.prepare('SELECT * FROM device_alert_preferences WHERE device_id = ?').get(deviceId) ?? null;
}

/**
 * Set or update alert preferences for a device.
 * @param {number} deviceId
 * @param {{ minSeverity?: string, quietHoursStart?: number | null, quietHoursEnd?: number | null, enabledTypes?: string[] | null }} prefs
 */
export function setDevicePreferences(deviceId, prefs) {
  const db = getDb();
  const enabledTypesJson = prefs.enabledTypes ? JSON.stringify(prefs.enabledTypes) : null;

  db.prepare(
    `INSERT INTO device_alert_preferences (device_id, min_severity, quiet_hours_start, quiet_hours_end, enabled_types)
     VALUES (?, ?, ?, ?, ?)
     ON CONFLICT(device_id) DO UPDATE SET
       min_severity = excluded.min_severity,
       quiet_hours_start = excluded.quiet_hours_start,
       quiet_hours_end = excluded.quiet_hours_end,
       enabled_types = excluded.enabled_types,
       updated_at = datetime('now')`,
  ).run(
    deviceId,
    prefs.minSeverity ?? 'moderate',
    prefs.quietHoursStart ?? null,
    prefs.quietHoursEnd ?? null,
    enabledTypesJson,
  );
}

/**
 * Log an alert to the history table.
 * @param {object} alert
 * @param {number} latitude
 * @param {number} longitude
 * @param {'sent' | 'cooldown' | 'filtered' | 'generated'} status
 * @param {number} devicesNotified
 */
export function logAlertHistory(alert, latitude, longitude, status, devicesNotified = 0) {
  const db = getDb();
  db.prepare(
    `INSERT INTO alert_history (alert_type, severity, title, description, location_name, latitude, longitude, status, devices_notified)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
  ).run(
    alert.type,
    alert.severity,
    alert.title,
    alert.description,
    alert.location_name,
    latitude,
    longitude,
    status,
    devicesNotified,
  );
}

/**
 * Query alert history with optional filters.
 * @param {{ limit?: number, offset?: number, severity?: string, status?: string }} options
 * @returns {{ alerts: object[], total: number }}
 */
export function getAlertHistory({ limit = 50, offset = 0, severity, status } = {}) {
  const db = getDb();

  let where = '1=1';
  const params = [];

  if (severity) {
    where += ' AND severity = ?';
    params.push(severity);
  }
  if (status) {
    where += ' AND status = ?';
    params.push(status);
  }

  const total = db.prepare(`SELECT COUNT(*) as count FROM alert_history WHERE ${where}`).get(...params).count;

  const alerts = db
    .prepare(`SELECT * FROM alert_history WHERE ${where} ORDER BY created_at DESC LIMIT ? OFFSET ?`)
    .all(...params, limit, offset);

  return { alerts, total };
}

/**
 * Delete alert history records older than the configured retention period.
 * @returns {number} Number of deleted rows
 */
export function cleanupOldAlerts() {
  const db = getDb();
  const days = config.alertScheduler.historyRetentionDays;
  const cutoff = new Date(Date.now() - days * 86_400_000).toISOString();

  const historyResult = db.prepare('DELETE FROM alert_history WHERE created_at < ?').run(cutoff);
  const sentResult = db.prepare('DELETE FROM sent_alerts WHERE sent_at < ?').run(cutoff);

  const total = historyResult.changes + sentResult.changes;
  if (total > 0) {
    log.info({ deletedHistory: historyResult.changes, deletedSent: sentResult.changes }, 'Cleaned up old alerts');
  }
  return total;
}

/**
 * Start the periodic cleanup job.
 */
export function startCleanupJob() {
  if (cleanupIntervalId) return;
  const intervalMs = config.alertScheduler.cleanupIntervalMs;
  log.info({ intervalHrs: intervalMs / 3_600_000 }, 'Starting alert cleanup job');
  cleanupIntervalId = setInterval(cleanupOldAlerts, intervalMs);
}

/**
 * Stop the periodic cleanup job.
 */
export function stopCleanupJob() {
  if (cleanupIntervalId) {
    clearInterval(cleanupIntervalId);
    cleanupIntervalId = null;
    log.info('Alert cleanup job stopped');
  }
}

/**
 * Record that a scheduler run completed.
 * @param {number} alertsSent
 */
export function recordRun(alertsSent) {
  lastRunAt = new Date().toISOString();
  runCount++;
  totalAlertsSent += alertsSent;
}

/**
 * Get current scheduler status.
 * @returns {object}
 */
export function getSchedulerStatus() {
  return {
    running: cleanupIntervalId !== null,
    lastRunAt,
    runCount,
    totalAlertsSent,
    config: {
      checkIntervalMs: config.weatherMonitor.intervalMs,
      cooldownMs: config.alertScheduler.cooldownMs,
      historyRetentionDays: config.alertScheduler.historyRetentionDays,
    },
  };
}

/**
 * Reset scheduler counters (used in tests).
 */
export function resetSchedulerState() {
  lastRunAt = null;
  runCount = 0;
  totalAlertsSent = 0;
}
