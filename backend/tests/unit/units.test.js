import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  celsiusToFahrenheit,
  kmhToMph,
  mmToInch,
  metersToMiles,
  convertTemperature,
  convertSpeed,
  convertPrecipitation,
  convertVisibility,
  getUnitLabels,
  convertArray,
} from '../../src/utils/units.js';

describe('celsiusToFahrenheit', () => {
  it('converts 0°C to 32°F', () => {
    assert.equal(celsiusToFahrenheit(0), 32);
  });

  it('converts 100°C to 212°F', () => {
    assert.equal(celsiusToFahrenheit(100), 212);
  });

  it('converts negative temperatures', () => {
    assert.equal(celsiusToFahrenheit(-40), -40);
  });

  it('returns null for null input', () => {
    assert.equal(celsiusToFahrenheit(null), null);
  });

  it('returns null for undefined input', () => {
    assert.equal(celsiusToFahrenheit(undefined), null);
  });
});

describe('kmhToMph', () => {
  it('converts km/h to mph', () => {
    const result = kmhToMph(100);
    assert.equal(result, 62.1);
  });

  it('converts 0 km/h', () => {
    assert.equal(kmhToMph(0), 0);
  });

  it('returns null for null input', () => {
    assert.equal(kmhToMph(null), null);
  });
});

describe('mmToInch', () => {
  it('converts mm to inches', () => {
    const result = mmToInch(25.4);
    assert.equal(result, 1);
  });

  it('returns null for null input', () => {
    assert.equal(mmToInch(null), null);
  });
});

describe('metersToMiles', () => {
  it('converts meters to miles', () => {
    const result = metersToMiles(1609.344);
    assert.equal(result, 1);
  });

  it('returns null for null input', () => {
    assert.equal(metersToMiles(null), null);
  });
});

describe('convertTemperature', () => {
  it('returns original value for metric', () => {
    assert.equal(convertTemperature(25, 'metric'), 25);
  });

  it('converts to Fahrenheit for imperial', () => {
    assert.equal(convertTemperature(0, 'imperial'), 32);
  });
});

describe('convertSpeed', () => {
  it('returns original value for metric', () => {
    assert.equal(convertSpeed(50, 'metric'), 50);
  });

  it('converts to mph for imperial', () => {
    assert.equal(convertSpeed(100, 'imperial'), 62.1);
  });
});

describe('convertPrecipitation', () => {
  it('returns original value for metric', () => {
    assert.equal(convertPrecipitation(5, 'metric'), 5);
  });

  it('converts to inches for imperial', () => {
    assert.equal(convertPrecipitation(25.4, 'imperial'), 1);
  });
});

describe('convertVisibility', () => {
  it('returns original value for metric', () => {
    assert.equal(convertVisibility(10000, 'metric'), 10000);
  });

  it('converts to miles for imperial', () => {
    assert.equal(convertVisibility(1609.344, 'imperial'), 1);
  });
});

describe('getUnitLabels', () => {
  it('returns metric labels by default', () => {
    const labels = getUnitLabels('metric');
    assert.equal(labels.temperature, '°C');
    assert.equal(labels.windSpeed, 'km/h');
    assert.equal(labels.precipitation, 'mm');
  });

  it('returns imperial labels', () => {
    const labels = getUnitLabels('imperial');
    assert.equal(labels.temperature, '°F');
    assert.equal(labels.windSpeed, 'mph');
    assert.equal(labels.precipitation, 'inch');
  });
});

describe('convertArray', () => {
  it('converts each element with the provided function', () => {
    const result = convertArray([0, 100], celsiusToFahrenheit);
    assert.deepEqual(result, [32, 212]);
  });

  it('returns non-array inputs unchanged', () => {
    assert.equal(convertArray(null, celsiusToFahrenheit), null);
    assert.equal(convertArray(undefined, celsiusToFahrenheit), undefined);
  });

  it('handles empty arrays', () => {
    assert.deepEqual(convertArray([], celsiusToFahrenheit), []);
  });

  it('preserves null elements in array', () => {
    const result = convertArray([0, null, 100], celsiusToFahrenheit);
    assert.deepEqual(result, [32, null, 212]);
  });
});
