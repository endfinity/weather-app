const THRESHOLDS = {
  EXTREME_HEAT: 40,
  EXTREME_COLD: -20,
  HIGH_WIND: 80,
  STORM_WIND: 100,
  HEAVY_PRECIP_MM: 20,
  VERY_HEAVY_PRECIP_MM: 50,
  HIGH_UV: 8,
  EXTREME_UV: 11,
  POOR_AQ_US_AQI: 151,
  HAZARD_AQ_US_AQI: 301,
  THUNDERSTORM_CODES: [95, 96, 99],
  FOG_CODES: [45, 48],
  FREEZING_CODES: [56, 57, 66, 67],
  SNOW_CODES: [71, 73, 75, 77, 85, 86],
};

const WMO_DESCRIPTIONS = {
  45: 'Fog',
  48: 'Depositing rime fog',
  56: 'Freezing drizzle (light)',
  57: 'Freezing drizzle (dense)',
  66: 'Freezing rain (light)',
  67: 'Freezing rain (heavy)',
  71: 'Snow fall (slight)',
  73: 'Snow fall (moderate)',
  75: 'Snow fall (heavy)',
  77: 'Snow grains',
  85: 'Snow showers (slight)',
  86: 'Snow showers (heavy)',
  95: 'Thunderstorm',
  96: 'Thunderstorm with slight hail',
  99: 'Thunderstorm with heavy hail',
};

/**
 * Analyze current and forecast weather data and return any active alerts.
 * @param {object} weatherData - Raw weather data with optional current and daily sections
 * @param {string} locationName - Human-readable location label for alert messages
 * @returns {object[]} Array of alert objects (may be empty)
 */
export function analyzeWeatherForAlerts(weatherData, locationName) {
  const alerts = [];
  const current = weatherData?.current;
  const daily = weatherData?.daily;

  if (!current && !daily) return alerts;

  if (current) {
    analyzeCurrentConditions(current, locationName, alerts);
  }

  if (daily) {
    analyzeForecastConditions(daily, locationName, alerts);
  }

  return alerts;
}

function analyzeCurrentConditions(current, locationName, alerts) {
  const temp = current.temperature_2m;
  if (temp !== undefined) {
    if (temp >= THRESHOLDS.EXTREME_HEAT) {
      alerts.push(
        buildAlert(
          'extreme_heat',
          'extreme',
          'Extreme Heat Warning',
          `Temperature has reached ${temp} C. Stay hydrated and avoid prolonged sun exposure.`,
          locationName,
        ),
      );
    } else if (temp <= THRESHOLDS.EXTREME_COLD) {
      alerts.push(
        buildAlert(
          'extreme_cold',
          'extreme',
          'Extreme Cold Warning',
          `Temperature has dropped to ${temp} C. Risk of frostbite and hypothermia.`,
          locationName,
        ),
      );
    }
  }

  const windSpeed = current.wind_speed_10m;
  if (windSpeed !== undefined) {
    if (windSpeed >= THRESHOLDS.STORM_WIND) {
      alerts.push(
        buildAlert(
          'storm_wind',
          'extreme',
          'Storm Wind Alert',
          `Wind speeds of ${windSpeed} km/h detected. Seek shelter immediately.`,
          locationName,
        ),
      );
    } else if (windSpeed >= THRESHOLDS.HIGH_WIND) {
      alerts.push(
        buildAlert(
          'high_wind',
          'severe',
          'High Wind Warning',
          `Wind speeds of ${windSpeed} km/h. Secure loose objects and avoid outdoor activities.`,
          locationName,
        ),
      );
    }
  }

  const weatherCode = current.weather_code;
  if (weatherCode !== undefined) {
    if (THRESHOLDS.THUNDERSTORM_CODES.includes(weatherCode)) {
      const desc = WMO_DESCRIPTIONS[weatherCode] || 'Thunderstorm';
      alerts.push(
        buildAlert(
          'thunderstorm',
          'severe',
          'Thunderstorm Alert',
          `${desc} detected in your area. Seek indoor shelter.`,
          locationName,
        ),
      );
    }
    if (THRESHOLDS.FREEZING_CODES.includes(weatherCode)) {
      const desc = WMO_DESCRIPTIONS[weatherCode] || 'Freezing precipitation';
      alerts.push(
        buildAlert(
          'freezing_precip',
          'severe',
          'Freezing Precipitation',
          `${desc}. Roads may be hazardous.`,
          locationName,
        ),
      );
    }
    if (THRESHOLDS.FOG_CODES.includes(weatherCode)) {
      const desc = WMO_DESCRIPTIONS[weatherCode] || 'Fog';
      alerts.push(buildAlert('fog', 'moderate', 'Fog Advisory', `${desc}. Reduced visibility expected.`, locationName));
    }
  }
}

