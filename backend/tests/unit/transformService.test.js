import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  transformForecast,
  transformAirQuality,
  transformGeocodingResults,
  transformHistorical,
} from '../../src/services/transformService.js';
import {
  MOCK_FORECAST_RAW,
  MOCK_AIR_QUALITY_RAW,
  MOCK_GEOCODING_RAW,
  MOCK_HISTORICAL_RAW,
} from '../helpers/mockData.js';

describe('transformForecast', () => {
  it('returns location info', () => {
    const result = transformForecast(MOCK_FORECAST_RAW, 'metric');
    assert.equal(result.location.latitude, 52.52);
    assert.equal(result.location.longitude, 13.41);
    assert.equal(result.location.timezone, 'Europe/Berlin');
  });

  it('transforms current weather with metric units', () => {
    const result = transformForecast(MOCK_FORECAST_RAW, 'metric');
    assert.equal(result.current.temperature, 5.2);
    assert.equal(result.current.windSpeed, 12.5);
    assert.equal(result.current.weatherDescription, 'Partly cloudy');
    assert.equal(result.current.isDay, true);
  });

  it('transforms current weather with imperial units', () => {
    const result = transformForecast(MOCK_FORECAST_RAW, 'imperial');
    assert.equal(result.current.temperature, 41.4);
    assert.equal(result.units.temperature, '°F');
    assert.equal(result.units.windSpeed, 'mph');
  });

  it('enriches hourly weather codes with descriptions and icons', () => {
    const result = transformForecast(MOCK_FORECAST_RAW, 'metric');
    assert.equal(result.hourly.weatherDescription.length, 2);
    assert.equal(result.hourly.weatherIcon.length, 2);
    assert.equal(result.hourly.weatherDescription[0], 'Mainly clear');
  });

  it('enriches daily weather codes', () => {
    const result = transformForecast(MOCK_FORECAST_RAW, 'metric');
    assert.equal(result.daily.weatherDescription[0], 'Rain: Slight');
  });

  it('includes minutely_15 data when available', () => {
    const result = transformForecast(MOCK_FORECAST_RAW, 'metric');
    assert.equal(result.minutely15.available, true);
    assert.equal(result.minutely15.time.length, 2);
  });

  it('marks minutely_15 as unavailable when missing', () => {
    const raw = { ...MOCK_FORECAST_RAW, minutely_15: {} };
    const result = transformForecast(raw, 'metric');
    assert.equal(result.minutely15.available, false);
  });

  it('returns correct unit labels', () => {
    const metric = transformForecast(MOCK_FORECAST_RAW, 'metric');
    assert.equal(metric.units.temperature, '°C');

    const imperial = transformForecast(MOCK_FORECAST_RAW, 'imperial');
    assert.equal(imperial.units.temperature, '°F');
  });
});

describe('transformAirQuality', () => {
  it('returns current AQI data', () => {
    const result = transformAirQuality(MOCK_AIR_QUALITY_RAW);
    assert.equal(result.current.aqi, 42);
    assert.equal(result.current.aqiCategory, 'Good');
    assert.equal(result.current.pm25, 8.2);
  });

  it('categorizes AQI correctly', () => {
    const good = transformAirQuality({
      ...MOCK_AIR_QUALITY_RAW,
      current: { ...MOCK_AIR_QUALITY_RAW.current, us_aqi: 50 },
    });
    assert.equal(good.current.aqiCategory, 'Good');

    const moderate = transformAirQuality({
      ...MOCK_AIR_QUALITY_RAW,
      current: { ...MOCK_AIR_QUALITY_RAW.current, us_aqi: 75 },
    });
    assert.equal(moderate.current.aqiCategory, 'Moderate');

    const unhealthy = transformAirQuality({
      ...MOCK_AIR_QUALITY_RAW,
      current: { ...MOCK_AIR_QUALITY_RAW.current, us_aqi: 175 },
    });
    assert.equal(unhealthy.current.aqiCategory, 'Unhealthy');

    const hazardous = transformAirQuality({
      ...MOCK_AIR_QUALITY_RAW,
      current: { ...MOCK_AIR_QUALITY_RAW.current, us_aqi: 350 },
    });
    assert.equal(hazardous.current.aqiCategory, 'Hazardous');
  });

  it('includes hourly data', () => {
    const result = transformAirQuality(MOCK_AIR_QUALITY_RAW);
    assert.equal(result.hourly.aqi.length, 2);
  });

  it('marks pollen as unavailable when no data', () => {
    const result = transformAirQuality(MOCK_AIR_QUALITY_RAW);
    assert.equal(result.pollen.available, false);
  });

  it('marks pollen as available when data exists', () => {
    const rawWithPollen = {
      ...MOCK_AIR_QUALITY_RAW,
      hourly: { ...MOCK_AIR_QUALITY_RAW.hourly, grass_pollen: [10, 15] },
    };
    const result = transformAirQuality(rawWithPollen);
    assert.equal(result.pollen.available, true);
  });
});

describe('transformGeocodingResults', () => {
  it('transforms results array', () => {
    const result = transformGeocodingResults(MOCK_GEOCODING_RAW);
    assert.equal(result.results.length, 1);
    assert.equal(result.results[0].name, 'Berlin');
    assert.equal(result.results[0].country, 'Germany');
    assert.equal(result.results[0].latitude, 52.52437);
  });

  it('returns empty array when no results', () => {
    const result = transformGeocodingResults({});
    assert.deepEqual(result.results, []);
  });
});

describe('transformHistorical', () => {
  it('returns location and daily data', () => {
    const result = transformHistorical(MOCK_HISTORICAL_RAW, 'metric');
    assert.equal(result.location.latitude, 52.52);
    assert.equal(result.daily.temperatureMax[0], 4.5);
    assert.equal(result.daily.weatherDescription[0], 'Rain: Moderate');
  });

  it('converts to imperial units', () => {
    const result = transformHistorical(MOCK_HISTORICAL_RAW, 'imperial');
    assert.equal(result.units.temperature, '°F');
    assert.ok(result.daily.temperatureMax[0] !== 4.5);
  });

  it('includes hourly data with enriched weather codes', () => {
    const result = transformHistorical(MOCK_HISTORICAL_RAW, 'metric');
    assert.equal(result.hourly.weatherDescription.length, 2);
    assert.equal(result.hourly.weatherDescription[0], 'Rain: Slight');
  });
});
