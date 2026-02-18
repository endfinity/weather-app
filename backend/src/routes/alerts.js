import { Router } from 'express';
import { fetchForecast, fetchAirQuality } from '../services/openMeteoService.js';
import { analyzeWeatherForAlerts, analyzeAirQualityForAlerts } from '../services/alertService.js';
import { getCached, setCached, buildCacheKey } from '../services/cacheService.js';
import { getSchedulerStatus, getAlertHistory, cleanupOldAlerts } from '../services/alertScheduler.js';
import { runCheck } from '../jobs/weatherMonitor.js';
import { validateCoordinates } from '../validation.js';

const router = Router();

/**
 * @openapi
 * /api/alerts:
 *   get:
 *     tags: [Alerts]
 *     summary: Get weather and air quality alerts
 *     description: Analyzes current weather and air quality data to generate alerts for the given location.
 *     parameters:
 *       - $ref: '#/components/parameters/Latitude'
 *       - $ref: '#/components/parameters/Longitude'
 *       - name: name
 *         in: query
 *         schema:
 *           type: string
 *           example: New York
 *         description: Location name (defaults to coordinates)
 *     responses:
 *       200:
 *         description: List of active alerts
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
 *                     alerts:
 *                       type: array
 *                       items:
 *                         type: object
 *                         properties:
 *                           type:
 *                             type: string
 *                           severity:
 *                             type: string
 *                             enum: [info, warning, danger]
 *                           message:
 *                             type: string
 *       400:
 *         description: Missing or invalid coordinates
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.get('/', async (req, res) => {
  const { lat, lon } = req.query;
  const { latitude, longitude } = validateCoordinates(lat, lon);

  const locationName = req.query.name || `${latitude.toFixed(2)}, ${longitude.toFixed(2)}`;

  const weatherCacheKey = buildCacheKey('weather', { lat: latitude, lon: longitude, timezone: 'auto' });
  const aqCacheKey = buildCacheKey('aq', { lat: latitude, lon: longitude });

  const cachedWeather = getCached(weatherCacheKey);
  const cachedAq = getCached(aqCacheKey);

  const [weatherRaw, aqRaw] = await Promise.all([
    cachedWeather ? Promise.resolve(cachedWeather.data) : fetchForecast(latitude, longitude, 'auto'),
    cachedAq ? Promise.resolve(cachedAq.data) : fetchAirQuality(latitude, longitude),
  ]);

  if (!cachedWeather) setCached(weatherCacheKey, weatherRaw);
  if (!cachedAq) setCached(aqCacheKey, aqRaw);

  const weatherAlerts = analyzeWeatherForAlerts(cachedWeather ? cachedWeather.data : weatherRaw, locationName);
  const aqAlerts = analyzeAirQualityForAlerts(cachedAq ? cachedAq.data : aqRaw, locationName);

  const alerts = [...weatherAlerts, ...aqAlerts];

  res.json({
    success: true,
    data: { alerts },
  });
});

/**
 * @openapi
 * /api/alerts/scheduler/status:
 *   get:
 *     tags: [Alerts]
 *     summary: Get alert scheduler status
 *     description: Returns the current status of the push alert scheduler including run count, last run time, and configuration.
 *     responses:
 *       200:
 *         description: Scheduler status
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 data:
 *                   type: object
 *                   properties:
 *                     running:
 *                       type: boolean
 *                     lastRunAt:
 *                       type: string
 *                       format: date-time
 *                     runCount:
 *                       type: integer
 *                     totalAlertsSent:
 *                       type: integer
 */
router.get('/scheduler/status', (_req, res) => {
  res.json({
    success: true,
    data: getSchedulerStatus(),
  });
});

/**
 * @openapi
 * /api/alerts/scheduler/trigger:
 *   post:
 *     tags: [Alerts]
 *     summary: Trigger an immediate alert check
 *     description: Manually triggers the weather monitor to run a check cycle now.
 *     responses:
 *       200:
 *         description: Check completed
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 data:
 *                   type: object
 *                   properties:
 *                     locationsChecked:
 *                       type: integer
 *                     alertsSent:
 *                       type: integer
 */
router.post('/scheduler/trigger', async (_req, res) => {
  const result = await runCheck();
  res.json({
    success: true,
    data: result,
  });
});

/**
 * @openapi
 * /api/alerts/history:
 *   get:
 *     tags: [Alerts]
 *     summary: Query alert history
 *     description: Returns a paginated list of past alerts with optional severity and status filters.
 *     parameters:
 *       - name: limit
 *         in: query
 *         schema:
 *           type: integer
 *           default: 50
 *       - name: offset
 *         in: query
 *         schema:
 *           type: integer
 *           default: 0
 *       - name: severity
 *         in: query
 *         schema:
 *           type: string
 *           enum: [moderate, severe, extreme]
 *       - name: status
 *         in: query
 *         schema:
 *           type: string
 *           enum: [generated, sent, cooldown, filtered]
 *     responses:
 *       200:
 *         description: Alert history
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 data:
 *                   type: object
 *                   properties:
 *                     alerts:
 *                       type: array
 *                     total:
 *                       type: integer
 *                     limit:
 *                       type: integer
 *                     offset:
 *                       type: integer
 */
router.get('/history', (req, res) => {
  const limit = Math.min(parseInt(req.query.limit, 10) || 50, 200);
  const offset = parseInt(req.query.offset, 10) || 0;
  const { severity, status } = req.query;

  const result = getAlertHistory({ limit, offset, severity, status });

  res.json({
    success: true,
    data: { ...result, limit, offset },
  });
});

/**
 * @openapi
 * /api/alerts/history/cleanup:
 *   post:
 *     tags: [Alerts]
 *     summary: Trigger alert history cleanup
 *     description: Manually removes old alert history records beyond the retention period.
 *     responses:
 *       200:
 *         description: Cleanup completed
 */
router.post('/history/cleanup', (_req, res) => {
  const deleted = cleanupOldAlerts();
  res.json({
    success: true,
    data: { deleted },
  });
});

export default router;
