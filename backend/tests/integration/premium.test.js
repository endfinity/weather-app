import { describe, it, before, after, beforeEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get, post } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';
import { getDb } from '../../src/db/database.js';

describe('POST /api/premium/verify', () => {
  before(async () => {
    mock.method(globalThis, 'fetch', createMockFetch());
    await startTestServer();
  });

  after(async () => {
    mock.restoreAll();
    await stopTestServer();
  });

  beforeEach(() => {
    const db = getDb();
    db.prepare("DELETE FROM premium_users WHERE device_id LIKE 'test-prem-%'").run();
  });

  it('verifies a new purchase', async () => {
    const res = await post('/api/premium/verify', {
      deviceId: 'test-prem-device-1',
      purchaseToken: 'test-prem-new-token',
      productId: 'clearsky_premium',
    });
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.success, true);
    assert.equal(body.data.premium, true);
    assert.equal(body.data.productId, 'clearsky_premium');
  });

  it('updates existing purchase token', async () => {
    await post('/api/premium/verify', {
      deviceId: 'test-prem-device-2',
      purchaseToken: 'test-prem-exist-token',
      productId: 'clearsky_premium',
    });

    const res = await post('/api/premium/verify', {
      deviceId: 'test-prem-device-3',
      purchaseToken: 'test-prem-exist-token',
      productId: 'clearsky_premium',
    });
    assert.equal(res.status, 200);
    assert.equal(res.json().data.premium, true);
  });

  it('returns 400 for missing deviceId', async () => {
    const res = await post('/api/premium/verify', {
      purchaseToken: 'token',
      productId: 'clearsky_premium',
    });
    assert.equal(res.status, 400);
    assert.equal(res.json().error.code, 'MISSING_PARAMETER');
  });

  it('returns 400 for missing purchaseToken', async () => {
    const res = await post('/api/premium/verify', {
      deviceId: 'test-prem-device',
      productId: 'clearsky_premium',
    });
    assert.equal(res.status, 400);
  });

  it('returns 400 for missing productId', async () => {
    const res = await post('/api/premium/verify', {
      deviceId: 'test-prem-device',
      purchaseToken: 'token',
    });
    assert.equal(res.status, 400);
  });
});

describe('GET /api/premium/status', () => {
  before(async () => {
    mock.method(globalThis, 'fetch', createMockFetch());
    await startTestServer();
    const db = getDb();
    db.prepare("DELETE FROM premium_users WHERE device_id LIKE 'test-status-%'").run();
    db.prepare(
      'INSERT INTO premium_users (device_id, purchase_token, product_id, purchase_time, active) VALUES (?, ?, ?, ?, 1)',
    ).run('test-status-active', 'token-active', 'clearsky_premium', Date.now());
  });

  after(async () => {
    const db = getDb();
    db.prepare("DELETE FROM premium_users WHERE device_id LIKE 'test-status-%'").run();
    mock.restoreAll();
    await stopTestServer();
  });

  it('returns premium true for active user', async () => {
    const res = await get('/api/premium/status?device_id=test-status-active');
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.data.premium, true);
    assert.equal(body.data.productId, 'clearsky_premium');
  });

  it('returns premium false for unknown device', async () => {
    const res = await get('/api/premium/status?device_id=unknown-device');
    const body = res.json();
    assert.equal(res.status, 200);
    assert.equal(body.data.premium, false);
    assert.equal(body.data.productId, null);
  });

  it('returns 400 for missing device_id', async () => {
    const res = await get('/api/premium/status');
    assert.equal(res.status, 400);
    assert.equal(res.json().error.code, 'MISSING_PARAMETER');
  });
});
