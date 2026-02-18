import { Router } from 'express';
import { searchLocations } from '../services/openMeteoService.js';
import { transformGeocodingResults } from '../services/transformService.js';
import { getCached, setCached, buildCacheKey } from '../services/cacheService.js';
import { validateSearchQuery } from '../validation.js';

const router = Router();

/**
 * @openapi
 * /api/geocoding/search:
 *   get:
 *     tags: [Geocoding]
 *     summary: Search for locations
 *     description: Search for cities and places by name. Returns matching locations with coordinates.
 *     parameters:
 *       - name: query
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *           minLength: 2
 *           example: New York
 *         description: Search term (min 2 characters)
 *       - name: count
 *         in: query
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 100
 *           default: 10
 *         description: Max results to return
 *       - name: lang
 *         in: query
 *         schema:
 *           type: string
 *           default: en
 *         description: Language code for results
 *     responses:
 *       200:
 *         description: List of matching locations
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 data:
 *                   type: array
 *                   items:
 *                     type: object
 *                 cached:
 *                   type: boolean
 *                 cachedAt:
 *                   type: string
 *                   nullable: true
 *       400:
 *         description: Query too short
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.get('/search', async (req, res) => {
  const { query, count = '10', lang = 'en' } = req.query;
  const trimmedQuery = validateSearchQuery(query);

  const countNum = Math.min(Math.max(parseInt(count, 10) || 10, 1), 100);

  const cacheKey = buildCacheKey('geo', { q: trimmedQuery.toLowerCase(), count: countNum, lang });
  const cached = getCached(cacheKey);

  if (cached) {
    const transformed = transformGeocodingResults(cached.data);
    return res.json({
      success: true,
      data: transformed,
      cached: true,
      cachedAt: cached.cachedAt,
    });
  }

  const raw = await searchLocations(trimmedQuery, countNum, lang);
  setCached(cacheKey, raw);

  const transformed = transformGeocodingResults(raw);
  res.json({
    success: true,
    data: transformed,
    cached: false,
    cachedAt: null,
  });
});

export default router;
