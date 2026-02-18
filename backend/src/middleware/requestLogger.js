import logger from '../logger.js';

const reqLogger = logger.child({ component: 'http' });

/**
 * Express middleware that logs every completed request with method, URL, status, and duration.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
export default function requestLogger(req, res, next) {
  const start = Date.now();

  res.on('finish', () => {
    const data = {
      reqId: req.id,
      method: req.method,
      url: req.originalUrl,
      status: res.statusCode,
      durationMs: Date.now() - start,
      ip: req.ip || req.socket.remoteAddress,
    };

    if (res.statusCode >= 500) {
      reqLogger.error(data, 'request completed');
    } else if (res.statusCode >= 400) {
      reqLogger.warn(data, 'request completed');
    } else {
      reqLogger.info(data, 'request completed');
    }
  });

  next();
}
