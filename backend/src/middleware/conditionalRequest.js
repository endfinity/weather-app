import crypto from 'node:crypto';

/**
 * Express middleware that adds ETag headers to GET JSON responses and handles
 * conditional requests via If-None-Match, returning 304 Not Modified when the
 * content hasn't changed.
 *
 * Uses weak ETags (`W/"<hash>"`) computed from the serialised JSON body.
 * Only applies to GET requests; other methods pass through unchanged.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
export default function conditionalRequest(req, res, next) {
  if (req.method !== 'GET') return next();

  const originalJson = res.json.bind(res);

  res.json = (body) => {
    if (res.headersSent) return originalJson(body);

    const jsonStr = JSON.stringify(body);
    const hash = crypto.createHash('md5').update(jsonStr).digest('hex');
    const etag = `W/"${hash}"`;

    res.set('ETag', etag);

    const ifNoneMatch = req.headers['if-none-match'];
    if (ifNoneMatch && ifNoneMatch === etag) {
      return res.status(304).end();
    }

    return originalJson(body);
  };

  next();
}
