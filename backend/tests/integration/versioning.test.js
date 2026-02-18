import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('API versioning', () => {
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

  describe('unversioned /api/*', () => {
    it('returns v1-style response without API-Version header', async () => {
      const res = await get('/api/weather?lat=52.52&lon=13.41');
      const body = res.json();
      assert.equal(res.status, 200);
      assert.equal(body.success, true);
      assert.ok(body.data);
      assert.equal(body.cached, false);
      assert.equal(body.meta, undefined);
      assert.equal(res.headers['api-version'], undefined);
    });
  });

  describe('explicit /api/v1/*', () => {
    it('returns v1-style response with API-Version: v1 header', async () => {
      const res = await get('/api/v1/weather?lat=52.52&lon=13.41');
      const body = res.json();
      assert.equal(res.status, 200);
      assert.equal(body.success, true);
      assert.ok(body.data);
      assert.equal(body.cached, false);
      assert.equal(body.meta, undefined);
      assert.equal(res.headers['api-version'], 'v1');
    });

    it('returns v1-style combined response', async () => {
      const res = await get('/api/v1/weather/combined?lat=52.52&lon=13.41');
      const body = res.json();
      assert.equal(res.status, 200);
      assert.ok(body.data.weather);
      assert.ok(body.data.airQuality);
      assert.equal(body.meta, undefined);
      assert.equal(res.headers['api-version'], 'v1');
    });

    it('returns v1-style error without meta', async () => {
      const res = await get('/api/v1/weather');
      const body = res.json();
      assert.equal(res.status, 400);
      assert.equal(body.success, false);
      assert.ok(body.error);
      assert.equal(body.meta, undefined);
    });
  });

  describe('/api/v2/*', () => {
    it('returns v2 envelope with meta and API-Version: v2 header', async () => {
      const res = await get('/api/v2/weather?lat=52.52&lon=13.41');
      const body = res.json();
      assert.equal(res.status, 200);
      assert.equal(body.success, true);
      assert.ok(body.data);
      assert.ok(body.meta);
      assert.equal(body.meta.version, 'v2');
      assert.ok(body.meta.requestId);
      assert.ok(body.meta.timestamp);
      assert.equal(body.meta.cached, false);
      assert.equal(body.cached, undefined);
      assert.equal(res.headers['api-version'], 'v2');
    });

    it('returns v2 envelope for combined endpoint', async () => {
      const res = await get('/api/v2/weather/combined?lat=52.52&lon=13.41');
      const body = res.json();
      assert.equal(res.status, 200);
      assert.ok(body.data.weather);
      assert.ok(body.data.airQuality);
      assert.ok(body.meta);
      assert.equal(body.meta.version, 'v2');
    });

    it('returns v2 error envelope with meta', async () => {
      const res = await get('/api/v2/weather');
      const body = res.json();
      assert.equal(res.status, 400);
      assert.equal(body.success, false);
      assert.ok(body.error.code);
      assert.ok(body.meta);
      assert.equal(body.meta.version, 'v2');
      assert.ok(body.meta.requestId);
    });

    it('returns v2 envelope for air-quality', async () => {
      const res = await get('/api/v2/air-quality?lat=52.52&lon=13.41');
      const body = res.json();
      assert.equal(res.status, 200);
      assert.ok(body.meta);
      assert.equal(body.meta.version, 'v2');
    });

    it('returns v2 envelope for geocoding', async () => {
      const res = await get('/api/v2/geocoding/search?query=Berlin');
      const body = res.json();
      assert.equal(res.status, 200);
      assert.ok(body.meta);
      assert.equal(body.meta.version, 'v2');
    });

    it('v2 404 includes meta', async () => {
      const res = await get('/api/v2/nonexistent');
      const body = res.json();
      assert.equal(res.status, 404);
      assert.equal(body.success, false);
      assert.ok(body.meta);
      assert.equal(body.meta.version, 'v2');
    });
  });

  describe('data equivalence', () => {
    it('unversioned, v1, and v2 return the same weather data', async () => {
      const [r0, r1, r2] = await Promise.all([
        get('/api/weather?lat=52.52&lon=13.41'),
        get('/api/v1/weather?lat=52.52&lon=13.41'),
        get('/api/v2/weather?lat=52.52&lon=13.41'),
      ]);
      const d0 = r0.json().data;
      const d1 = r1.json().data;
      const d2 = r2.json().data;
      assert.deepStrictEqual(d0, d1);
      assert.deepStrictEqual(d1, d2);
    });
  });
});
