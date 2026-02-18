import { Router } from 'express';
import { fetchForecast } from '../../services/openMeteoService.js';
import { transformForecast } from '../../services/transformService.js';
import { getCached, setCached, buildCacheKey } from '../../services/cacheService.js';
import { validateBatchLocations } from '../../validation.js';
import { AppError } from '../../errors.js';

const router = Router();

/**
 * @openapi
 * /api/v2/batch/weather:
 *   post:
 *     tags: [Batch]
 *     summary: Fetch weather for multiple locations
 *     description: |
 *       Returns weather forecast data for up to 10 locations in a single request.
 *       Each location is fetched concurrently; individual failures do not prevent
 *       other locations from succeeding.
 *
 *       **Available only on API v2.**
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [locations]
 *             properties:
 *               locations:
 *                 type: array
 *                 minItems: 1
 *                 maxItems: 10
 *                 items:
 *                   type: object
 *                   required: [lat, lon]
 *                   properties:
 *                     lat:
 *                       type: number
 *                       minimum: -90
 *                       maximum: 90
 *                       example: 52.52
 *                     lon:
 *                       type: number
 *                       minimum: -180
 *                       maximum: 180
 *                       example: 13.41
 *                     units:
 *                       type: string
 *                       enum: [metric, imperial]
 *                       default: metric
 *                     timezone:
 *                       type: string
 *                       default: auto
 *     responses:
 *       200:
 *         description: Per-location weather results
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
 *                     results:
 *                       type: array
 *                       items:
 *                         type: object
 *                         properties:
 *                           location:
 *                             type: object
 *                             properties:
 *                               lat:
 *                                 type: number
 *                               lon:
 *                                 type: number
 *                           success:
 *                             type: boolean
 *                           data:
 *                             type: object
 *                             nullable: true
 *                           error:
 *                             type: object
 *                             nullable: true
 *                 meta:
 *                   type: object
 *                   properties:
 *                     requestId:
 *                       type: string
 *                     timestamp:
 *                       type: string
 *                       format: date-time
 *                     version:
 *                       type: string
 *                       example: v2
 *                     totalLocations:
 *                       type: integer
 *                     succeeded:
 *                       type: integer
 *                     failed:
 *                       type: integer
 *       400:
 *         description: Invalid request body or too many locations
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.post('/weather', async (req, res) => {
  const { locations } = validateBatchLocations(req.body);

  const results = await Promise.allSettled(
    locations.map(async (loc) => {
      const lat = Number(loc.lat);
      const lon = Number(loc.lon);
      const units = loc.units ?? 'metric';
      const timezone = loc.timezone ?? 'auto';

      if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
        throw new AppError('INVALID_COORDINATES', 'Latitude must be -90..90, longitude -180..180');
      }

      const cacheKey = buildCacheKey('weather', { lat, lon, timezone });
      const cached = getCached(cacheKey);

      if (cached) {
        return {
          location: { lat, lon },
          success: true,
          data: transformForecast(cached.data, units),
          cached: true,
          cachedAt: cached.cachedAt,
        };
      }

      const raw = await fetchForecast(lat, lon, timezone);
      setCached(cacheKey, raw);

      return {
        location: { lat, lon },
        success: true,
        data: transformForecast(raw, units),
        cached: false,
        cachedAt: null,
      };
    }),
  );

  const processedResults = results.map((result, index) => {
    if (result.status === 'fulfilled') return result.value;

    const loc = locations[index];
    const err = result.reason;
    return {
      location: { lat: loc.lat, lon: loc.lon },
      success: false,
      error: {
        code: err instanceof AppError ? err.code : 'UPSTREAM_ERROR',
        message: err.message || 'Failed to fetch weather data',
      },
    };
  });

  const succeeded = processedResults.filter((r) => r.success).length;
  const failed = processedResults.length - succeeded;

  res.json({
    success: true,
    data: { results: processedResults },
    totalLocations: locations.length,
    succeeded,
    failed,
  });
});

export default router;
