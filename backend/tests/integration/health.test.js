import { describe, it, before, after, mock } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get } from '../helpers/testApp.js';
import { createMockFetch } from '../helpers/mockData.js';

describe('GET /api/health', () => {
  before(async () => {
    mock.method(globalThis, 'fetch', createMockFetch());
    await startTestServer();
  });

  after(async () => {
    mock.restoreAll();
    await stopTestServer();
  });

  it('returns health status', async () => {
    const res = await get('/api/health');
    const body = res.json();
    assert.equal(res.status, 200);
    assert.ok(['ok', 'degraded'].includes(body.status));
    assert.ok(typeof body.uptime === 'number');
    assert.ok(body.uptime >= 0);
  });

  it('includes version and timestamp', async () => {
    const res = await get('/api/health');
    const body = res.json();
    assert.ok(typeof body.version === 'string');
    assert.ok(body.version.length > 0);
    assert.ok(typeof body.timestamp === 'string');
  });

  it('includes database check', async () => {
    const res = await get('/api/health');
    const body = res.json();
    assert.ok('checks' in body);
    assert.ok('database' in body.checks);
    assert.equal(body.checks.database.status, 'ok');
    assert.ok(typeof body.checks.database.responseMs === 'number');
  });

  it('includes upstream check', async () => {
    const res = await get('/api/health');
    const body = res.json();
    assert.ok('upstream' in body.checks);
    assert.ok(typeof body.checks.upstream.responseMs === 'number');
  });

  it('includes memory check', async () => {
    const res = await get('/api/health');
    const body = res.json();
    assert.ok('memory' in body.checks);
    assert.ok(typeof body.checks.memory.rss === 'number');
    assert.ok(typeof body.checks.memory.heapUsed === 'number');
    assert.ok(typeof body.checks.memory.heapTotal === 'number');
  });

  it('includes cache stats', async () => {
    const res = await get('/api/health');
    const body = res.json();
    assert.ok('cache' in body);
    assert.ok('keys' in body.cache);
    assert.ok('hits' in body.cache);
    assert.ok('misses' in body.cache);
  });
});
