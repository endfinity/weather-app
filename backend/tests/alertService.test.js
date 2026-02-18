import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { analyzeWeatherForAlerts, analyzeAirQualityForAlerts } from '../src/services/alertService.js';

describe('analyzeWeatherForAlerts', () => {
  it('returns empty array when no data', () => {
    assert.deepStrictEqual(analyzeWeatherForAlerts(null, 'City'), []);
    assert.deepStrictEqual(analyzeWeatherForAlerts({}, 'City'), []);
  });

  it('detects extreme heat', () => {
    const data = { current: { temperature_2m: 42 } };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    assert.ok(alerts.length >= 1);
    const heat = alerts.find((a) => a.type === 'extreme_heat');
    assert.ok(heat);
    assert.strictEqual(heat.severity, 'extreme');
    assert.ok(heat.description.includes('42'));
  });

  it('detects extreme cold', () => {
    const data = { current: { temperature_2m: -25 } };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const cold = alerts.find((a) => a.type === 'extreme_cold');
    assert.ok(cold);
    assert.strictEqual(cold.severity, 'extreme');
  });

  it('does not alert for normal temperature', () => {
    const data = { current: { temperature_2m: 22 } };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    assert.strictEqual(alerts.length, 0);
  });

  it('detects storm wind', () => {
    const data = { current: { wind_speed_10m: 110 } };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const storm = alerts.find((a) => a.type === 'storm_wind');
    assert.ok(storm);
    assert.strictEqual(storm.severity, 'extreme');
  });

  it('detects high wind', () => {
    const data = { current: { wind_speed_10m: 85 } };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const wind = alerts.find((a) => a.type === 'high_wind');
    assert.ok(wind);
    assert.strictEqual(wind.severity, 'severe');
  });

  it('detects thunderstorm codes', () => {
    for (const code of [95, 96, 99]) {
      const data = { current: { weather_code: code } };
      const alerts = analyzeWeatherForAlerts(data, 'City');
      const ts = alerts.find((a) => a.type === 'thunderstorm');
      assert.ok(ts, `Should detect thunderstorm for code ${code}`);
    }
  });

  it('detects freezing precipitation codes', () => {
    for (const code of [56, 57, 66, 67]) {
      const data = { current: { weather_code: code } };
      const alerts = analyzeWeatherForAlerts(data, 'City');
      const fp = alerts.find((a) => a.type === 'freezing_precip');
      assert.ok(fp, `Should detect freezing precip for code ${code}`);
    }
  });

  it('detects fog codes', () => {
    for (const code of [45, 48]) {
      const data = { current: { weather_code: code } };
      const alerts = analyzeWeatherForAlerts(data, 'City');
      const fog = alerts.find((a) => a.type === 'fog');
      assert.ok(fog, `Should detect fog for code ${code}`);
      assert.strictEqual(fog.severity, 'moderate');
    }
  });

  it('detects heavy precipitation forecast', () => {
    const data = {
      daily: {
        time: ['2025-01-01', '2025-01-02'],
        precipitation_sum: [0, 25],
      },
    };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const precip = alerts.find((a) => a.type === 'heavy_precip');
    assert.ok(precip);
    assert.strictEqual(precip.severity, 'moderate');
  });

  it('detects very heavy precipitation forecast', () => {
    const data = {
      daily: {
        time: ['2025-01-01', '2025-01-02'],
        precipitation_sum: [0, 55],
      },
    };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const precip = alerts.find((a) => a.type === 'very_heavy_precip');
    assert.ok(precip);
    assert.strictEqual(precip.severity, 'severe');
  });

  it('detects extreme UV forecast', () => {
    const data = {
      daily: {
        time: ['2025-01-01', '2025-01-02'],
        uv_index_max: [5, 12],
      },
    };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const uv = alerts.find((a) => a.type === 'extreme_uv');
    assert.ok(uv);
  });

  it('detects high UV forecast', () => {
    const data = {
      daily: {
        time: ['2025-01-01', '2025-01-02'],
        uv_index_max: [5, 9],
      },
    };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const uv = alerts.find((a) => a.type === 'high_uv');
    assert.ok(uv);
    assert.strictEqual(uv.severity, 'moderate');
  });

  it('detects heat forecast for tomorrow', () => {
    const data = {
      daily: {
        time: ['2025-01-01', '2025-01-02'],
        temperature_2m_max: [30, 42],
      },
    };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const heat = alerts.find((a) => a.type === 'heat_forecast');
    assert.ok(heat);
  });

  it('detects cold forecast for tomorrow', () => {
    const data = {
      daily: {
        time: ['2025-01-01', '2025-01-02'],
        temperature_2m_min: [-5, -25],
      },
    };
    const alerts = analyzeWeatherForAlerts(data, 'City');
    const cold = alerts.find((a) => a.type === 'cold_forecast');
    assert.ok(cold);
  });

  it('alert has correct structure', () => {
    const data = { current: { temperature_2m: 42 } };
    const alerts = analyzeWeatherForAlerts(data, 'Test City');
    const alert = alerts[0];
    assert.ok(alert.id);
    assert.ok(alert.type);
    assert.ok(alert.severity);
    assert.ok(alert.title);
    assert.ok(alert.description);
    assert.strictEqual(alert.location_name, 'Test City');
    assert.ok(alert.start_time);
    assert.ok(alert.issued_at);
  });
});

describe('analyzeAirQualityForAlerts', () => {
  it('returns empty array for no data', () => {
    assert.deepStrictEqual(analyzeAirQualityForAlerts(null, 'City'), []);
    assert.deepStrictEqual(analyzeAirQualityForAlerts({}, 'City'), []);
  });

  it('detects hazardous air quality', () => {
    const data = { current: { us_aqi: 350 } };
    const alerts = analyzeAirQualityForAlerts(data, 'City');
    const hazard = alerts.find((a) => a.type === 'hazardous_aqi');
    assert.ok(hazard);
    assert.strictEqual(hazard.severity, 'extreme');
  });

  it('detects poor air quality', () => {
    const data = { current: { us_aqi: 200 } };
    const alerts = analyzeAirQualityForAlerts(data, 'City');
    const poor = alerts.find((a) => a.type === 'poor_aqi');
    assert.ok(poor);
    assert.strictEqual(poor.severity, 'severe');
  });

  it('no alert for good air quality', () => {
    const data = { current: { us_aqi: 50 } };
    const alerts = analyzeAirQualityForAlerts(data, 'City');
    assert.strictEqual(alerts.length, 0);
  });

  it('no alert at boundary below threshold', () => {
    const data = { current: { us_aqi: 150 } };
    const alerts = analyzeAirQualityForAlerts(data, 'City');
    assert.strictEqual(alerts.length, 0);
  });
});
