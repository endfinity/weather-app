import { Router } from 'express';
import { getCached, setCached, buildCacheKey } from '../services/cacheService.js';

const RAINVIEWER_API_URL = 'https://api.rainviewer.com/public/weather-maps.json';
const FETCH_TIMEOUT_MS = 10_000;

const router = Router();

async function fetchRainViewerMaps() {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS);

  try {
    const response = await fetch(RAINVIEWER_API_URL, { signal: controller.signal });

    if (!response.ok) {
      throw new Error(`RainViewer returned ${response.status}`);
    }

    return await response.json();
  } finally {
    clearTimeout(timeoutId);
  }
}

/**
 * @openapi
 * /api/radar:
 *   get:
 *     tags: [Radar]
 *     summary: Get radar and satellite map data
 *     description: Returns radar frame metadata and satellite imagery from RainViewer. Requires premium subscription.
 *     security:
 *       - BearerAuth: []
 *     responses:
 *       200:
 *         description: Radar and satellite frame metadata
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
 *                     generated:
 *                       type: integer
 *                       description: Generation timestamp
 *                     host:
 *                       type: string
 *                     radar:
 *                       type: object
 *                       properties:
 *                         past:
 *                           type: array
 *                           items:
 *                             type: object
 *                             properties:
 *                               time:
 *                                 type: integer
 *                               path:
 *                                 type: string
 *                         nowcast:
 *                           type: array
 *                           items:
 *                             type: object
 *                             properties:
 *                               time:
 *                                 type: integer
 *                               path:
 *                                 type: string
 *                     satellite:
 *                       type: object
 *                       properties:
 *                         infrared:
 *                           type: array
 *                           items:
 *                             type: object
 *                             properties:
 *                               time:
 *                                 type: integer
 *                               path:
 *                                 type: string
 *                     tileUrlTemplate:
 *                       type: string
 *                       example: '{host}{path}/{size}/{z}/{x}/{y}/{color}/{options}.png'
 *                 cached:
 *                   type: boolean
 *                 cachedAt:
 *                   type: string
 *                   nullable: true
 *       401:
 *         description: Premium subscription required
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
// GET /api/radar — returns radar frame metadata from RainViewer
router.get('/', async (req, res) => {
  const cacheKey = buildCacheKey('radar', {});
  const cached = getCached(cacheKey);

  if (cached) {
    return res.json({
      success: true,
      data: cached.data,
      cached: true,
      cachedAt: cached.cachedAt,
    });
  }

  const raw = await fetchRainViewerMaps();

  const data = {
    generated: raw.generated,
    host: raw.host,
    radar: {
      past: (raw.radar?.past ?? []).map((frame) => ({
        time: frame.time,
        path: frame.path,
      })),
      nowcast: (raw.radar?.nowcast ?? []).map((frame) => ({
        time: frame.time,
        path: frame.path,
      })),
    },
    satellite: {
      infrared: (raw.satellite?.infrared ?? []).map((frame) => ({
        time: frame.time,
        path: frame.path,
      })),
    },
    // Tile URL pattern: {host}{path}/{size}/{z}/{x}/{y}/{color}/{options}.png
    tileUrlTemplate: '{host}{path}/{size}/{z}/{x}/{y}/{color}/{options}.png',
  };

  setCached(cacheKey, data);

  res.json({
    success: true,
    data,
    cached: false,
    cachedAt: null,
  });
});

export default router;
