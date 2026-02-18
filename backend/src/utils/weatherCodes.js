const WMO_CODES = {
  0: { description: 'Clear sky', dayIcon: 'clear_day', nightIcon: 'clear_night', severity: 'none' },
  1: { description: 'Mainly clear', dayIcon: 'partly_cloudy_day', nightIcon: 'partly_cloudy_night', severity: 'none' },
  2: { description: 'Partly cloudy', dayIcon: 'partly_cloudy_day', nightIcon: 'partly_cloudy_night', severity: 'none' },
  3: { description: 'Overcast', dayIcon: 'overcast', nightIcon: 'overcast', severity: 'none' },
  45: { description: 'Fog', dayIcon: 'fog', nightIcon: 'fog', severity: 'low' },
  48: { description: 'Depositing rime fog', dayIcon: 'fog', nightIcon: 'fog', severity: 'low' },
  51: { description: 'Drizzle: Light', dayIcon: 'drizzle_light', nightIcon: 'drizzle_light', severity: 'low' },
  53: { description: 'Drizzle: Moderate', dayIcon: 'drizzle', nightIcon: 'drizzle', severity: 'low' },
  55: { description: 'Drizzle: Dense', dayIcon: 'drizzle', nightIcon: 'drizzle', severity: 'low' },
  56: {
    description: 'Freezing drizzle: Light',
    dayIcon: 'freezing_drizzle',
    nightIcon: 'freezing_drizzle',
    severity: 'moderate',
  },
  57: {
    description: 'Freezing drizzle: Dense',
    dayIcon: 'freezing_drizzle',
    nightIcon: 'freezing_drizzle',
    severity: 'moderate',
  },
  61: { description: 'Rain: Slight', dayIcon: 'rain_light', nightIcon: 'rain_light', severity: 'low' },
  63: { description: 'Rain: Moderate', dayIcon: 'rain', nightIcon: 'rain', severity: 'low' },
  65: { description: 'Rain: Heavy', dayIcon: 'rain_heavy', nightIcon: 'rain_heavy', severity: 'moderate' },
  66: {
    description: 'Freezing rain: Light',
    dayIcon: 'freezing_rain',
    nightIcon: 'freezing_rain',
    severity: 'moderate',
  },
  67: { description: 'Freezing rain: Heavy', dayIcon: 'freezing_rain', nightIcon: 'freezing_rain', severity: 'high' },
  71: { description: 'Snow fall: Slight', dayIcon: 'snow_light', nightIcon: 'snow_light', severity: 'low' },
  73: { description: 'Snow fall: Moderate', dayIcon: 'snow', nightIcon: 'snow', severity: 'moderate' },
  75: { description: 'Snow fall: Heavy', dayIcon: 'snow_heavy', nightIcon: 'snow_heavy', severity: 'high' },
  77: { description: 'Snow grains', dayIcon: 'snow_grains', nightIcon: 'snow_grains', severity: 'low' },
  80: {
    description: 'Rain showers: Slight',
    dayIcon: 'rain_showers_light',
    nightIcon: 'rain_showers_light',
    severity: 'low',
  },
  81: {
    description: 'Rain showers: Moderate',
    dayIcon: 'rain_showers',
    nightIcon: 'rain_showers',
    severity: 'moderate',
  },
  82: {
    description: 'Rain showers: Violent',
    dayIcon: 'rain_showers_heavy',
    nightIcon: 'rain_showers_heavy',
    severity: 'high',
  },
  85: {
    description: 'Snow showers: Slight',
    dayIcon: 'snow_showers_light',
    nightIcon: 'snow_showers_light',
    severity: 'moderate',
  },
  86: {
    description: 'Snow showers: Heavy',
    dayIcon: 'snow_showers_heavy',
    nightIcon: 'snow_showers_heavy',
    severity: 'high',
  },
  95: {
    description: 'Thunderstorm: Slight or moderate',
    dayIcon: 'thunderstorm',
    nightIcon: 'thunderstorm',
    severity: 'high',
  },
  96: {
    description: 'Thunderstorm with slight hail',
    dayIcon: 'thunderstorm_hail',
    nightIcon: 'thunderstorm_hail',
    severity: 'severe',
  },
  99: {
    description: 'Thunderstorm with heavy hail',
    dayIcon: 'thunderstorm_hail',
    nightIcon: 'thunderstorm_hail',
    severity: 'severe',
  },
};

/**
 * Get a human-readable description for a WMO weather code.
 * @param {number} code - WMO weather condition code
 * @returns {string}
 */
export function getWeatherDescription(code) {
  return WMO_CODES[code]?.description ?? 'Unknown';
}

/**
 * Get the icon identifier for a WMO weather code.
 * @param {number} code - WMO weather condition code
 * @param {boolean} isDay - true for daytime icon
 * @returns {string}
 */
export function getWeatherIcon(code, isDay) {
  const entry = WMO_CODES[code];
  if (!entry) return 'unknown';
  return isDay ? entry.dayIcon : entry.nightIcon;
}

/**
 * Get the severity level for a WMO weather code.
 * @param {number} code - WMO weather condition code
 * @returns {'none' | 'low' | 'moderate' | 'high' | 'severe'}
 */
export function getWeatherSeverity(code) {
  return WMO_CODES[code]?.severity ?? 'none';
}

/**
 * Check if a WMO weather code warrants an alert (high or severe severity).
 * @param {number} code - WMO weather condition code
 * @returns {boolean}
 */
export function isAlertWorthy(code) {
  const severity = getWeatherSeverity(code);
  return severity === 'high' || severity === 'severe';
}

export default WMO_CODES;
