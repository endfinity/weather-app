import config from '../config.js';

const FORECAST_CURRENT_VARS = [
  'temperature_2m',
  'relative_humidity_2m',
  'apparent_temperature',
  'is_day',
  'precipitation',
  'rain',
  'showers',
  'snowfall',
  'weather_code',
  'cloud_cover',
  'pressure_msl',
  'surface_pressure',
  'wind_speed_10m',
  'wind_direction_10m',
  'wind_gusts_10m',
].join(',');

const FORECAST_HOURLY_VARS = [
  'temperature_2m',
  'relative_humidity_2m',
  'dew_point_2m',
  'apparent_temperature',
  'precipitation_probability',
  'precipitation',
  'rain',
  'showers',
  'snowfall',
  'weather_code',
  'cloud_cover',
  'visibility',
  'wind_speed_10m',
  'wind_direction_10m',
  'wind_gusts_10m',
  'uv_index',
  'pressure_msl',
  'is_day',
].join(',');

const FORECAST_DAILY_VARS = [
  'temperature_2m_max',
  'temperature_2m_min',
  'apparent_temperature_max',
  'apparent_temperature_min',
  'precipitation_sum',
  'precipitation_probability_max',
  'rain_sum',
  'snowfall_sum',
  'weather_code',
  'sunrise',
  'sunset',
  'sunshine_duration',
  'daylight_duration',
  'uv_index_max',
  'wind_speed_10m_max',
  'wind_gusts_10m_max',
  'wind_direction_10m_dominant',
  'precipitation_hours',
].join(',');

const FORECAST_MINUTELY15_VARS = 'precipitation,rain,snowfall,weather_code';

const AQ_CURRENT_VARS = [
  'us_aqi',
  'pm10',
  'pm2_5',
  'carbon_monoxide',
  'nitrogen_dioxide',
  'sulphur_dioxide',
  'ozone',
  'uv_index',
  'uv_index_clear_sky',
].join(',');

const AQ_HOURLY_VARS = [
  'pm10',
  'pm2_5',
  'carbon_monoxide',
  'nitrogen_dioxide',
  'sulphur_dioxide',
  'ozone',
  'us_aqi',
  'uv_index',
  'uv_index_clear_sky',
  'alder_pollen',
  'birch_pollen',
  'grass_pollen',
  'mugwort_pollen',
  'olive_pollen',
  'ragweed_pollen',
].join(',');

const FETCH_TIMEOUT_MS = 10_000;

/**
 * Error representing a failed or timed-out Open-Meteo API call.
 */
class OpenMeteoError extends Error {
  /**
   * @param {string} message
   * @param {number} statusCode - HTTP status code to surface
   * @param {boolean} upstream - Whether the error originated from an upstream service
   */
  constructor(message, statusCode, upstream) {
    super(message);
    this.name = 'OpenMeteoError';
    this.statusCode = statusCode;
    this.upstream = upstream;
  }
}

/**
 * Fetch JSON from a URL with timeout and error wrapping.
 * @param {string} url
 * @returns {Promise<object>}
 * @throws {OpenMeteoError}
 */
async function fetchJson(url) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS);

  try {
    const response = await fetch(url, { signal: controller.signal });

    if (!response.ok) {
      const body = await response.text().catch(() => '');
      throw new OpenMeteoError(`Open-Meteo returned ${response.status}: ${body}`, response.status, true);
    }

    return await response.json();
  } catch (err) {
    if (err.name === 'AbortError') {
      throw new OpenMeteoError('Open-Meteo request timed out', 504, true);
    }
    if (err instanceof OpenMeteoError) throw err;
    throw new OpenMeteoError(`Failed to fetch from Open-Meteo: ${err.message}`, 502, true);
  } finally {
    clearTimeout(timeoutId);
  }
}

/**
 * Fetch a 16-day weather forecast from Open-Meteo.
 * @param {number} lat - Latitude
 * @param {number} lon - Longitude
 * @param {string} [timezone='auto']
 * @returns {Promise<object>} Raw Open-Meteo forecast response
 */
export async function fetchForecast(lat, lon, timezone = 'auto') {
  const params = new URLSearchParams({
    latitude: String(lat),
    longitude: String(lon),
    current: FORECAST_CURRENT_VARS,
    hourly: FORECAST_HOURLY_VARS,
    daily: FORECAST_DAILY_VARS,
    minutely_15: FORECAST_MINUTELY15_VARS,
    timezone,
    forecast_days: '16',
  });

  const url = `${config.openMeteo.baseUrl}/forecast?${params}`;
  return fetchJson(url);
}

/**
 * Fetch current air quality data from Open-Meteo.
 * @param {number} lat - Latitude
 * @param {number} lon - Longitude
 * @returns {Promise<object>} Raw Open-Meteo air quality response
 */
export async function fetchAirQuality(lat, lon) {
  const params = new URLSearchParams({
    latitude: String(lat),
    longitude: String(lon),
    current: AQ_CURRENT_VARS,
    hourly: AQ_HOURLY_VARS,
  });

  const url = `${config.openMeteo.aqBaseUrl}/air-quality?${params}`;
  return fetchJson(url);
}

/**
 * Search for locations by name via Open-Meteo geocoding.
 * @param {string} query - Search term
 * @param {number} [count=10] - Max results
 * @param {string} [lang='en'] - Language code
 * @returns {Promise<object>} Raw geocoding response
 */
export async function searchLocations(query, count = 10, lang = 'en') {
  const params = new URLSearchParams({
    name: query,
    count: String(count),
    language: lang,
    format: 'json',
  });

  const url = `${config.openMeteo.geoBaseUrl}/search?${params}`;
  return fetchJson(url);
}

const HISTORICAL_DAILY_VARS = [
  'temperature_2m_max',
  'temperature_2m_min',
  'apparent_temperature_max',
  'apparent_temperature_min',
  'precipitation_sum',
  'rain_sum',
  'snowfall_sum',
  'weather_code',
  'sunrise',
  'sunset',
  'sunshine_duration',
  'wind_speed_10m_max',
  'wind_gusts_10m_max',
  'wind_direction_10m_dominant',
].join(',');

const HISTORICAL_HOURLY_VARS = [
  'temperature_2m',
  'relative_humidity_2m',
  'apparent_temperature',
  'precipitation',
  'rain',
  'snowfall',
  'weather_code',
  'cloud_cover',
  'wind_speed_10m',
  'wind_direction_10m',
  'pressure_msl',
].join(',');

/**
 * Fetch historical weather data from Open-Meteo archive.
 * @param {number} lat - Latitude
 * @param {number} lon - Longitude
 * @param {string} startDate - Start date (YYYY-MM-DD)
 * @param {string} endDate - End date (YYYY-MM-DD)
 * @param {string} [timezone='auto']
 * @returns {Promise<object>} Raw historical weather response
 */
export async function fetchHistorical(lat, lon, startDate, endDate, timezone = 'auto') {
  const params = new URLSearchParams({
    latitude: String(lat),
    longitude: String(lon),
    start_date: startDate,
    end_date: endDate,
    daily: HISTORICAL_DAILY_VARS,
    hourly: HISTORICAL_HOURLY_VARS,
    timezone,
  });

  const url = `${config.openMeteo.archiveBaseUrl}/archive?${params}`;
  return fetchJson(url);
}

export { OpenMeteoError };
export default { fetchForecast, fetchAirQuality, searchLocations, fetchHistorical };