function analyzeForecastConditions(daily, locationName, alerts) {
  if (!daily.time || !Array.isArray(daily.time)) return;

  const tomorrow = daily.time[1];
  if (!tomorrow) return;

  const tomorrowIdx = 1;

  const precipSum = daily.precipitation_sum?.[tomorrowIdx];
  if (precipSum !== undefined) {
    if (precipSum >= THRESHOLDS.VERY_HEAVY_PRECIP_MM) {
      alerts.push(
        buildAlert(
          'very_heavy_precip',
          'severe',
          'Heavy Precipitation Warning',
          `${precipSum} mm of precipitation expected tomorrow. Flooding risk.`,
          locationName,
        ),
      );
    } else if (precipSum >= THRESHOLDS.HEAVY_PRECIP_MM) {
      alerts.push(
        buildAlert(
          'heavy_precip',
          'moderate',
          'Precipitation Alert',
          `${precipSum} mm of precipitation expected tomorrow.`,
          locationName,
        ),
      );
    }
  }

  const uvMax = daily.uv_index_max?.[tomorrowIdx];
  if (uvMax !== undefined) {
    if (uvMax >= THRESHOLDS.EXTREME_UV) {
      alerts.push(
        buildAlert(
          'extreme_uv',
          'severe',
          'Extreme UV Index',
          `UV index of ${uvMax} expected tomorrow. Avoid outdoor exposure.`,
          locationName,
        ),
      );
    } else if (uvMax >= THRESHOLDS.HIGH_UV) {
      alerts.push(
        buildAlert(
          'high_uv',
          'moderate',
          'High UV Index',
          `UV index of ${uvMax} expected tomorrow. Use sun protection.`,
          locationName,
        ),
      );
    }
  }

  const maxTemp = daily.temperature_2m_max?.[tomorrowIdx];
  if (maxTemp !== undefined && maxTemp >= THRESHOLDS.EXTREME_HEAT) {
    alerts.push(
      buildAlert(
        'heat_forecast',
        'severe',
        'Heat Advisory',
        `Maximum temperature of ${maxTemp} C expected tomorrow.`,
        locationName,
      ),
    );
  }

  const minTemp = daily.temperature_2m_min?.[tomorrowIdx];
  if (minTemp !== undefined && minTemp <= THRESHOLDS.EXTREME_COLD) {
    alerts.push(
      buildAlert(
        'cold_forecast',
        'severe',
        'Cold Advisory',
        `Minimum temperature of ${minTemp} C expected tomorrow.`,
        locationName,
      ),
    );
  }
}

/**
 * Analyze air quality data and return alerts when AQI exceeds thresholds.
 * @param {object} aqData - Air quality data with current.us_aqi
 * @param {string} locationName - Human-readable location label for alert messages
 * @returns {object[]} Array of alert objects (may be empty)
 */
export function analyzeAirQualityForAlerts(aqData, locationName) {
  const alerts = [];
  const aqi = aqData?.current?.us_aqi;

  if (aqi === undefined) return alerts;

  if (aqi >= THRESHOLDS.HAZARD_AQ_US_AQI) {
    alerts.push(
      buildAlert(
        'hazardous_aqi',
        'extreme',
        'Hazardous Air Quality',
        `US AQI has reached ${aqi}. Avoid all outdoor activity. Keep windows closed.`,
        locationName,
      ),
    );
  } else if (aqi >= THRESHOLDS.POOR_AQ_US_AQI) {
    alerts.push(
      buildAlert(
        'poor_aqi',
        'severe',
        'Poor Air Quality',
        `US AQI is ${aqi}. Sensitive groups should limit outdoor exposure.`,
        locationName,
      ),
    );
  }

  return alerts;
}

function buildAlert(type, severity, title, description, locationName) {
  return {
    id: `${type}_${locationName.replace(/\s+/g, '_').toLowerCase()}_${Date.now()}`,
    type,
    severity,
    title,
    description,
    location_name: locationName,
    start_time: new Date().toISOString(),
    end_time: null,
    issued_at: new Date().toISOString(),
  };
}
