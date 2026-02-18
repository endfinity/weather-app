import { Router } from 'express';
import { getDb } from '../db/database.js';
import { AppError } from '../errors.js';
import { validateFcmToken } from '../validation.js';
import { getDevicePreferences, setDevicePreferences } from '../services/alertScheduler.js';

const router = Router();

/**
 * @openapi
 * /api/devices/register:
 *   post:
 *     tags: [Devices]
 *     summary: Register a device for push notifications
 *     description: Registers or updates a device with its FCM token and monitored locations.
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [fcm_token]
 *             properties:
 *               fcm_token:
 *                 type: string
 *                 description: Firebase Cloud Messaging token
 *               platform:
 *                 type: string
 *                 default: android
 *               locations:
 *                 type: array
 *                 items:
 *                   $ref: '#/components/schemas/Location'
 *     responses:
 *       200:
 *         description: Device registered
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 data:
 *                   type: object
 *                   properties:
 *                     id:
 *                       type: integer
 *                     message:
 *                       type: string
 *       400:
 *         description: Missing fcm_token
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.post('/register', (req, res) => {
  const { fcm_token, platform = 'android', locations = [] } = req.body;
  validateFcmToken(req.body);

  const db = getDb();

  const upsertDevice = db.prepare(
    "INSERT INTO devices (fcm_token, platform) VALUES (?, ?) ON CONFLICT(fcm_token) DO UPDATE SET platform = excluded.platform, updated_at = datetime('now') RETURNING id",
  );

  const insertLocation = db.prepare(
    'INSERT INTO device_locations (device_id, latitude, longitude, name) VALUES (?, ?, ?, ?)',
  );

  const deleteLocations = db.prepare('DELETE FROM device_locations WHERE device_id = ?');

  const result = db.transaction(() => {
    const row = upsertDevice.get(fcm_token, platform);
    const deviceId = row.id;

    deleteLocations.run(deviceId);

    for (const loc of locations) {
      if (loc.lat !== null && loc.lat !== undefined && loc.lon !== null && loc.lon !== undefined && loc.name) {
        insertLocation.run(deviceId, loc.lat, loc.lon, loc.name);
      }
    }

    return deviceId;
  })();

  res.json({
    success: true,
    data: { id: result, message: 'Device registered successfully' },
  });
});

/**
 * @openapi
 * /api/devices/{token}:
 *   put:
 *     tags: [Devices]
 *     summary: Update device locations
 *     description: Replaces the monitored locations for a registered device.
 *     parameters:
 *       - name: token
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *         description: FCM token of the device
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               locations:
 *                 type: array
 *                 items:
 *                   $ref: '#/components/schemas/Location'
 *     responses:
 *       200:
 *         description: Locations updated
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 data:
 *                   type: object
 *                   properties:
 *                     message:
 *                       type: string
 *       404:
 *         description: Device not found
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.put('/:token', (req, res) => {
  const { token } = req.params;
  const { locations = [] } = req.body;

  const db = getDb();

  const device = db.prepare('SELECT id FROM devices WHERE fcm_token = ?').get(token);
  if (!device) {
    throw new AppError('NOT_FOUND', 'Device not found', 404);
  }

  const insertLocation = db.prepare(
    'INSERT INTO device_locations (device_id, latitude, longitude, name) VALUES (?, ?, ?, ?)',
  );

  const deleteLocations = db.prepare('DELETE FROM device_locations WHERE device_id = ?');

  db.transaction(() => {
    deleteLocations.run(device.id);
    for (const loc of locations) {
      if (loc.lat !== null && loc.lat !== undefined && loc.lon !== null && loc.lon !== undefined && loc.name) {
        insertLocation.run(device.id, loc.lat, loc.lon, loc.name);
      }
    }
    db.prepare("UPDATE devices SET updated_at = datetime('now') WHERE id = ?").run(device.id);
  })();

  res.json({
    success: true,
    data: { message: 'Device locations updated' },
  });
});

/**
 * @openapi
 * /api/devices/{token}:
 *   delete:
 *     tags: [Devices]
 *     summary: Unregister a device
 *     description: Removes a device and its monitored locations.
 *     parameters:
 *       - name: token
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *         description: FCM token of the device
 *     responses:
 *       200:
 *         description: Device unregistered
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 data:
 *                   type: object
 *                   properties:
 *                     message:
 *                       type: string
 *       404:
 *         description: Device not found
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.delete('/:token', (req, res) => {
  const { token } = req.params;
  const db = getDb();

  const result = db.prepare('DELETE FROM devices WHERE fcm_token = ?').run(token);

  if (result.changes === 0) {
    throw new AppError('NOT_FOUND', 'Device not found', 404);
  }

  res.json({
    success: true,
    data: { message: 'Device unregistered' },
  });
});

/**
 * @openapi
 * /api/devices/{token}/preferences:
 *   get:
 *     tags: [Devices]
 *     summary: Get device alert preferences
 *     description: Returns the alert notification preferences for a device.
 *     parameters:
 *       - name: token
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Device preferences
 *       404:
 *         description: Device not found
 */
router.get('/:token/preferences', (req, res) => {
  const { token } = req.params;
  const db = getDb();

  const device = db.prepare('SELECT id FROM devices WHERE fcm_token = ?').get(token);
  if (!device) {
    throw new AppError('NOT_FOUND', 'Device not found', 404);
  }

  const prefs = getDevicePreferences(device.id);

  res.json({
    success: true,
    data: {
      preferences: prefs
        ? {
            minSeverity: prefs.min_severity,
            quietHoursStart: prefs.quiet_hours_start,
            quietHoursEnd: prefs.quiet_hours_end,
            enabledTypes: prefs.enabled_types ? JSON.parse(prefs.enabled_types) : null,
          }
        : null,
    },
  });
});

/**
 * @openapi
 * /api/devices/{token}/preferences:
 *   put:
 *     tags: [Devices]
 *     summary: Update device alert preferences
 *     description: Sets or updates alert notification preferences for a device.
 *     parameters:
 *       - name: token
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               minSeverity:
 *                 type: string
 *                 enum: [moderate, severe, extreme]
 *               quietHoursStart:
 *                 type: integer
 *                 minimum: 0
 *                 maximum: 23
 *               quietHoursEnd:
 *                 type: integer
 *                 minimum: 0
 *                 maximum: 23
 *               enabledTypes:
 *                 type: array
 *                 items:
 *                   type: string
 *     responses:
 *       200:
 *         description: Preferences updated
 *       404:
 *         description: Device not found
 */
router.put('/:token/preferences', (req, res) => {
  const { token } = req.params;
  const { minSeverity, quietHoursStart, quietHoursEnd, enabledTypes } = req.body;
  const db = getDb();

  const device = db.prepare('SELECT id FROM devices WHERE fcm_token = ?').get(token);
  if (!device) {
    throw new AppError('NOT_FOUND', 'Device not found', 404);
  }

  setDevicePreferences(device.id, {
    minSeverity: minSeverity || 'moderate',
    quietHoursStart: quietHoursStart ?? null,
    quietHoursEnd: quietHoursEnd ?? null,
    enabledTypes: enabledTypes || null,
  });

  res.json({
    success: true,
    data: { message: 'Alert preferences updated' },
  });
});

export default router;
