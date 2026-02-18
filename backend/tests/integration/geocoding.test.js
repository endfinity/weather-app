import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('GET /api/geocoding/search', () => {
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

  it('returns geocoding results for valid query', async () => {
    const res = await get('/api/geocoding/search?query=Berlin');
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.ok(Array.isArray(body.data.results));
    assert.ok(body.data.results.length > 0);
    assert.equal(body.data.results[0].name, 'Berlin');
  });

  it('returns 400 for missing query', async () => {
    const res = await get('/api/geocoding/search');
    assert.equal(res.status, 400);
    assert.equal(res.json().error.code, 'INVALID_QUERY');
  });

  it('returns 400 for query shorter than 2 characters', async () => {
    const res = await get('/api/geocoding/search?query=B');
    assert.equal(res.status, 400);
    assert.equal(res.json().error.code, 'INVALID_QUERY');
  });

  it('accepts query of exactly 2 characters', async () => {
    const res = await get('/api/geocoding/search?query=NY');
    assert.equal(res.status, 200);
  });

  it('returns cached results on second request', async () => {
    await get('/api/geocoding/search?query=Berlin');
    const res2 = await get('/api/geocoding/search?query=Berlin');
    assert.equal(res2.json().cached, true);
  });

  it('respects count parameter', async () => {
    const res = await get('/api/geocoding/search?query=Berlin&count=5');
    assert.equal(res.status, 200);
  });
});
