import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, post } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';
import config from '../../src/config.js';

describe('POST /api/v2/batch/weather', () => {
  const originalMaxLocations = config.batch.maxLocations;

  before(async () => {
    mock.method(globalThis, 'fetch', createMockFetch());
    await startTestServer();
  });

  after(async () => {
    config.batch.maxLocations = originalMaxLocations;
    mock.restoreAll();
    await stopTestServer();
  });

  beforeEach(() => {
    flushAll();
    config.batch.maxLocations = originalMaxLocations;
  });

  it('returns weather data for multiple valid locations', async () => {
    const res = await post('/api/v2/batch/weather', {
      locations: [
        { lat: 52.52, lon: 13.41 },
        { lat: 40.71, lon: -74.01 },
      ],
    });
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.equal(body.data.results.length, 2);
    assert.equal(body.data.results[0].success, true);
    assert.ok(body.data.results[0].data.current);
    assert.equal(body.data.results[1].success, true);
    assert.ok(body.meta);
    assert.equal(body.meta.version, 'v2');
    assert.equal(body.meta.totalLocations, 2);
    assert.equal(body.meta.succeeded, 2);
    assert.equal(body.meta.failed, 0);
  });

  it('returns v2 envelope with meta', async () => {
    const res = await post('/api/v2/batch/weather', {
      locations: [{ lat: 52.52, lon: 13.41 }],
    });
    const body = res.json();
    assert.ok(body.meta.requestId);
    assert.ok(body.meta.timestamp);
    assert.equal(body.meta.version, 'v2');
    assert.equal(res.headers['api-version'], 'v2');
  });

  it('returns 400 for missing locations field', async () => {
    const res = await post('/api/v2/batch/weather', {});
    const body = res.json();
    assert.equal(res.status, 400);
    assert.equal(body.success, false);
    assert.equal(body.error.code, 'MISSING_PARAMETER');
  });

  it('returns 400 for empty locations array', async () => {
    const res = await post('/api/v2/batch/weather', { locations: [] });
    const body = res.json();
    assert.equal(res.status, 400);
    assert.equal(body.success, false);
    assert.equal(body.error.code, 'MISSING_PARAMETER');
  });

  it('returns 400 when exceeding max locations', async () => {
    config.batch.maxLocations = 2;
    const res = await post('/api/v2/batch/weather', {
      locations: [
        { lat: 1, lon: 1 },
        { lat: 2, lon: 2 },
        { lat: 3, lon: 3 },
      ],
    });
    const body = res.json();
    assert.equal(res.status, 400);
    assert.equal(body.success, false);
    assert.equal(body.error.code, 'BATCH_TOO_LARGE');
  });

  it('handles individual location failures gracefully', async () => {
    mock.restoreAll();
    mock.method(globalThis, 'fetch', async (url) => {
      const urlStr = url.toString();
      if (urlStr.includes('/v1/forecast')) {
        throw new Error('Network failure');
      }
      return { ok: true, status: 200, json: async () => ({}) };
    });

    const res = await post('/api/v2/batch/weather', {
      locations: [{ lat: 52.52, lon: 13.41 }],
    });
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.equal(body.data.results[0].success, false);
    assert.ok(body.data.results[0].error.code);
    assert.equal(body.meta.failed, 1);

    mock.restoreAll();
    mock.method(globalThis, 'fetch', createMockFetch());
  });

  it('supports per-location units and timezone', async () => {
    const res = await post('/api/v2/batch/weather', {
      locations: [{ lat: 52.52, lon: 13.41, units: 'imperial', timezone: 'Europe/Berlin' }],
    });
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.data.results[0].data.units.temperature, '°F');
  });

  it('caches batch results individually', async () => {
    await post('/api/v2/batch/weather', {
      locations: [{ lat: 52.52, lon: 13.41 }],
    });

    const res = await post('/api/v2/batch/weather', {
      locations: [{ lat: 52.52, lon: 13.41 }],
    });
    const body = res.json();
    assert.equal(body.data.results[0].cached, true);
    assert.ok(body.data.results[0].cachedAt);
  });
});
