/**
 * Initial schema — devices, device_locations, sent_alerts.
 * @param {import('better-sqlite3').Database} db
 */
export function up(db) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS devices (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      fcm_token TEXT UNIQUE NOT NULL,
      platform TEXT DEFAULT 'android',
      created_at TEXT DEFAULT (datetime('now')),
      updated_at TEXT DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS device_locations (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      device_id INTEGER NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      name TEXT NOT NULL,
      FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS sent_alerts (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      alert_id TEXT NOT NULL,
      device_id INTEGER NOT NULL,
      sent_at TEXT DEFAULT (datetime('now')),
      UNIQUE(alert_id, device_id),
      FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
    );

    CREATE INDEX IF NOT EXISTS idx_devices_fcm_token ON devices(fcm_token);
    CREATE INDEX IF NOT EXISTS idx_device_locations_device ON device_locations(device_id);
    CREATE INDEX IF NOT EXISTS idx_sent_alerts_alert ON sent_alerts(alert_id, device_id);
  `);
}
