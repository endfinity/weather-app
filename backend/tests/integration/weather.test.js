import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('GET /api/weather', () => {
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

  it('returns weather data for valid coordinates', async () => {
    const res = await get('/api/weather?lat=52.52&lon=13.41');
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.ok(body.data.location);
    assert.ok(body.data.current);
    assert.ok(body.data.hourly);
    assert.ok(body.data.daily);
    assert.equal(body.cached, false);
  });

  it('returns 400 for missing coordinates', async () => {
    const res = await get('/api/weather');
    const body = res.json();
    assert.equal(res.status, 400);
    assert.equal(body.success, false);
    assert.equal(body.error.code, 'MISSING_PARAMETER');
  });

  it('returns 400 for invalid latitude', async () => {
    const res = await get('/api/weather?lat=100&lon=13.41');
    const body = res.json();
    assert.equal(res.status, 400);
    assert.equal(body.error.code, 'INVALID_COORDINATES');
  });

  it('returns 400 for invalid longitude', async () => {
    const res = await get('/api/weather?lat=52.52&lon=200');
    const body = res.json();
    assert.equal(res.status, 400);
    assert.equal(body.error.code, 'INVALID_COORDINATES');
  });

  it('returns 400 for non-numeric coordinates', async () => {
    const res = await get('/api/weather?lat=abc&lon=def');
    assert.equal(res.status, 400);
  });

  it('returns cached data on second request', async () => {
    const res1 = await get('/api/weather?lat=52.52&lon=13.41');
    assert.equal(res1.json().cached, false);

    const res2 = await get('/api/weather?lat=52.52&lon=13.41');
    const body2 = res2.json();
    assert.equal(body2.cached, true);
    assert.ok(body2.cachedAt);
  });

  it('converts units to imperial when requested', async () => {
    const res = await get('/api/weather?lat=52.52&lon=13.41&units=imperial');
    const body = res.json();
    assert.equal(body.data.units.temperature, '°F');
    assert.equal(body.data.units.windSpeed, 'mph');
  });

  it('accepts boundary coordinates', async () => {
    const res1 = await get('/api/weather?lat=-90&lon=-180');
    assert.equal(res1.status, 200);

    const res2 = await get('/api/weather?lat=90&lon=180');
    assert.equal(res2.status, 200);
  });
});
