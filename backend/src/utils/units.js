/** @param {number | null | undefined} c @returns {number | null} */
export function celsiusToFahrenheit(c) {
  return c !== null && c !== undefined ? Math.round(((c * 9) / 5 + 32) * 10) / 10 : null;
}

/** @param {number | null | undefined} kmh @returns {number | null} */
export function kmhToMph(kmh) {
  return kmh !== null && kmh !== undefined ? Math.round(kmh * 0.621371 * 10) / 10 : null;
}

/** @param {number | null | undefined} mm @returns {number | null} */
export function mmToInch(mm) {
  return mm !== null && mm !== undefined ? Math.round(mm * 0.0393701 * 100) / 100 : null;
}

/** @param {number | null | undefined} m @returns {number | null} */
export function metersToMiles(m) {
  return m !== null && m !== undefined ? Math.round((m / 1609.344) * 10) / 10 : null;
}

/**
 * Convert a temperature value based on the selected unit system.
 * @param {number | null | undefined} value - Temperature in Celsius
 * @param {'metric' | 'imperial'} units
 * @returns {number | null}
 */
export function convertTemperature(value, units) {
  return units === 'imperial' ? celsiusToFahrenheit(value) : value;
}

/**
 * Convert a speed value based on the selected unit system.
 * @param {number | null | undefined} value - Speed in km/h
 * @param {'metric' | 'imperial'} units
 * @returns {number | null}
 */
export function convertSpeed(value, units) {
  return units === 'imperial' ? kmhToMph(value) : value;
}

/**
 * Convert a precipitation value based on the selected unit system.
 * @param {number | null | undefined} value - Precipitation in mm
 * @param {'metric' | 'imperial'} units
 * @returns {number | null}
 */
export function convertPrecipitation(value, units) {
  return units === 'imperial' ? mmToInch(value) : value;
}

/**
 * Convert a visibility value based on the selected unit system.
 * @param {number | null | undefined} value - Visibility in meters
 * @param {'metric' | 'imperial'} units
 * @returns {number | null}
 */
export function convertVisibility(value, units) {
  return units === 'imperial' ? metersToMiles(value) : value;
}

/**
 * Return human-readable unit labels for the given unit system.
 * @param {'metric' | 'imperial'} units
 * @returns {{ temperature: string, windSpeed: string, precipitation: string, pressure: string, visibility: string }}
 */
export function getUnitLabels(units) {
  if (units === 'imperial') {
    return {
      temperature: '°F',
      windSpeed: 'mph',
      precipitation: 'inch',
      pressure: 'hPa',
      visibility: 'mi',
    };
  }
  return {
    temperature: '°C',
    windSpeed: 'km/h',
    precipitation: 'mm',
    pressure: 'hPa',
    visibility: 'm',
  };
}

/**
 * Apply a converter function to every element in an array, returning nullish inputs unchanged.
 * @param {Array | *} arr
 * @param {(v: number) => number | null} converterFn
 * @returns {Array | *}
 */
export function convertArray(arr, converterFn) {
  if (!Array.isArray(arr)) return arr;
  return arr.map((v) => converterFn(v));
}
