import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import conditionalRequest from '../../src/middleware/conditionalRequest.js';

function createMockReqRes(method = 'GET', ifNoneMatch = undefined) {
  const headers = {};
  let statusCode = 200;
  let ended = false;
  let jsonBody = null;
  let headersSent = false;

  const req = {
    method,
    headers: ifNoneMatch ? { 'if-none-match': ifNoneMatch } : {},
  };

  const res = {
    get headersSent() {
      return headersSent;
    },
    set(key, val) {
      headers[key] = val;
    },
    status(code) {
      statusCode = code;
      return res;
    },
    end() {
      ended = true;
      headersSent = true;
      return res;
    },
    json(body) {
      jsonBody = body;
      headersSent = true;
      return res;
    },
  };

  return {
    req,
    res,
    getHeaders: () => headers,
    getStatus: () => statusCode,
    getJsonBody: () => jsonBody,
    isEnded: () => ended,
  };
}

function computeWeakEtag(body) {
  const hash = crypto.createHash('md5').update(JSON.stringify(body)).digest('hex');
  return `W/"${hash}"`;
}

describe('conditionalRequest middleware', () => {
  it('sets ETag header on GET responses', () => {
    const { req, res, getHeaders, getJsonBody } = createMockReqRes('GET');
    conditionalRequest(req, res, () => {});

    const body = { success: true, data: { temp: 20 } };
    res.json(body);

    const etag = getHeaders()['ETag'];
    assert.ok(etag, 'ETag header should be set');
    assert.ok(etag.startsWith('W/"'), 'Should be a weak ETag');
    assert.deepStrictEqual(getJsonBody(), body);
  });

  it('returns 304 when If-None-Match matches the ETag', () => {
    const body = { success: true, data: { temp: 20 } };
    const expectedEtag = computeWeakEtag(body);

    const { req, res, getStatus, isEnded, getJsonBody } = createMockReqRes('GET', expectedEtag);
    conditionalRequest(req, res, () => {});
    res.json(body);

    assert.equal(getStatus(), 304);
    assert.equal(isEnded(), true);
    assert.equal(getJsonBody(), null, 'Should not send JSON body on 304');
  });

  it('returns 200 when If-None-Match does not match', () => {
    const body = { success: true, data: { temp: 20 } };

    const { req, res, getStatus, getJsonBody, getHeaders } = createMockReqRes('GET', 'W/"stale-hash"');
    conditionalRequest(req, res, () => {});
    res.json(body);

    assert.equal(getStatus(), 200);
    assert.deepStrictEqual(getJsonBody(), body);
    assert.ok(getHeaders()['ETag']);
  });

  it('does not apply to POST requests', () => {
    const { req, res, getHeaders } = createMockReqRes('POST');
    let nextCalled = false;
    conditionalRequest(req, res, () => {
      nextCalled = true;
    });

    assert.equal(nextCalled, true);

    res.json({ success: true });
    assert.equal(getHeaders()['ETag'], undefined, 'Should not set ETag on POST');
  });

  it('does not apply to PUT requests', () => {
    const { req, res, getHeaders } = createMockReqRes('PUT');
    conditionalRequest(req, res, () => {});
    res.json({ success: true });
    assert.equal(getHeaders()['ETag'], undefined);
  });

  it('same body produces same ETag', () => {
    const body = { success: true, data: { value: 42 } };

    const m1 = createMockReqRes('GET');
    conditionalRequest(m1.req, m1.res, () => {});
    m1.res.json(body);

    const m2 = createMockReqRes('GET');
    conditionalRequest(m2.req, m2.res, () => {});
    m2.res.json(body);

    assert.equal(m1.getHeaders()['ETag'], m2.getHeaders()['ETag']);
  });

  it('different bodies produce different ETags', () => {
    const m1 = createMockReqRes('GET');
    conditionalRequest(m1.req, m1.res, () => {});
    m1.res.json({ data: 'a' });

    const m2 = createMockReqRes('GET');
    conditionalRequest(m2.req, m2.res, () => {});
    m2.res.json({ data: 'b' });

    assert.notEqual(m1.getHeaders()['ETag'], m2.getHeaders()['ETag']);
  });

  it('uses weak ETag format W/"hash"', () => {
    const { req, res, getHeaders } = createMockReqRes('GET');
    conditionalRequest(req, res, () => {});
    res.json({ test: true });

    const etag = getHeaders()['ETag'];
    assert.match(etag, /^W\/"[a-f0-9]{32}"$/);
  });
});
