import config from '../config.js';
import { getDb } from '../db/database.js';
import { fetchForecast, fetchAirQuality } from '../services/openMeteoService.js';
import { analyzeWeatherForAlerts, analyzeAirQualityForAlerts } from '../services/alertService.js';
import { sendAlertNotification } from '../services/notificationService.js';
import { isFirebaseReady } from '../services/firebaseService.js';
import {
  isAlertOnCooldown,
  filterAlertsByPreferences,
  isQuietHours,
  getDevicePreferences,
  logAlertHistory,
  recordRun,
  startCleanupJob,
  stopCleanupJob,
} from '../services/alertScheduler.js';
import logger from '../logger.js';

const log = logger.child({ component: 'weatherMonitor' });

let intervalId = null;

/**
 * Start the periodic weather monitoring job that checks registered locations
 * and sends push alerts when conditions exceed thresholds.
 */
export function startWeatherMonitor() {
  if (intervalId) return;

  const intervalMs = config.weatherMonitor.intervalMs;
  log.info({ intervalSec: intervalMs / 1000 }, 'Starting weather monitor');

  startCleanupJob();
  runCheck();

  intervalId = setInterval(runCheck, intervalMs);
}

/**
 * Stop the periodic weather monitoring job.
 */
export function stopWeatherMonitor() {
  if (intervalId) {
    clearInterval(intervalId);
    intervalId = null;
    log.info('Weather monitor stopped');
  }
  stopCleanupJob();
}

/**
 * Run a single weather check across all registered locations.
 * Exported so it can be triggered manually from the scheduler API.
 * @returns {Promise<{ locationsChecked: number, alertsSent: number }>}
 */
export async function runCheck() {
  if (!isFirebaseReady()) return { locationsChecked: 0, alertsSent: 0 };

  let alertsSent = 0;
  let locationsChecked = 0;

  try {
    const db = getDb();

    const locations = db
      .prepare(
        'SELECT DISTINCT dl.latitude, dl.longitude, dl.name FROM device_locations dl INNER JOIN devices d ON dl.device_id = d.id',
      )
      .all();

    if (locations.length === 0) {
      recordRun(0);
      return { locationsChecked: 0, alertsSent: 0 };
    }

    log.info({ count: locations.length }, 'Checking locations');

    for (const loc of locations) {
      try {
        locationsChecked++;

        const [weatherRaw, aqRaw] = await Promise.all([
          fetchForecast(loc.latitude, loc.longitude, 'auto'),
          fetchAirQuality(loc.latitude, loc.longitude),
        ]);

        const weatherAlerts = analyzeWeatherForAlerts(weatherRaw, loc.name);
        const aqAlerts = analyzeAirQualityForAlerts(aqRaw, loc.name);
        const allAlerts = [...weatherAlerts, ...aqAlerts];

        if (allAlerts.length === 0) continue;

        const activeAlerts = allAlerts.filter((alert) => {
          if (isAlertOnCooldown(alert.type, loc.name)) {
            logAlertHistory(alert, loc.latitude, loc.longitude, 'cooldown');
            return false;
          }
          return true;
        });

        if (activeAlerts.length === 0) continue;

        const devices = db
          .prepare(
            'SELECT d.id, d.fcm_token FROM devices d INNER JOIN device_locations dl ON d.id = dl.device_id WHERE dl.latitude = ? AND dl.longitude = ?',
          )
          .all(loc.latitude, loc.longitude);

        if (devices.length === 0) continue;

        for (const device of devices) {
          const prefs = getDevicePreferences(device.id);

          if (prefs && isQuietHours(prefs.quiet_hours_start, prefs.quiet_hours_end)) {
            continue;
          }

          const filtered = filterAlertsByPreferences(activeAlerts, prefs);

          for (const alert of filtered) {
            const didSend = await sendAlertNotification(device, alert);
            if (didSend) {
              alertsSent++;
              logAlertHistory(alert, loc.latitude, loc.longitude, 'sent', 1);
            }
          }
        }
      } catch (locErr) {
        log.error({ err: locErr, location: loc.name }, 'Error checking location');
      }
    }
  } catch (err) {
    log.error({ err }, 'Check failed');
  }

  recordRun(alertsSent);
  if (alertsSent > 0) {
    log.info({ alertsSent, locationsChecked }, 'Check complete');
  }

  return { locationsChecked, alertsSent };
}
