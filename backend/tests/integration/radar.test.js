import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get, seedPremiumUser } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('GET /api/radar', () => {
  let token;

  before(async () => {
    mock.method(globalThis, 'fetch', createMockFetch());
    await startTestServer();
    token = seedPremiumUser('test-radar-device', 'test-radar-token');
  });

  after(async () => {
    mock.restoreAll();
    await stopTestServer();
  });

  beforeEach(() => {
    flushAll();
  });

  it('returns 401 without authorization', async () => {
    const res = await get('/api/radar');
    assert.equal(res.status, 401);
    assert.equal(res.json().error.code, 'UNAUTHORIZED');
  });

  it('returns 403 with invalid token', async () => {
    const res = await get('/api/radar', { Authorization: 'Bearer bad-token' });
    assert.equal(res.status, 403);
  });

  it('returns radar data with valid premium token', async () => {
    const res = await get('/api/radar', { Authorization: `Bearer ${token}` });
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.ok(body.data.generated);
    assert.ok(body.data.host);
    assert.ok(body.data.radar);
    assert.ok(body.data.radar.past.length > 0);
    assert.ok(body.data.tileUrlTemplate);
  });

  it('returns cached radar data on second request', async () => {
    await get('/api/radar', { Authorization: `Bearer ${token}` });
    const res2 = await get('/api/radar', { Authorization: `Bearer ${token}` });
    const body = res2.json();
    assert.equal(body.cached, true);
  });

  it('includes satellite and nowcast data', async () => {
    const res = await get('/api/radar', { Authorization: `Bearer ${token}` });
    const body = res.json();
    assert.ok(body.data.radar.nowcast);
    assert.ok(body.data.satellite);
    assert.ok(body.data.satellite.infrared);
  });
});
