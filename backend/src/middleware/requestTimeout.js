import config from '../config.js';

/**
 * Express middleware that aborts requests exceeding the configured timeout.
 * Responds with 408 Request Timeout when the deadline is exceeded.
 * A timeout of 0 disables the middleware.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
export default function requestTimeout(req, res, next) {
  const ms = config.server.requestTimeoutMs;
  if (!ms) return next();

  const timer = setTimeout(() => {
    if (!res.headersSent) {
      res.status(408).json({
        success: false,
        error: { code: 'REQUEST_TIMEOUT', message: `Request timed out after ${ms}ms` },
      });
    }
  }, ms);
  timer.unref();

  res.on('finish', () => clearTimeout(timer));
  res.on('close', () => clearTimeout(timer));

  next();
}
