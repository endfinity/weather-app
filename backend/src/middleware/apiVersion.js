/**
 * Middleware for explicit /api/v1/* routes — sets the API-Version response header.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
export function v1Headers(req, res, next) {
  req.apiVersion = 'v1';
  res.set('API-Version', 'v1');
  next();
}

/**
 * Middleware for /api/v2/* routes — sets the API-Version header and wraps
 * JSON responses in the standard { success, data|error, meta } envelope.
 *
 * Success responses: extra top-level fields (cached, cachedAt, etc.) are
 * moved into the `meta` object alongside requestId, timestamp, and version.
 *
 * Error responses: { success: false, error, meta } with requestId and timestamp.
 *
 * Non-standard responses (e.g. health check) pass through unchanged.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
export function v2Envelope(req, res, next) {
  req.apiVersion = 'v2';
  res.set('API-Version', 'v2');

  const originalJson = res.json.bind(res);

  res.json = (body) => {
    if (!body || body.meta) return originalJson(body);

    const meta = {
      requestId: req.id,
      timestamp: new Date().toISOString(),
      version: 'v2',
    };

    if (body.success === true) {
      const { success: _s, data, ...extra } = body;
      return originalJson({ success: true, data, meta: { ...meta, ...extra } });
    }

    if (body.success === false) {
      const { success: _s, error, ...extra } = body;
      return originalJson({ success: false, error, meta: { ...meta, ...extra } });
    }

    return originalJson(body);
  };

  next();
}
