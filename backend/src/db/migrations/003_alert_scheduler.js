/**
 * Alert scheduler tables — alert_history and device_alert_preferences.
 * @param {import('better-sqlite3').Database} db
 */
export function up(db) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS alert_history (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      alert_type TEXT NOT NULL,
      severity TEXT NOT NULL,
      title TEXT NOT NULL,
      description TEXT NOT NULL,
      location_name TEXT NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      status TEXT NOT NULL DEFAULT 'generated',
      devices_notified INTEGER NOT NULL DEFAULT 0,
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE INDEX IF NOT EXISTS idx_alert_history_type_location
      ON alert_history(alert_type, location_name);
    CREATE INDEX IF NOT EXISTS idx_alert_history_created
      ON alert_history(created_at);

    CREATE TABLE IF NOT EXISTS device_alert_preferences (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      device_id INTEGER NOT NULL UNIQUE,
      min_severity TEXT NOT NULL DEFAULT 'moderate',
      quiet_hours_start INTEGER,
      quiet_hours_end INTEGER,
      enabled_types TEXT,
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      updated_at TEXT NOT NULL DEFAULT (datetime('now')),
      FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
    );

    CREATE INDEX IF NOT EXISTS idx_device_alert_prefs_device
      ON device_alert_preferences(device_id);
  `);
}
