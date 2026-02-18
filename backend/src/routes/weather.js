import { Router } from 'express';
import { fetchForecast, fetchAirQuality } from '../services/openMeteoService.js';
import { transformForecast, transformAirQuality } from '../services/transformService.js';
import { getCached, setCached, buildCacheKey } from '../services/cacheService.js';
import { validateCoordinates } from '../validation.js';

const router = Router();

/**
 * @openapi
 * /api/weather:
 *   get:
 *     tags: [Weather]
 *     summary: Get weather forecast
 *     description: Returns a 7-day weather forecast for the given coordinates.
 *     parameters:
 *       - $ref: '#/components/parameters/Latitude'
 *       - $ref: '#/components/parameters/Longitude'
 *       - $ref: '#/components/parameters/Units'
 *       - $ref: '#/components/parameters/Timezone'
 *     responses:
 *       200:
 *         description: Weather forecast data
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
 *                   description: Transformed forecast data
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
  const { lat, lon, units = 'metric', timezone = 'auto' } = req.query;
  const { latitude, longitude } = validateCoordinates(lat, lon);

  const cacheKey = buildCacheKey('weather', { lat: latitude, lon: longitude, timezone });
  const cached = getCached(cacheKey);

  if (cached) {
    const transformed = transformForecast(cached.data, units);
    return res.json({
      success: true,
      data: transformed,
      cached: true,
      cachedAt: cached.cachedAt,
    });
  }

  const raw = await fetchForecast(latitude, longitude, timezone);
  setCached(cacheKey, raw);

  const transformed = transformForecast(raw, units);
  res.json({
    success: true,
    data: transformed,
    cached: false,
    cachedAt: null,
  });
});

/**
 * @openapi
 * /api/weather/combined:
 *   get:
 *     tags: [Weather]
 *     summary: Get combined weather and air quality data
 *     description: Returns both weather forecast and air quality data in a single request.
 *     parameters:
 *       - $ref: '#/components/parameters/Latitude'
 *       - $ref: '#/components/parameters/Longitude'
 *       - $ref: '#/components/parameters/Units'
 *       - $ref: '#/components/parameters/Timezone'
 *     responses:
 *       200:
 *         description: Combined weather and air quality data
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
 *                     weather:
 *                       type: object
 *                     airQuality:
 *                       type: object
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
router.get('/combined', async (req, res) => {
  const { lat, lon, units = 'metric', timezone = 'auto' } = req.query;
  const { latitude, longitude } = validateCoordinates(lat, lon);

  const weatherCacheKey = buildCacheKey('weather', { lat: latitude, lon: longitude, timezone });
  const aqCacheKey = buildCacheKey('aq', { lat: latitude, lon: longitude });

  const cachedWeather = getCached(weatherCacheKey);
  const cachedAq = getCached(aqCacheKey);

  const [weatherRaw, aqRaw] = await Promise.all([
    cachedWeather ? Promise.resolve(cachedWeather.data) : fetchForecast(latitude, longitude, timezone),
    cachedAq ? Promise.resolve(cachedAq.data) : fetchAirQuality(latitude, longitude),
  ]);

  if (!cachedWeather) setCached(weatherCacheKey, weatherRaw);
  if (!cachedAq) setCached(aqCacheKey, aqRaw);

  const weather = transformForecast(cachedWeather ? cachedWeather.data : weatherRaw, units);
  const airQuality = transformAirQuality(cachedAq ? cachedAq.data : aqRaw);

  res.json({
    success: true,
    data: { weather, airQuality },
    cached: Boolean(cachedWeather && cachedAq),
    cachedAt: cachedWeather?.cachedAt ?? cachedAq?.cachedAt ?? null,
  });
});

export default router;
