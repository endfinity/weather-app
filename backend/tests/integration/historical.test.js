import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get, seedPremiumUser } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { flushAll } from '../../src/services/cacheService.js';

describe('Historical endpoints', () => {
  let token;

  before(async () => {
    mock.method(globalThis, 'fetch', createMockFetch());
    await startTestServer();
    token = seedPremiumUser('test-hist-device', 'test-hist-token');
  });

  after(async () => {
    mock.restoreAll();
    await stopTestServer();
  });

  beforeEach(() => {
    flushAll();
  });

  describe('GET /api/historical', () => {
    it('returns 401 without authorization header', async () => {
      const res = await get('/api/historical?lat=52.52&lon=13.41&start_date=2024-01-01&end_date=2024-01-07');
      assert.equal(res.status, 401);
      assert.equal(res.json().error.code, 'UNAUTHORIZED');
    });

    it('returns 403 with invalid token', async () => {
      const res = await get('/api/historical?lat=52.52&lon=13.41&start_date=2024-01-01&end_date=2024-01-07', {
        Authorization: 'Bearer invalid-token',
      });
      assert.equal(res.status, 403);
      assert.equal(res.json().error.code, 'FORBIDDEN');
    });

    it('returns historical data with valid premium token', async () => {
      const res = await get('/api/historical?lat=52.52&lon=13.41&start_date=2024-01-01&end_date=2024-01-07', {
        Authorization: `Bearer ${token}`,
      });
      const body = res.json();
      assert.equal(res.status, 200);
      assert.equal(body.success, true);
      assert.ok(body.data.location);
      assert.ok(body.data.daily);
      assert.ok(body.data.hourly);
    });

    it('returns 400 for missing date parameters', async () => {
      const res = await get('/api/historical?lat=52.52&lon=13.41', { Authorization: `Bearer ${token}` });
      assert.equal(res.status, 400);
      assert.equal(res.json().error.code, 'MISSING_PARAMETER');
    });

    it('returns 400 for invalid date format', async () => {
      const res = await get('/api/historical?lat=52.52&lon=13.41&start_date=01-01-2024&end_date=01-07-2024', {
        Authorization: `Bearer ${token}`,
      });
      assert.equal(res.status, 400);
      assert.equal(res.json().error.code, 'INVALID_DATE');
    });

    it('returns 400 when end_date is before start_date', async () => {
      const res = await get('/api/historical?lat=52.52&lon=13.41&start_date=2024-01-07&end_date=2024-01-01', {
        Authorization: `Bearer ${token}`,
      });
      assert.equal(res.status, 400);
      assert.equal(res.json().error.code, 'INVALID_DATE');
    });

    it('returns 400 when date range exceeds 366 days', async () => {
      const res = await get('/api/historical?lat=52.52&lon=13.41&start_date=2022-01-01&end_date=2024-01-01', {
        Authorization: `Bearer ${token}`,
      });
      assert.equal(res.status, 400);
      assert.equal(res.json().error.code, 'INVALID_DATE');
    });
  });

  describe('GET /api/historical/on-this-day', () => {
    it('returns 401 without auth', async () => {
      const res = await get('/api/historical/on-this-day?lat=52.52&lon=13.41');
      assert.equal(res.status, 401);
    });

    it('returns on-this-day data with valid token', async () => {
      const res = await get('/api/historical/on-this-day?lat=52.52&lon=13.41&years=3', {
        Authorization: `Bearer ${token}`,
      });
      const body = res.json();
      assert.equal(res.status, 200);
      assert.equal(body.success, true);
      assert.ok(body.data.date);
      assert.ok(Array.isArray(body.data.years));
      assert.equal(body.data.years.length, 3);
    });

    it('defaults to 5 years when not specified', async () => {
      const res = await get('/api/historical/on-this-day?lat=52.52&lon=13.41', { Authorization: `Bearer ${token}` });
      const body = res.json();
      assert.equal(body.data.years.length, 5);
    });
  });
});
