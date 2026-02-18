import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  getWeatherDescription,
  getWeatherIcon,
  getWeatherSeverity,
  isAlertWorthy,
} from '../../src/utils/weatherCodes.js';

describe('getWeatherDescription', () => {
  it('returns description for known code', () => {
    assert.equal(getWeatherDescription(0), 'Clear sky');
    assert.equal(getWeatherDescription(95), 'Thunderstorm: Slight or moderate');
  });

  it('returns Unknown for unknown code', () => {
    assert.equal(getWeatherDescription(999), 'Unknown');
    assert.equal(getWeatherDescription(undefined), 'Unknown');
  });
});

describe('getWeatherIcon', () => {
  it('returns day icon when isDay is true', () => {
    assert.equal(getWeatherIcon(0, true), 'clear_day');
    assert.equal(getWeatherIcon(2, true), 'partly_cloudy_day');
  });

  it('returns night icon when isDay is false', () => {
    assert.equal(getWeatherIcon(0, false), 'clear_night');
    assert.equal(getWeatherIcon(2, false), 'partly_cloudy_night');
  });

  it('returns same icon for codes without day/night variant', () => {
    assert.equal(getWeatherIcon(3, true), 'overcast');
    assert.equal(getWeatherIcon(3, false), 'overcast');
  });

  it('returns unknown for invalid code', () => {
    assert.equal(getWeatherIcon(999, true), 'unknown');
  });
});

describe('getWeatherSeverity', () => {
  it('returns none for clear weather', () => {
    assert.equal(getWeatherSeverity(0), 'none');
    assert.equal(getWeatherSeverity(1), 'none');
  });

  it('returns low for fog', () => {
    assert.equal(getWeatherSeverity(45), 'low');
  });

  it('returns moderate for freezing drizzle', () => {
    assert.equal(getWeatherSeverity(56), 'moderate');
  });

  it('returns high for thunderstorm', () => {
    assert.equal(getWeatherSeverity(95), 'high');
  });

  it('returns severe for thunderstorm with hail', () => {
    assert.equal(getWeatherSeverity(96), 'severe');
    assert.equal(getWeatherSeverity(99), 'severe');
  });

  it('returns none for unknown code', () => {
    assert.equal(getWeatherSeverity(999), 'none');
  });
});

describe('isAlertWorthy', () => {
  it('returns true for high severity codes', () => {
    assert.equal(isAlertWorthy(95), true);
    assert.equal(isAlertWorthy(67), true);
  });

  it('returns true for severe codes', () => {
    assert.equal(isAlertWorthy(96), true);
    assert.equal(isAlertWorthy(99), true);
  });

  it('returns false for low/moderate/none severity', () => {
    assert.equal(isAlertWorthy(0), false);
    assert.equal(isAlertWorthy(45), false);
    assert.equal(isAlertWorthy(56), false);
  });
});
