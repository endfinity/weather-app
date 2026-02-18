import { z } from 'zod';
import { AppError } from './errors.js';
import config from './config.js';

/**
 * Preprocess a value using parseFloat, converting non-numeric inputs to undefined
 * so that z.number() rejects them with an invalid_type error.
 */
const floatFromString = z.preprocess((val) => {
  if (val === undefined || val === null || val === '') return undefined;
  const num = parseFloat(String(val));
  return Number.isNaN(num) ? undefined : num;
}, z.number());

const latitudeSchema = floatFromString.refine((v) => v >= -90 && v <= 90, 'Latitude must be between -90 and 90');

const longitudeSchema = floatFromString.refine((v) => v >= -180 && v <= 180, 'Longitude must be between -180 and 180');

/** Zod schema for geographic coordinates. */
export const coordinatesSchema = z.object({
  lat: latitudeSchema,
  lon: longitudeSchema,
});

/**
 * Validate and parse geographic coordinates.
 * @param {string | number | undefined} lat
 * @param {string | number | undefined} lon
 * @returns {{ latitude: number, longitude: number }}
 * @throws {AppError} MISSING_PARAMETER when lat/lon are missing or non-numeric
 * @throws {AppError} INVALID_COORDINATES when lat/lon are out of range
 */
export function validateCoordinates(lat, lon) {
  const result = coordinatesSchema.safeParse({ lat, lon });
  if (!result.success) {
    const issue = result.error.issues[0];
    if (issue.code === 'custom') {
      throw new AppError('INVALID_COORDINATES', issue.message);
    }
    throw new AppError('MISSING_PARAMETER', 'Both lat and lon query parameters are required');
  }
  return { latitude: result.data.lat, longitude: result.data.lon };
}

const dateStringSchema = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}$/, 'Dates must be in YYYY-MM-DD format')
  .refine((s) => !Number.isNaN(new Date(s).getTime()), 'Invalid date values');

/** Zod schema for a historical date range. */
export const dateRangeSchema = z
  .object({
    start_date: dateStringSchema,
    end_date: dateStringSchema,
  })
  .refine(({ start_date, end_date }) => new Date(end_date) >= new Date(start_date), {
    message: 'end_date must be after start_date',
  })
  .refine(
    ({ start_date, end_date }) => {
      const diffMs = new Date(end_date).getTime() - new Date(start_date).getTime();
      return diffMs / (1000 * 60 * 60 * 24) <= 366;
    },
    { message: 'Date range cannot exceed 366 days' },
  );

/**
 * Validate and parse a historical date range.
 * @param {string | undefined} startDate - Start date in YYYY-MM-DD format
 * @param {string | undefined} endDate - End date in YYYY-MM-DD format
 * @returns {{ startDate: string, endDate: string }}
 * @throws {AppError} MISSING_PARAMETER when dates are missing
 * @throws {AppError} INVALID_DATE when format, order, or range is invalid
 */
export function validateDateRange(startDate, endDate) {
  const result = dateRangeSchema.safeParse({ start_date: startDate, end_date: endDate });
  if (!result.success) {
    const issue = result.error.issues[0];
    if (issue.code === 'invalid_type') {
      throw new AppError('MISSING_PARAMETER', 'Both start_date and end_date are required');
    }
    throw new AppError('INVALID_DATE', issue.message);
  }
  return { startDate: result.data.start_date, endDate: result.data.end_date };
}

/**
 * Validate a geocoding search query string.
 * @param {string | undefined} query - The search term
 * @returns {string} Trimmed query string
 * @throws {AppError} INVALID_QUERY when query is missing or too short
 */
export function validateSearchQuery(query) {
  const schema = z.string().trim().min(2, 'Search query must be at least 2 characters');
  const result = schema.safeParse(query);
  if (!result.success) {
    throw new AppError('INVALID_QUERY', 'Search query must be at least 2 characters');
  }
  return result.data;
}

/**
 * Validate the premium purchase verification request body.
 * @param {{ deviceId?: string, purchaseToken?: string, productId?: string }} body
 * @returns {{ deviceId: string, purchaseToken: string, productId: string }}
 * @throws {AppError} MISSING_PARAMETER when any required field is missing
 */
export function validatePremiumVerify(body) {
  const schema = z.object({
    deviceId: z.string().min(1),
    purchaseToken: z.string().min(1),
    productId: z.string().min(1),
  });
  const result = schema.safeParse(body);
  if (!result.success) {
    throw new AppError('MISSING_PARAMETER', 'deviceId, purchaseToken, and productId are required');
  }
  return result.data;
}

/**
 * Validate an FCM token from a device registration request body.
 * @param {{ fcm_token?: unknown }} body
 * @throws {AppError} MISSING_PARAMETER when fcm_token is missing or not a string
 */
export function validateFcmToken(body) {
  const schema = z.object({
    fcm_token: z.string().min(1, 'fcm_token is required'),
  });
  const result = schema.safeParse(body);
  if (!result.success) {
    throw new AppError('MISSING_PARAMETER', 'fcm_token is required');
  }
}

/**
 * Validate a device_id query parameter.
 * @param {string | undefined} deviceId
 * @returns {string} Validated device ID
 * @throws {AppError} MISSING_PARAMETER when device_id is missing
 */
export function validateDeviceId(deviceId) {
  const schema = z.string().min(1);
  const result = schema.safeParse(deviceId);
  if (!result.success) {
    throw new AppError('MISSING_PARAMETER', 'device_id query parameter is required');
  }
  return result.data;
}

/** Zod schema for the batch weather request body. */
export const batchLocationsSchema = z.object({
  locations: z
    .array(
      z.object({
        lat: z.coerce.number().min(-90).max(90),
        lon: z.coerce.number().min(-180).max(180),
        units: z.enum(['metric', 'imperial']).optional(),
        timezone: z.string().optional(),
      }),
    )
    .min(1, 'At least one location is required'),
});

/**
 * Validate and parse a batch locations request body.
 * @param {unknown} body - Request body
 * @returns {{ locations: Array<{ lat: number, lon: number, units?: string, timezone?: string }> }}
 * @throws {AppError} MISSING_PARAMETER when locations array is missing or empty
 * @throws {AppError} BATCH_TOO_LARGE when the array exceeds the configured max
 */
export function validateBatchLocations(body) {
  const result = batchLocationsSchema.safeParse(body);
  if (!result.success) {
    const issue = result.error.issues[0];
    throw new AppError('MISSING_PARAMETER', issue.message);
  }

  const maxLocations = config.batch.maxLocations;
  if (result.data.locations.length > maxLocations) {
    throw new AppError('BATCH_TOO_LARGE', `Maximum ${maxLocations} locations per batch request`);
  }

  return result.data;
}
