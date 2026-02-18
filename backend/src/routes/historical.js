import { Router } from 'express';
import { fetchHistorical } from '../services/openMeteoService.js';
import { transformHistorical } from '../services/transformService.js';
import { getCached, setCached, buildCacheKey } from '../services/cacheService.js';
import { validateCoordinates, validateDateRange } from '../validation.js';

const router = Router();

/**
 * @openapi
 * /api/historical:
 *   get:
 *     tags: [Historical]
 *     summary: Get historical weather data
 *     description: Returns historical weather data for a date range. Requires premium subscription.
 *     security:
 *       - BearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/Latitude'
 *       - $ref: '#/components/parameters/Longitude'
 *       - name: start_date
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *           format: date
 *           example: '2024-01-01'
 *         description: Start date (YYYY-MM-DD)
 *       - name: end_date
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *           format: date
 *           example: '2024-01-07'
 *         description: End date (YYYY-MM-DD, max 366-day range)
 *       - $ref: '#/components/parameters/Units'
 *       - $ref: '#/components/parameters/Timezone'
 *     responses:
 *       200:
 *         description: Historical weather data
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
 *                 cached:
 *                   type: boolean
 *                 cachedAt:
 *                   type: string
 *                   nullable: true
 *       400:
 *         description: Invalid coordinates or date range
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       401:
 *         description: Premium subscription required
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
// GET /api/historical?lat=...&lon=...&start_date=...&end_date=...&units=metric&timezone=auto
router.get('/', async (req, res) => {
  const { lat, lon, start_date, end_date, units = 'metric', timezone = 'auto' } = req.query;
  const { latitude, longitude } = validateCoordinates(lat, lon);
  const { startDate, endDate } = validateDateRange(start_date, end_date);

  const cacheKey = buildCacheKey('historical', {
    lat: latitude,
    lon: longitude,
    start: startDate,
    end: endDate,
    timezone,
  });
  const cached = getCached(cacheKey);

  if (cached) {
    const transformed = transformHistorical(cached.data, units);
    return res.json({
      success: true,
      data: transformed,
      cached: true,
      cachedAt: cached.cachedAt,
    });
  }

  const raw = await fetchHistorical(latitude, longitude, startDate, endDate, timezone);
  setCached(cacheKey, raw);

  const transformed = transformHistorical(raw, units);
  res.json({
    success: true,
    data: transformed,
    cached: false,
    cachedAt: null,
  });
});

/**
 * @openapi
 * /api/historical/on-this-day:
 *   get:
 *     tags: [Historical]
 *     summary: Get historical data for today's date across past years
 *     description: Returns weather data for the current calendar date over the specified number of past years. Requires premium subscription.
 *     security:
 *       - BearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/Latitude'
 *       - $ref: '#/components/parameters/Longitude'
 *       - name: years
 *         in: query
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 30
 *           default: 5
 *         description: Number of past years to include
 *       - $ref: '#/components/parameters/Units'
 *       - $ref: '#/components/parameters/Timezone'
 *     responses:
 *       200:
 *         description: Yearly historical data for today's date
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
 *                     date:
 *                       type: string
 *                       example: '07-14'
 *                     years:
 *                       type: array
 *                       items:
 *                         type: object
 *                         properties:
 *                           year:
 *                             type: integer
 *                           data:
 *                             type: object
 *                             nullable: true
 *                           error:
 *                             type: string
 *       401:
 *         description: Premium subscription required
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
// GET /api/historical/on-this-day?lat=...&lon=...&years=5&units=metric&timezone=auto
router.get('/on-this-day', async (req, res) => {
  const { lat, lon, years = '5', units = 'metric', timezone = 'auto' } = req.query;
  const { latitude, longitude } = validateCoordinates(lat, lon);

  const numYears = Math.min(Math.max(parseInt(years, 10) || 5, 1), 30);
  const today = new Date();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');

  const yearlyData = [];

  for (let i = 1; i <= numYears; i++) {
    const year = today.getFullYear() - i;
    const dateStr = `${year}-${month}-${day}`;

    const cacheKey = buildCacheKey('otd', { lat: latitude, lon: longitude, date: dateStr });
    const cached = getCached(cacheKey);

    if (cached) {
      yearlyData.push({ year, data: transformHistorical(cached.data, units) });
    } else {
      try {
        const raw = await fetchHistorical(latitude, longitude, dateStr, dateStr, timezone);
        setCached(cacheKey, raw);
        yearlyData.push({ year, data: transformHistorical(raw, units) });
      } catch {
        yearlyData.push({ year, data: null, error: 'Data not available' });
      }
    }
  }

  res.json({
    success: true,
    data: {
      date: `${month}-${day}`,
      years: yearlyData,
    },
    cached: false,
  });
});

export default router;
