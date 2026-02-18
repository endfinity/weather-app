import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('GET /api/weather/combined', () => {
  before(async () => {
    mock.method(globalThis, 'fetch', createMockFetch());
    await startTestServer();
  });

  after(async () => {
    mock.restoreAll();
    await stopTestServer();
  });

  beforeEach(() => {
    flushAll();
  });

  it('returns both weather and air quality data', async () => {
    const res = await get('/api/weather/combined?lat=52.52&lon=13.41');
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.ok(body.data.weather);
    assert.ok(body.data.airQuality);
    assert.ok(body.data.weather.current);
    assert.ok(body.data.airQuality.current);
  });

  it('returns 400 for missing coordinates', async () => {
    const res = await get('/api/weather/combined');
    assert.equal(res.status, 400);
    assert.equal(res.json().error.code, 'MISSING_PARAMETER');
  });

  it('supports imperial units', async () => {
    const res = await get('/api/weather/combined?lat=52.52&lon=13.41&units=imperial');
    const body = res.json();
    assert.equal(body.data.weather.units.temperature, '°F');
  });

  it('uses cache for both weather and air quality', async () => {
    const res1 = await get('/api/weather/combined?lat=52.52&lon=13.41');
    assert.equal(res1.json().cached, false);

    const res2 = await get('/api/weather/combined?lat=52.52&lon=13.41');
    assert.equal(res2.json().cached, true);
  });
});
