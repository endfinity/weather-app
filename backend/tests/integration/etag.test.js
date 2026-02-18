import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get, post } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('ETag / conditional requests', () => {
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

  it('GET /api/weather returns an ETag header', async () => {
    const res = await get('/api/weather?lat=52.52&lon=13.41');
    assert.equal(res.status, 200);
    assert.ok(res.headers['etag'], 'Should include an ETag header');
    assert.ok(res.headers['etag'].startsWith('W/"'), 'Should be a weak ETag');
  });

  it('returns 304 when If-None-Match matches', async () => {
    // Warm the cache
    await get('/api/weather?lat=52.52&lon=13.41');

    // Get the cached response and its ETag
    const res1 = await get('/api/weather?lat=52.52&lon=13.41');
    const etag = res1.headers['etag'];
    assert.ok(etag);

    // Third request with matching ETag → 304
    const res2 = await get('/api/weather?lat=52.52&lon=13.41', { 'If-None-Match': etag });
    assert.equal(res2.status, 304);
    assert.equal(res2.body, '', 'Body should be empty on 304');
  });

  it('returns 200 with new ETag when If-None-Match is stale', async () => {
    const res = await get('/api/weather?lat=52.52&lon=13.41', { 'If-None-Match': 'W/"stale"' });
    assert.equal(res.status, 200);
    assert.ok(res.headers['etag']);
    assert.notEqual(res.headers['etag'], 'W/"stale"');
  });

  it('same cached data produces identical ETags across requests', async () => {
    // Warm the cache
    await get('/api/weather?lat=52.52&lon=13.41');

    // Both requests now return cached data with the same body
    const res1 = await get('/api/weather?lat=52.52&lon=13.41');
    const res2 = await get('/api/weather?lat=52.52&lon=13.41');
    assert.equal(res1.headers['etag'], res2.headers['etag']);
  });

  it('different parameters produce different ETags', async () => {
    const res1 = await get('/api/weather?lat=52.52&lon=13.41');
    const res2 = await get('/api/air-quality?lat=52.52&lon=13.41');
    assert.notEqual(res1.headers['etag'], res2.headers['etag']);
  });

  it('POST requests do not include ETag', async () => {
    const res = await post('/api/devices/register', {
      fcm_token: 'etag-test-token',
      platform: 'android',
      locations: [],
    });
    assert.equal(res.headers['etag'], undefined);
  });

  it('v1 routes include ETag', async () => {
    const res = await get('/api/v1/weather?lat=52.52&lon=13.41');
    assert.equal(res.status, 200);
    assert.ok(res.headers['etag']);
    assert.equal(res.headers['api-version'], 'v1');
  });

  it('v2 routes include ETag header', async () => {
    const res = await get('/api/v2/weather?lat=52.52&lon=13.41');
    assert.equal(res.status, 200);
    assert.ok(res.headers['etag']);
    assert.equal(res.headers['api-version'], 'v2');
  });

  it('v2 ETag differs from unversioned ETag for same data', async () => {
    const res1 = await get('/api/weather?lat=52.52&lon=13.41');
    const res2 = await get('/api/v2/weather?lat=52.52&lon=13.41');
    assert.notEqual(res1.headers['etag'], res2.headers['etag']);
  });

  it('error responses include ETag', async () => {
    const res = await get('/api/weather');
    assert.equal(res.status, 400);
    assert.ok(res.headers['etag'], 'Error responses should also get an ETag');
  });
});
