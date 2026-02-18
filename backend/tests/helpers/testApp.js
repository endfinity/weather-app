import http from 'node:http';
import { createApp } from '../../src/app.js';
import { getDb, migrateDb } from '../../src/db/database.js';
import { flushAll } from '../../src/services/cacheService.js';

let server;
let baseUrl;

export async function startTestServer() {
  const app = createApp();
  getDb();
  await migrateDb();
  flushAll();

  return new Promise((resolve) => {
    server = app.listen(0, '127.0.0.1', () => {
      const { port } = server.address();
      baseUrl = `http://127.0.0.1:${port}`;
      resolve(baseUrl);
    });
  });
}

export async function stopTestServer() {
  flushAll();
  cleanTestData();
  if (server) {
    server.closeAllConnections();
    return new Promise((resolve) => server.close(resolve));
  }
}

export function getBaseUrl() {
  return baseUrl;
}

export function get(path, headers = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, baseUrl);
    const req = http.get(url, { headers }, (res) => {
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
      });
      res.on('end', () => {
        resolve({
          status: res.statusCode,
          headers: res.headers,
          body: data,
          json() {
            return JSON.parse(data);
          },
        });
      });
    });
    req.on('error', reject);
  });
}

export function post(path, body, headers = {}) {
  return httpRequest('POST', path, body, headers);
}

export function put(path, body, headers = {}) {
  return httpRequest('PUT', path, body, headers);
}

export function del(path, headers = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, baseUrl);
    const req = http.request(url, { method: 'DELETE', headers }, (res) => {
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
      });
      res.on('end', () => {
        resolve({
          status: res.statusCode,
          headers: res.headers,
          body: data,
          json() {
            return JSON.parse(data);
          },
        });
      });
    });
    req.on('error', reject);
    req.end();
  });
}

function httpRequest(method, path, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, baseUrl);
    const payload = JSON.stringify(body);
    const req = http.request(
      url,
      {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(payload),
          ...headers,
        },
      },
      (res) => {
        let data = '';
        res.on('data', (chunk) => {
          data += chunk;
        });
        res.on('end', () => {
          resolve({
            status: res.statusCode,
            headers: res.headers,
            body: data,
            json() {
              return JSON.parse(data);
            },
          });
        });
      },
    );
    req.on('error', reject);
    req.write(payload);
    req.end();
  });
}

export function seedPremiumUser(
  deviceId = 'test-device',
  purchaseToken = 'test-token',
  productId = 'clearsky_premium',
) {
  const db = getDb();
  db.prepare('DELETE FROM premium_users WHERE device_id = ?').run(deviceId);
  db.prepare(
    'INSERT INTO premium_users (device_id, purchase_token, product_id, purchase_time, active) VALUES (?, ?, ?, ?, 1)',
  ).run(deviceId, purchaseToken, productId, Date.now());
  return purchaseToken;
}

function cleanTestData() {
  try {
    const db = getDb();
    db.prepare("DELETE FROM premium_users WHERE device_id LIKE 'test-%'").run();
    db.prepare("DELETE FROM api_keys WHERE name LIKE 'test-%'").run();
  } catch {
    // DB may already be closed
  }
}
