import { Router } from 'express';
import { fetchAirQuality } from '../services/openMeteoService.js';
import { transformAirQuality } from '../services/transformService.js';
import { getCached, setCached, buildCacheKey } from '../services/cacheService.js';
import { validateCoordinates } from '../validation.js';

const router = Router();

/**
 * @openapi
 * /api/air-quality:
 *   get:
 *     tags: [Air Quality]
 *     summary: Get air quality data
 *     description: Returns current air quality index and pollutant levels for the given coordinates.
 *     parameters:
 *       - $ref: '#/components/parameters/Latitude'
 *       - $ref: '#/components/parameters/Longitude'
 *     responses:
 *       200:
 *         description: Air quality data
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
 *                   description: Transformed air quality data
 *                 cached:
 *                   type: boolean
 *                 cachedAt:
 *                   type: string
 *                   nullable: true
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

  const cacheKey = buildCacheKey('aq', { lat: latitude, lon: longitude });
  const cached = getCached(cacheKey);

  if (cached) {
    const transformed = transformAirQuality(cached.data);
    return res.json({
      success: true,
      data: transformed,
      cached: true,
      cachedAt: cached.cachedAt,
    });
  }

  const raw = await fetchAirQuality(latitude, longitude);
  setCached(cacheKey, raw);

  const transformed = transformAirQuality(raw);
  res.json({
    success: true,
    data: transformed,
    cached: false,
    cachedAt: null,
  });
});

export default router;
