import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get, post, del } from '../helpers/testApp.js';
import { getDb } from '../../src/db/database.js';
import { resetRateLimiter } from '../../src/middleware/rateLimiter.js';

describe('API Keys & Tiered Rate Limiting', () => {
  before(async () => {
    await startTestServer();
  });

  after(async () => {
    const db = getDb();
    db.prepare("DELETE FROM api_keys WHERE name LIKE 'test-%'").run();
    await stopTestServer();
  });

  describe('POST /api/api-keys', () => {
    it('creates a standard API key', async () => {
      const res = await post('/api/api-keys', { name: 'test-standard-key' });
      assert.strictEqual(res.status, 201);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok(body.data.key.startsWith('csk_std_'));
      assert.strictEqual(body.data.name, 'test-standard-key');
      assert.strictEqual(body.data.tier, 'standard');
      assert.ok(body.data.rateLimit);
      assert.strictEqual(body.data.rateLimit.tier, 'standard');
    });

    it('creates a premium API key', async () => {
      const res = await post('/api/api-keys', { name: 'test-premium-key', tier: 'premium' });
      assert.strictEqual(res.status, 201);
      const body = res.json();
      assert.ok(body.data.key.startsWith('csk_prm_'));
      assert.strictEqual(body.data.tier, 'premium');
      assert.ok(body.data.rateLimit.maxRequests > 0);
    });

    it('returns 400 for missing name', async () => {
      const res = await post('/api/api-keys', {});
      assert.strictEqual(res.status, 400);
    });

    it('returns 400 for invalid tier', async () => {
      const res = await post('/api/api-keys', { name: 'test-bad-tier', tier: 'superadmin' });
      assert.strictEqual(res.status, 400);
    });
  });

  describe('GET /api/api-keys', () => {
    it('lists all active API keys', async () => {
      const res = await get('/api/api-keys');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok(Array.isArray(body.data.keys));
      assert.ok(body.data.keys.length >= 2);
      assert.ok(body.data.tiers);
      assert.ok(body.data.tiers.anonymous > 0);
      assert.ok(body.data.tiers.standard > body.data.tiers.anonymous);
      assert.ok(body.data.tiers.premium > body.data.tiers.standard);
    });

    it('filters by tier', async () => {
      const res = await get('/api/api-keys?tier=premium');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.ok(body.data.keys.every((k) => k.tier === 'premium'));
    });
  });

  describe('GET /api/api-keys/tiers', () => {
    it('returns tier information', async () => {
      const res = await get('/api/api-keys/tiers');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok(body.data.tiers.anonymous);
      assert.ok(body.data.tiers.standard);
      assert.ok(body.data.tiers.premium);
      assert.ok(body.data.tiers.anonymous.maxRequests < body.data.tiers.standard.maxRequests);
      assert.ok(body.data.tiers.standard.maxRequests < body.data.tiers.premium.maxRequests);
    });
  });

  describe('DELETE /api/api-keys/:id', () => {
    it('revokes an API key', async () => {
      const createRes = await post('/api/api-keys', { name: 'test-revoke-key' });
      const { id } = createRes.json().data;

      const revokeRes = await del(`/api/api-keys/${id}`);
      assert.strictEqual(revokeRes.status, 200);
      assert.strictEqual(revokeRes.json().data.message, 'API key revoked');

      // Verify key no longer appears in active list
      const listRes = await get('/api/api-keys');
      const keys = listRes.json().data.keys;
      assert.ok(!keys.some((k) => k.id === id));
    });

    it('returns 404 for nonexistent key', async () => {
      const res = await del('/api/api-keys/999999');
      assert.strictEqual(res.status, 404);
    });
  });

  describe('Tiered rate limit headers', () => {
    it('anonymous requests get default limit header', async () => {
      resetRateLimiter();
      const res = await get('/api/weather?lat=40.71&lon=-74.01');
      assert.strictEqual(res.status, 200);
      assert.ok(res.headers['x-ratelimit-limit']);
      assert.ok(!res.headers['x-ratelimit-tier']);
    });

    it('standard key gets higher limit in headers', async () => {
      resetRateLimiter();
      const createRes = await post('/api/api-keys', { name: 'test-rate-std' });
      const key = createRes.json().data.key;

      const res = await get('/api/weather?lat=40.71&lon=-74.01', { 'X-API-Key': key });
      assert.strictEqual(res.status, 200);
      assert.strictEqual(res.headers['x-ratelimit-tier'], 'standard');
      const limit = parseInt(res.headers['x-ratelimit-limit'], 10);
      assert.ok(limit > 60, `Standard limit ${limit} should be > 60 (anonymous)`);
    });

    it('premium key gets highest limit in headers', async () => {
      resetRateLimiter();
      const createRes = await post('/api/api-keys', { name: 'test-rate-prm', tier: 'premium' });
      const key = createRes.json().data.key;

      const res = await get('/api/weather?lat=40.71&lon=-74.01', { 'X-API-Key': key });
      assert.strictEqual(res.status, 200);
      assert.strictEqual(res.headers['x-ratelimit-tier'], 'premium');
      const limit = parseInt(res.headers['x-ratelimit-limit'], 10);
      assert.ok(limit > 120, `Premium limit ${limit} should be > 120 (standard)`);
    });

    it('invalid API key falls back to anonymous limit', async () => {
      resetRateLimiter();
      const res = await get('/api/weather?lat=40.71&lon=-74.01', { 'X-API-Key': 'invalid-key-123' });
      assert.strictEqual(res.status, 200);
      assert.ok(!res.headers['x-ratelimit-tier']);
    });

    it('revoked API key falls back to anonymous', async () => {
      resetRateLimiter();
      const createRes = await post('/api/api-keys', { name: 'test-revoked-rate' });
      const { id, key } = createRes.json().data;

      await del(`/api/api-keys/${id}`);

      const res = await get('/api/weather?lat=40.71&lon=-74.01', { 'X-API-Key': key });
      assert.strictEqual(res.status, 200);
      assert.ok(!res.headers['x-ratelimit-tier']);
    });
  });

  describe('v1 and v2 API key routes', () => {
    it('v1 tiers endpoint includes API-Version header', async () => {
      const res = await get('/api/v1/api-keys/tiers');
      assert.strictEqual(res.status, 200);
      assert.strictEqual(res.headers['api-version'], 'v1');
    });

    it('v2 tiers endpoint wraps in envelope', async () => {
      const res = await get('/api/v2/api-keys/tiers');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok(body.meta);
    });
  });
});
