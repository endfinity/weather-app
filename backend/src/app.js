import crypto from 'node:crypto';
import express from 'express';
import helmet from 'helmet';
import cors from 'cors';
import compression from 'compression';
import swaggerUi from 'swagger-ui-express';
import { swaggerSpec } from './swagger.js';
import config from './config.js';
import rateLimiter from './middleware/rateLimiter.js';
import requestLogger from './middleware/requestLogger.js';
import requestTimeout from './middleware/requestTimeout.js';
import conditionalRequest from './middleware/conditionalRequest.js';
import { v1Headers, v2Envelope } from './middleware/apiVersion.js';
import errorHandler from './middleware/errorHandler.js';
import weatherRouter from './routes/weather.js';
import airQualityRouter from './routes/airQuality.js';
import geocodingRouter from './routes/geocoding.js';
import devicesRouter from './routes/devices.js';
import alertsRouter from './routes/alerts.js';
import historicalRouter from './routes/historical.js';
import radarRouter from './routes/radar.js';
import premiumRouter from './routes/premium.js';
import batchRouter from './routes/v2/batch.js';
import apiKeysRouter from './routes/apiKeys.js';
import { requirePremium } from './middleware/premiumAuth.js';
import { getStats } from './services/cacheService.js';
import { pingDb } from './db/database.js';
import { createRequire } from 'module';

const require = createRequire(import.meta.url);
const { version } = require('../package.json');

/**
 * Build and configure the Express application with all middleware and routes.
 * @returns {import('express').Express}
 */
export function createApp() {
  const app = express();

  // Disable Express's built-in ETag — we use our own middleware
  app.set('etag', false);

  if (config.server.trustProxy) {
    app.set('trust proxy', 1);
  }

  app.use(helmet());
  app.use(compression());

  const corsOrigins = config.cors.allowedOrigins;
  app.use(
    cors({
      origin: corsOrigins.includes('*') ? '*' : corsOrigins,
      methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
      allowedHeaders: ['Content-Type', 'Authorization', 'X-API-Key'],
      maxAge: 86400,
    }),
  );

  app.use((req, _res, next) => {
    req.id = crypto.randomUUID();
    next();
  });

  app.use(express.json({ limit: config.server.bodyLimit }));
  app.use(requestLogger);
  app.use(requestTimeout);
  app.use(rateLimiter);
  app.use(conditionalRequest);

  /**
   * Mount all standard API route handlers onto the given Express Router.
   * Reused for unversioned, v1, and v2 route prefixes.
   * @param {import('express').Router} target
   */
  function mountApiRoutes(target) {
    target.use('/weather', weatherRouter);
    target.use('/air-quality', airQualityRouter);
    target.use('/geocoding', geocodingRouter);
    target.use('/devices', devicesRouter);
    target.use('/alerts', alertsRouter);
    target.use('/historical', requirePremium, historicalRouter);
    target.use('/radar', requirePremium, radarRouter);
    target.use('/premium', premiumRouter);
    target.use('/api-keys', apiKeysRouter);
  }

  // Unversioned routes — backward compatible (same as v1, no version header)
  const apiRouter = express.Router();
  mountApiRoutes(apiRouter);
  app.use('/api', apiRouter);

  // Explicit v1 — identical behaviour with API-Version header
  const v1Router = express.Router();
  v1Router.use(v1Headers);
  mountApiRoutes(v1Router);
  app.use('/api/v1', v1Router);

  // v2 — response envelope { success, data|error, meta } + batch endpoint
  const v2Router = express.Router();
  v2Router.use(v2Envelope);
  mountApiRoutes(v2Router);
  v2Router.use('/batch', batchRouter);
  app.use('/api/v2', v2Router);

  app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec, { explorer: true }));
  app.get('/api-docs/swagger.json', (_req, res) => res.json(swaggerSpec));

  /**
   * @openapi
   * /api/health:
   *   get:
   *     tags: [System]
   *     summary: Health check
   *     description: Returns server health including DB connectivity, upstream reachability, memory usage and cache statistics.
   *     responses:
   *       200:
   *         description: Server is healthy
   *         content:
   *           application/json:
   *             schema:
   *               type: object
   *               properties:
   *                 status:
   *                   type: string
   *                   enum: [ok, degraded, unhealthy]
   *                 version:
   *                   type: string
   *                 uptime:
   *                   type: integer
   *                   description: Server uptime in seconds
   *                 timestamp:
   *                   type: string
   *                   format: date-time
   *                 checks:
   *                   type: object
   *                   properties:
   *                     database:
   *                       type: object
   *                     upstream:
   *                       type: object
   *                     memory:
   *                       type: object
   *                 cache:
   *                   type: object
   *                   properties:
   *                     keys:
   *                       type: integer
   *                     hits:
   *                       type: integer
   *                     misses:
   *                       type: integer
   */
  app.get('/api/health', async (req, res) => {
    const cacheStats = getStats();
    const dbCheck = pingDb();

    let upstreamCheck;
    try {
      const start = Date.now();
      const upstreamRes = await fetch(
        `${config.openMeteo.baseUrl}/forecast?latitude=0&longitude=0&current=temperature_2m`,
        { signal: AbortSignal.timeout(5000) },
      );
      upstreamCheck = { ok: upstreamRes.ok, statusCode: upstreamRes.status, responseMs: Date.now() - start };
    } catch {
      upstreamCheck = { ok: false, responseMs: -1 };
    }

    const mem = process.memoryUsage();
    const memoryCheck = { rss: mem.rss, heapUsed: mem.heapUsed, heapTotal: mem.heapTotal };

    let status = 'ok';
    if (!dbCheck.ok) status = 'unhealthy';
    else if (!upstreamCheck.ok) status = 'degraded';

    res.json({
      status,
      version,
      uptime: Math.floor(process.uptime()),
      timestamp: new Date().toISOString(),
      checks: {
        database: { status: dbCheck.ok ? 'ok' : 'error', responseMs: dbCheck.responseMs },
        upstream: { status: upstreamCheck.ok ? 'ok' : 'error', responseMs: upstreamCheck.responseMs },
        memory: memoryCheck,
      },
      cache: {
        keys: cacheStats.keys,
        hits: cacheStats.hits,
        misses: cacheStats.misses,
      },
    });
  });

  app.use((_req, res) => {
    res.status(404).json({
      success: false,
      error: { code: 'NOT_FOUND', message: 'The requested endpoint does not exist' },
    });
  });

  app.use(errorHandler);

  return app;
}
