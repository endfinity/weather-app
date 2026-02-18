import { describe, it, before, after, mock } from 'node:test';
import assert from 'node:assert/strict';
import http from 'node:http';
import { createApp } from '../../src/app.js';
import { getDb, migrateDb, closeDb } from '../../src/db/database.js';
import { flushAll } from '../../src/services/cacheService.js';
import { createMockFetch } from '../helpers/mockData.js';
import config from '../../src/config.js';

let server;
let baseUrl;
const ORIGINAL_TIMEOUT = config.server.requestTimeoutMs;

function get(path) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, baseUrl);
    const req = http.get(url, (res) => {
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
      });
      res.on('end', () => {
        resolve({
          status: res.statusCode,
          json() {
            return JSON.parse(data);
          },
        });
      });
    });
    req.on('error', reject);
  });
}

describe('Request timeout middleware', () => {
  before(async () => {
    config.server.requestTimeoutMs = 100;
    mock.method(globalThis, 'fetch', createMockFetch());
    const app = createApp();
    getDb();
    await migrateDb();
    flushAll();

    await new Promise((resolve) => {
      server = app.listen(0, '127.0.0.1', () => {
        const { port } = server.address();
        baseUrl = `http://127.0.0.1:${port}`;
        resolve();
      });
    });
  });

  after(async () => {
    config.server.requestTimeoutMs = ORIGINAL_TIMEOUT;
    mock.restoreAll();
    flushAll();
    if (server) {
      server.closeAllConnections();
      await new Promise((resolve) => server.close(resolve));
    }
    closeDb();
  });

  it('returns 408 when a request exceeds the timeout', async () => {
    const originalFetch = globalThis.fetch.mock.implementation;
    mock.method(globalThis, 'fetch', async (url, opts) => {
      if (url.toString().includes('/v1/forecast')) {
        await new Promise((resolve) => setTimeout(resolve, 300));
      }
      return originalFetch(url, opts);
    });

    const res = await get('/api/weather?lat=52.52&lon=13.41');
    assert.equal(res.status, 408);
    const body = res.json();
    assert.equal(body.success, false);
    assert.equal(body.error.code, 'REQUEST_TIMEOUT');
  });

  it('allows fast requests to complete normally', async () => {
    mock.restoreAll();
    mock.method(globalThis, 'fetch', createMockFetch());
    flushAll();

    const res = await get('/api/health');
    assert.equal(res.status, 200);
  });
});
