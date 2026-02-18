import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { v1Headers, v2Envelope } from '../../src/middleware/apiVersion.js';

function createMockReqRes() {
  const req = { id: 'test-req-123' };
  const headers = {};
  let jsonBody = null;
  const res = {
    set(key, val) {
      headers[key] = val;
    },
    json(body) {
      jsonBody = body;
      return res;
    },
  };
  return { req, res, getHeaders: () => headers, getJson: () => jsonBody };
}

describe('v1Headers middleware', () => {
  it('sets API-Version header to v1', () => {
    const { req, res, getHeaders } = createMockReqRes();
    let called = false;
    v1Headers(req, res, () => {
      called = true;
    });
    assert.equal(called, true);
    assert.equal(req.apiVersion, 'v1');
    assert.equal(getHeaders()['API-Version'], 'v1');
  });
});

describe('v2Envelope middleware', () => {
  let req, res, getHeaders, getJson;

  beforeEach(() => {
    ({ req, res, getHeaders, getJson } = createMockReqRes());
    let nextCalled = false;
    v2Envelope(req, res, () => {
      nextCalled = true;
    });
    assert.equal(nextCalled, true);
  });

  it('sets API-Version header to v2', () => {
    assert.equal(req.apiVersion, 'v2');
    assert.equal(getHeaders()['API-Version'], 'v2');
  });

  it('wraps success response with meta', () => {
    res.json({ success: true, data: { temp: 20 }, cached: false, cachedAt: null });
    const body = getJson();
    assert.equal(body.success, true);
    assert.deepStrictEqual(body.data, { temp: 20 });
    assert.ok(body.meta);
    assert.equal(body.meta.requestId, 'test-req-123');
    assert.equal(body.meta.version, 'v2');
    assert.equal(body.meta.cached, false);
    assert.equal(body.meta.cachedAt, null);
    assert.ok(body.meta.timestamp);
  });

  it('moves extra fields from success response into meta', () => {
    res.json({
      success: true,
      data: {},
      cached: true,
      cachedAt: '2025-01-01',
      totalLocations: 3,
      succeeded: 2,
      failed: 1,
    });
    const body = getJson();
    assert.equal(body.meta.cached, true);
    assert.equal(body.meta.cachedAt, '2025-01-01');
    assert.equal(body.meta.totalLocations, 3);
    assert.equal(body.meta.succeeded, 2);
    assert.equal(body.meta.failed, 1);
    assert.equal(body.cached, undefined);
    assert.equal(body.cachedAt, undefined);
  });

  it('wraps error response with meta', () => {
    res.json({ success: false, error: { code: 'NOT_FOUND', message: 'nope' } });
    const body = getJson();
    assert.equal(body.success, false);
    assert.deepStrictEqual(body.error, { code: 'NOT_FOUND', message: 'nope' });
    assert.ok(body.meta);
    assert.equal(body.meta.requestId, 'test-req-123');
    assert.equal(body.meta.version, 'v2');
    assert.ok(body.meta.timestamp);
  });

  it('passes through non-standard responses unchanged', () => {
    const healthBody = { status: 'ok', version: '1.0.0', uptime: 42 };
    res.json(healthBody);
    const body = getJson();
    assert.deepStrictEqual(body, healthBody);
    assert.equal(body.meta, undefined);
  });

  it('does not double-wrap responses that already have meta', () => {
    const alreadyWrapped = { success: true, data: {}, meta: { requestId: 'x', timestamp: 'y', version: 'v2' } };
    res.json(alreadyWrapped);
    const body = getJson();
    assert.deepStrictEqual(body, alreadyWrapped);
  });

  it('passes through null body', () => {
    res.json(null);
    assert.equal(getJson(), null);
  });
});
