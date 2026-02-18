import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { getCached, setCached, buildCacheKey, getStats, flushAll } from '../../src/services/cacheService.js';

describe('cacheService', () => {
  beforeEach(() => {
    flushAll();
  });

  describe('buildCacheKey', () => {
    it('builds key with sorted params', () => {
      const key = buildCacheKey('weather', { lon: 13.41, lat: 52.52, timezone: 'auto' });
      assert.equal(key, 'weather:lat=52.52&lon=13.41&timezone=auto');
    });

    it('filters out null/undefined params', () => {
      const key = buildCacheKey('aq', { lat: 52.52, lon: 13.41, extra: null });
      assert.equal(key, 'aq:lat=52.52&lon=13.41');
    });

    it('produces consistent keys regardless of param order', () => {
      const key1 = buildCacheKey('geo', { q: 'berlin', count: 10, lang: 'en' });
      const key2 = buildCacheKey('geo', { lang: 'en', q: 'berlin', count: 10 });
      assert.equal(key1, key2);
    });

    it('handles empty params', () => {
      const key = buildCacheKey('radar', {});
      assert.equal(key, 'radar:');
    });
  });

  describe('getCached / setCached', () => {
    it('returns null for missing key', () => {
      assert.equal(getCached('nonexistent'), null);
    });

    it('stores and retrieves data', () => {
      setCached('test-key', { temp: 25 });
      const result = getCached('test-key');
      assert.ok(result);
      assert.deepEqual(result.data, { temp: 25 });
      assert.ok(result.cachedAt);
    });

    it('cachedAt is a valid ISO date string', () => {
      setCached('date-key', { x: 1 });
      const result = getCached('date-key');
      const parsed = new Date(result.cachedAt);
      assert.ok(!Number.isNaN(parsed.getTime()));
    });
  });

  describe('flushAll', () => {
    it('clears all cached entries', () => {
      setCached('a', 1);
      setCached('b', 2);
      flushAll();
      assert.equal(getCached('a'), null);
      assert.equal(getCached('b'), null);
    });
  });

  describe('getStats', () => {
    it('returns stats object with expected keys', () => {
      const stats = getStats();
      assert.ok('keys' in stats);
      assert.ok('hits' in stats);
      assert.ok('misses' in stats);
    });

    it('tracks hits and misses', () => {
      flushAll();
      getCached('miss-key');
      setCached('hit-key', 'value');
      getCached('hit-key');
      const stats = getStats();
      assert.ok(stats.misses >= 1);
      assert.ok(stats.hits >= 1);
    });
  });
});
