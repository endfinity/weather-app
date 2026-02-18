import { getFirebaseMessaging, isFirebaseReady } from './firebaseService.js';
import { getDb } from '../db/database.js';
import logger from '../logger.js';

const log = logger.child({ component: 'notification' });

/**
 * Send a single push notification for an alert to a device via FCM.
 * Skips if already sent or Firebase is not ready. Removes stale device tokens.
 * @param {{ id: string, fcm_token: string }} device
 * @param {{ id: string, title: string, description: string, severity: string, type: string, location_name?: string }} alert
 * @returns {Promise<boolean>} true if the notification was sent
 */
export async function sendAlertNotification(device, alert) {
  if (!isFirebaseReady()) {
    log.warn({ alertId: alert.id }, 'Firebase not ready, skipping push');
    return false;
  }

  const db = getDb();

  const alreadySent = db
    .prepare('SELECT 1 FROM sent_alerts WHERE alert_id = ? AND device_id = ?')
    .get(alert.id, device.id);

  if (alreadySent) return false;

  const channelId = mapSeverityToChannel(alert.severity);

  const message = {
    token: device.fcm_token,
    data: {
      alertId: alert.id,
      title: alert.title,
      body: alert.description,
      severity: alert.severity,
      channel: channelId,
      type: alert.type,
      locationName: alert.location_name || '',
    },
    android: {
      priority: alert.severity === 'extreme' || alert.severity === 'severe' ? 'high' : 'normal',
      ttl: 3600000,
    },
  };

  try {
    const messaging = getFirebaseMessaging();
    await messaging.send(message);

    db.prepare('INSERT OR IGNORE INTO sent_alerts (alert_id, device_id) VALUES (?, ?)').run(alert.id, device.id);

    log.info({ alertId: alert.id, deviceId: device.id }, 'Sent alert');
    return true;
  } catch (err) {
    if (
      err.code === 'messaging/registration-token-not-registered' ||
      err.code === 'messaging/invalid-registration-token'
    ) {
      log.warn({ deviceId: device.id }, 'Invalid token — removing device');
      db.prepare('DELETE FROM devices WHERE id = ?').run(device.id);
      return false;
    }
    log.error({ err, alertId: alert.id, deviceId: device.id }, 'Failed to send');
    return false;
  }
}

/**
 * Send alert notifications to multiple devices, deduplicating already-sent alerts.
 * @param {{ id: string, fcm_token: string }[]} devices
 * @param {object[]} alerts
 * @returns {Promise<number>} Count of successfully sent notifications
 */
export async function sendBatchAlerts(devices, alerts) {
  let sent = 0;
  for (const device of devices) {
    for (const alert of alerts) {
      const didSend = await sendAlertNotification(device, alert);
      if (didSend) sent++;
    }
  }
  return sent;
}

function mapSeverityToChannel(severity) {
  switch (severity) {
    case 'extreme':
    case 'severe':
      return 'severe_alerts';
    case 'moderate':
      return 'precipitation_alerts';
    default:
      return 'daily_summary';
  }
}
