/**
 * @typedef {'MISSING_PARAMETER' | 'INVALID_COORDINATES' | 'INVALID_QUERY' | 'INVALID_DATE' | 'NOT_FOUND' | 'UNAUTHORIZED' | 'FORBIDDEN' | 'RATE_LIMITED' | 'BATCH_TOO_LARGE' | 'UPSTREAM_ERROR' | 'UPSTREAM_TIMEOUT' | 'REQUEST_TIMEOUT' | 'INTERNAL_ERROR'} ErrorCode
 */

/**
 * Centralized registry mapping error codes to HTTP statuses and default messages.
 * @type {Readonly<Record<ErrorCode, {httpStatus: number, defaultMessage: string}>>}
 */
export const ErrorCodes = Object.freeze({
  MISSING_PARAMETER: { httpStatus: 400, defaultMessage: 'A required parameter is missing' },
  INVALID_COORDINATES: { httpStatus: 400, defaultMessage: 'Invalid geographic coordinates' },
  INVALID_QUERY: { httpStatus: 400, defaultMessage: 'Invalid search query' },
  INVALID_DATE: { httpStatus: 400, defaultMessage: 'Invalid date value' },
  NOT_FOUND: { httpStatus: 404, defaultMessage: 'Resource not found' },
  UNAUTHORIZED: { httpStatus: 401, defaultMessage: 'Authentication required' },
  FORBIDDEN: { httpStatus: 403, defaultMessage: 'Access denied' },
  RATE_LIMITED: { httpStatus: 429, defaultMessage: 'Too many requests' },
  BATCH_TOO_LARGE: { httpStatus: 400, defaultMessage: 'Too many locations in batch request' },
  UPSTREAM_ERROR: { httpStatus: 502, defaultMessage: 'Upstream service error' },
  UPSTREAM_TIMEOUT: { httpStatus: 504, defaultMessage: 'Upstream service timeout' },
  REQUEST_TIMEOUT: { httpStatus: 408, defaultMessage: 'Request timed out' },
  INTERNAL_ERROR: { httpStatus: 500, defaultMessage: 'An unexpected error occurred' },
});

/**
 * Application-level error with a registered error code.
 */
export class AppError extends Error {
  /**
   * @param {ErrorCode} code - Registered error code from ErrorCodes
   * @param {string} [message] - Custom message (falls back to code's defaultMessage)
   * @param {number} [httpStatus] - Override HTTP status (falls back to code's httpStatus)
   */
  constructor(code, message, httpStatus) {
    const def = ErrorCodes[code];
    super(message || def?.defaultMessage || 'An error occurred');
    this.name = 'AppError';
    this.code = code;
    this.httpStatus = httpStatus ?? def?.httpStatus ?? 500;
  }
}

/**
 * Factory to create an AppError from a registered code.
 * @param {ErrorCode} code
 * @param {string} [message] - Optional custom message
 * @returns {AppError}
 */
export function createError(code, message) {
  return new AppError(code, message);
}
