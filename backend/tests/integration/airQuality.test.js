import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('GET /api/air-quality', () => {
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

  it('returns air quality data for valid coordinates', async () => {
    const res = await get('/api/air-quality?lat=52.52&lon=13.41');
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.ok(body.data.current);
    assert.ok(body.data.current.aqi);
    assert.ok(body.data.current.aqiCategory);
    assert.equal(body.cached, false);
  });

  it('returns 400 for missing coordinates', async () => {
    const res = await get('/api/air-quality');
    assert.equal(res.status, 400);
    assert.equal(res.json().error.code, 'MISSING_PARAMETER');
  });

  it('returns 400 for out-of-range latitude', async () => {
    const res = await get('/api/air-quality?lat=91&lon=0');
    assert.equal(res.status, 400);
    assert.equal(res.json().error.code, 'INVALID_COORDINATES');
  });

  it('returns cached data on second request', async () => {
    await get('/api/air-quality?lat=52.52&lon=13.41');
    const res2 = await get('/api/air-quality?lat=52.52&lon=13.41');
    assert.equal(res2.json().cached, true);
  });

  it('includes pollen data in response', async () => {
    const res = await get('/api/air-quality?lat=52.52&lon=13.41');
    const body = res.json();
    assert.ok('pollen' in body.data);
    assert.ok('available' in body.data.pollen);
  });
});
