export const MOCK_FORECAST_RAW = {
  latitude: 52.52,
  longitude: 13.41,
  elevation: 38,
  timezone: 'Europe/Berlin',
  timezone_abbreviation: 'CET',
  utc_offset_seconds: 3600,
  current: {
    time: '2025-01-15T12:00',
    temperature_2m: 5.2,
    relative_humidity_2m: 75,
    apparent_temperature: 2.1,
    is_day: 1,
    precipitation: 0,
    rain: 0,
    showers: 0,
    snowfall: 0,
    weather_code: 2,
    cloud_cover: 40,
    pressure_msl: 1015.5,
    surface_pressure: 1011.2,
    wind_speed_10m: 12.5,
    wind_direction_10m: 220,
    wind_gusts_10m: 25.0,
  },
  hourly: {
    time: ['2025-01-15T00:00', '2025-01-15T01:00'],
    temperature_2m: [3.1, 2.8],
    relative_humidity_2m: [80, 82],
    dew_point_2m: [0.1, -0.2],
    apparent_temperature: [0.5, 0.2],
    precipitation_probability: [10, 15],
    precipitation: [0, 0],
    rain: [0, 0],
    showers: [0, 0],
    snowfall: [0, 0],
    weather_code: [1, 2],
    cloud_cover: [30, 45],
    visibility: [20000, 18000],
    wind_speed_10m: [10.5, 11.0],
    wind_direction_10m: [200, 210],
    wind_gusts_10m: [20.0, 22.0],
    uv_index: [0, 0],
    pressure_msl: [1015.0, 1014.8],
    is_day: [0, 0],
  },
  daily: {
    time: ['2025-01-15'],
    temperature_2m_max: [6.5],
    temperature_2m_min: [1.2],
    apparent_temperature_max: [3.8],
    apparent_temperature_min: [-1.5],
    precipitation_sum: [0.5],
    precipitation_probability_max: [30],
    rain_sum: [0.5],
    snowfall_sum: [0],
    weather_code: [61],
    sunrise: ['2025-01-15T07:45'],
    sunset: ['2025-01-15T16:15'],
    sunshine_duration: [12000],
    daylight_duration: [30600],
    uv_index_max: [2.1],
    wind_speed_10m_max: [18.5],
    wind_gusts_10m_max: [30.0],
    wind_direction_10m_dominant: [220],
    precipitation_hours: [2],
  },
  minutely_15: {
    time: ['2025-01-15T12:00', '2025-01-15T12:15'],
    precipitation: [0, 0],
    rain: [0, 0],
    snowfall: [0, 0],
    weather_code: [2, 2],
  },
};

export const MOCK_AIR_QUALITY_RAW = {
  current: {
    time: '2025-01-15T12:00',
    us_aqi: 42,
    pm10: 18.5,
    pm2_5: 8.2,
    carbon_monoxide: 220,
    nitrogen_dioxide: 15.3,
    sulphur_dioxide: 5.1,
    ozone: 55.0,
    uv_index: 2.0,
    uv_index_clear_sky: 2.5,
  },
  hourly: {
    time: ['2025-01-15T00:00', '2025-01-15T01:00'],
    us_aqi: [38, 40],
    pm10: [15.0, 16.5],
    pm2_5: [7.0, 7.5],
    carbon_monoxide: [200, 210],
    nitrogen_dioxide: [12.0, 13.5],
    sulphur_dioxide: [4.0, 4.5],
    ozone: [50.0, 52.0],
    uv_index: [0, 0],
    uv_index_clear_sky: [0, 0],
    alder_pollen: [null, null],
    birch_pollen: [null, null],
    grass_pollen: [null, null],
    mugwort_pollen: [null, null],
    olive_pollen: [null, null],
    ragweed_pollen: [null, null],
  },
};

export const MOCK_GEOCODING_RAW = {
  results: [
    {
      id: 2950159,
      name: 'Berlin',
      latitude: 52.52437,
      longitude: 13.41053,
      elevation: 74,
      timezone: 'Europe/Berlin',
      country: 'Germany',
      country_code: 'DE',
      admin1: 'Land Berlin',
      admin2: null,
      population: 3644826,
      feature_code: 'PPLC',
    },
  ],
};

export const MOCK_HISTORICAL_RAW = {
  latitude: 52.52,
  longitude: 13.41,
  elevation: 38,
  timezone: 'Europe/Berlin',
  timezone_abbreviation: 'CET',
  utc_offset_seconds: 3600,
  daily: {
    time: ['2024-01-15'],
    temperature_2m_max: [4.5],
    temperature_2m_min: [-1.2],
    apparent_temperature_max: [1.8],
    apparent_temperature_min: [-4.5],
    precipitation_sum: [2.1],
    rain_sum: [2.1],
    snowfall_sum: [0],
    weather_code: [63],
    sunrise: ['2024-01-15T08:00'],
    sunset: ['2024-01-15T16:00'],
    sunshine_duration: [10800],
    wind_speed_10m_max: [22.0],
    wind_gusts_10m_max: [35.0],
    wind_direction_10m_dominant: [250],
  },
  hourly: {
    time: ['2024-01-15T00:00', '2024-01-15T01:00'],
    temperature_2m: [1.0, 0.5],
    relative_humidity_2m: [85, 87],
    apparent_temperature: [-2.0, -2.5],
    precipitation: [0.1, 0.2],
    rain: [0.1, 0.2],
    snowfall: [0, 0],
    weather_code: [61, 63],
    cloud_cover: [80, 85],
    wind_speed_10m: [15.0, 16.0],
    wind_direction_10m: [240, 245],
    pressure_msl: [1010.0, 1009.5],
  },
};

export const MOCK_RAINVIEWER_RAW = {
  generated: 1705312800,
  host: 'https://tilecache.rainviewer.com',
  radar: {
    past: [
      { time: 1705312200, path: '/v2/radar/past/1705312200' },
      { time: 1705312500, path: '/v2/radar/past/1705312500' },
    ],
    nowcast: [{ time: 1705313100, path: '/v2/radar/nowcast/1705313100' }],
  },
  satellite: {
    infrared: [{ time: 1705312200, path: '/v2/satellite/infrared/1705312200' }],
  },
};

export function createMockResponse(data, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => data,
    text: async () => JSON.stringify(data),
  };
}

export function createMockFetch(overrides = {}) {
  return async function mockFetch(url) {
    const urlStr = url.toString();

    if (urlStr.includes('/v1/forecast')) {
      return createMockResponse(overrides.forecast ?? MOCK_FORECAST_RAW);
    }
    if (urlStr.includes('/air-quality')) {
      return createMockResponse(overrides.airQuality ?? MOCK_AIR_QUALITY_RAW);
    }
    if (urlStr.includes('/v1/search')) {
      return createMockResponse(overrides.geocoding ?? MOCK_GEOCODING_RAW);
    }
    if (urlStr.includes('/archive')) {
      return createMockResponse(overrides.historical ?? MOCK_HISTORICAL_RAW);
    }
    if (urlStr.includes('rainviewer')) {
      return createMockResponse(overrides.rainviewer ?? MOCK_RAINVIEWER_RAW);
    }

    return createMockResponse({ error: `Unmocked URL: ${urlStr}` }, 500);
  };
}
