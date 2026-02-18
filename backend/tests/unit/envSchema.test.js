import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { envSchema, parseEnv } from '../../src/envSchema.js';

describe('envSchema', () => {
  it('applies all defaults when given an empty object', () => {
    const result = envSchema.safeParse({});
    assert.ok(result.success);
    assert.equal(result.data.PORT, 3000);
    assert.equal(result.data.NODE_ENV, 'development');
    assert.equal(result.data.TRUST_PROXY, false);
    assert.equal(result.data.BODY_LIMIT, '1mb');
    assert.equal(result.data.SHUTDOWN_TIMEOUT_MS, 30_000);
    assert.equal(result.data.REQUEST_TIMEOUT_MS, 30_000);
    assert.equal(result.data.CACHE_TTL_SECONDS, 900);
    assert.equal(result.data.RATE_LIMIT_WINDOW_MS, 60_000);
    assert.equal(result.data.RATE_LIMIT_MAX_REQUESTS, 60);
    assert.equal(result.data.WEATHER_MONITOR_INTERVAL_MS, 900_000);
    assert.equal(result.data.LOG_LEVEL, 'info');
  });

  it('coerces numeric strings', () => {
    const result = envSchema.safeParse({ PORT: '8080', CACHE_TTL_SECONDS: '300' });
    assert.ok(result.success);
    assert.equal(result.data.PORT, 8080);
    assert.equal(result.data.CACHE_TTL_SECONDS, 300);
  });

  it('coerces TRUST_PROXY boolean strings', () => {
    const result = envSchema.safeParse({ TRUST_PROXY: 'true' });
    assert.ok(result.success);
    assert.equal(result.data.TRUST_PROXY, true);
  });

  it('rejects invalid NODE_ENV', () => {
    const result = envSchema.safeParse({ NODE_ENV: 'staging' });
    assert.ok(!result.success);
  });

  it('rejects non-HTTP URL for OPEN_METEO_BASE_URL', () => {
    const result = envSchema.safeParse({ OPEN_METEO_BASE_URL: 'ftp://bad.com' });
    assert.ok(!result.success);
  });

  it('rejects negative PORT', () => {
    const result = envSchema.safeParse({ PORT: '-1' });
    assert.ok(!result.success);
  });

  it('allows REQUEST_TIMEOUT_MS of 0 (disabled)', () => {
    const result = envSchema.safeParse({ REQUEST_TIMEOUT_MS: '0' });
    assert.ok(result.success);
    assert.equal(result.data.REQUEST_TIMEOUT_MS, 0);
  });

  it('rejects non-numeric PORT string', () => {
    const result = envSchema.safeParse({ PORT: 'abc' });
    assert.ok(!result.success);
  });
});

describe('parseEnv', () => {
  it('returns parsed config for valid env', () => {
    const env = parseEnv({});
    assert.equal(env.PORT, 3000);
    assert.equal(env.NODE_ENV, 'development');
  });

  it('throws descriptive error for invalid env', () => {
    assert.throws(() => parseEnv({ PORT: 'not-a-number' }), {
      message: /Invalid environment configuration/,
    });
  });
});
