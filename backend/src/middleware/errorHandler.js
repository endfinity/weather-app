import { OpenMeteoError } from '../services/openMeteoService.js';
import { AppError } from '../errors.js';
import logger from '../logger.js';

export { AppError };

/**
 * Express 5 error handler (4 args signature).
 * @param {Error} err
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} _next
 */
export default function errorHandler(err, req, res, _next) {
  if (res.headersSent) return;

  if (err instanceof AppError) {
    return res.status(err.httpStatus).json({
      success: false,
      error: { code: err.code, message: err.message },
    });
  }

  if (err instanceof OpenMeteoError) {
    const httpStatus = err.statusCode === 504 ? 504 : 502;
    const code = err.statusCode === 504 ? 'UPSTREAM_TIMEOUT' : 'UPSTREAM_ERROR';
    return res.status(httpStatus).json({
      success: false,
      error: { code, message: err.message },
    });
  }

  logger.error({ err, reqId: req?.id }, 'Unhandled error');
  res.status(500).json({
    success: false,
    error: { code: 'INTERNAL_ERROR', message: 'An unexpected error occurred' },
  });
}
