import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { startTestServer, stopTestServer, get, post, put } from '../helpers/testApp.js';
import { getDb } from '../../src/db/database.js';

describe('Alert Scheduler Integration', () => {
  before(async () => {
    await startTestServer();
  });

  after(async () => {
    await stopTestServer();
  });

  describe('GET /api/alerts/scheduler/status', () => {
    it('returns scheduler status', async () => {
      const res = await get('/api/alerts/scheduler/status');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok('running' in body.data);
      assert.ok('runCount' in body.data);
      assert.ok('totalAlertsSent' in body.data);
      assert.ok('config' in body.data);
      assert.ok(body.data.config.checkIntervalMs > 0);
      assert.ok(body.data.config.cooldownMs > 0);
    });
  });

  describe('POST /api/alerts/scheduler/trigger', () => {
    it('triggers a check and returns results', async () => {
      const res = await post('/api/alerts/scheduler/trigger', {});
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok('locationsChecked' in body.data);
      assert.ok('alertsSent' in body.data);
      assert.strictEqual(typeof body.data.locationsChecked, 'number');
      assert.strictEqual(typeof body.data.alertsSent, 'number');
    });
  });

  describe('GET /api/alerts/history', () => {
    before(() => {
      const db = getDb();
      db.prepare(
        `INSERT INTO alert_history (alert_type, severity, title, description, location_name, latitude, longitude, status, devices_notified)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      ).run('extreme_heat', 'extreme', 'Extreme Heat', 'Temperature 42C', 'TestCity', 40.71, -74.01, 'sent', 2);
      db.prepare(
        `INSERT INTO alert_history (alert_type, severity, title, description, location_name, latitude, longitude, status, devices_notified)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      ).run('fog', 'moderate', 'Fog Advisory', 'Reduced visibility', 'TestCity', 40.71, -74.01, 'cooldown', 0);
      db.prepare(
        `INSERT INTO alert_history (alert_type, severity, title, description, location_name, latitude, longitude, status, devices_notified)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      ).run('high_wind', 'severe', 'High Wind', 'Wind 85km/h', 'TestCity', 40.71, -74.01, 'sent', 1);
    });

    it('returns paginated alert history', async () => {
      const res = await get('/api/alerts/history');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok(Array.isArray(body.data.alerts));
      assert.ok(body.data.total >= 3);
      assert.ok(body.data.limit > 0);
      assert.strictEqual(body.data.offset, 0);
    });

    it('supports limit and offset', async () => {
      const res = await get('/api/alerts/history?limit=1&offset=1');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.data.limit, 1);
      assert.strictEqual(body.data.offset, 1);
      assert.ok(body.data.alerts.length <= 1);
    });

    it('filters by severity', async () => {
      const res = await get('/api/alerts/history?severity=extreme');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.ok(body.data.alerts.every((a) => a.severity === 'extreme'));
    });

    it('filters by status', async () => {
      const res = await get('/api/alerts/history?status=cooldown');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.ok(body.data.alerts.every((a) => a.status === 'cooldown'));
    });
  });

  describe('POST /api/alerts/history/cleanup', () => {
    it('runs cleanup and returns deleted count', async () => {
      const res = await post('/api/alerts/history/cleanup', {});
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.strictEqual(typeof body.data.deleted, 'number');
    });
  });

  describe('Device Alert Preferences', () => {
    let token;

    before(() => {
      token = `test-prefs-token-${Date.now()}`;
      const db = getDb();
      db.prepare('INSERT INTO devices (fcm_token, platform) VALUES (?, ?)').run(token, 'android');
    });

    after(() => {
      const db = getDb();
      db.prepare('DELETE FROM devices WHERE fcm_token = ?').run(token);
    });

    it('GET returns null preferences for new device', async () => {
      const res = await get(`/api/devices/${token}/preferences`);
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.strictEqual(body.data.preferences, null);
    });

    it('PUT sets alert preferences', async () => {
      const res = await put(`/api/devices/${token}/preferences`, {
        minSeverity: 'severe',
        quietHoursStart: 22,
        quietHoursEnd: 7,
        enabledTypes: ['extreme_heat', 'thunderstorm', 'high_wind'],
      });
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.strictEqual(body.data.message, 'Alert preferences updated');

      const getRes = await get(`/api/devices/${token}/preferences`);
      assert.strictEqual(getRes.status, 200);
      const getBody = getRes.json();
      assert.strictEqual(getBody.data.preferences.minSeverity, 'severe');
      assert.strictEqual(getBody.data.preferences.quietHoursStart, 22);
      assert.strictEqual(getBody.data.preferences.quietHoursEnd, 7);
      assert.deepStrictEqual(getBody.data.preferences.enabledTypes, ['extreme_heat', 'thunderstorm', 'high_wind']);
    });

    it('GET returns 404 for unknown device', async () => {
      const res = await get('/api/devices/nonexistent-token/preferences');
      assert.strictEqual(res.status, 404);
    });
  });

  describe('v1 and v2 scheduler endpoints', () => {
    it('v1 scheduler status includes API-Version header', async () => {
      const res = await get('/api/v1/alerts/scheduler/status');
      assert.strictEqual(res.status, 200);
      assert.strictEqual(res.headers['api-version'], 'v1');
    });

    it('v2 scheduler status is wrapped in envelope', async () => {
      const res = await get('/api/v2/alerts/scheduler/status');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok(body.meta);
      assert.ok(body.data.running !== undefined || body.data.runCount !== undefined);
    });

    it('v2 alert history is wrapped in envelope', async () => {
      const res = await get('/api/v2/alerts/history');
      assert.strictEqual(res.status, 200);
      const body = res.json();
      assert.strictEqual(body.success, true);
      assert.ok(body.meta);
      assert.ok(body.data);
    });
  });
});
