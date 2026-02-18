import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { getLimitForTier } from '../../src/services/apiKeyService.js';

describe('getLimitForTier', () => {
  it('returns anonymous limit for unknown tier', () => {
    const limit = getLimitForTier('anonymous');
    assert.ok(limit > 0);
    assert.strictEqual(typeof limit, 'number');
  });

  it('returns higher limit for standard tier', () => {
    const anonymous = getLimitForTier('anonymous');
    const standard = getLimitForTier('standard');
    assert.ok(standard > anonymous, `standard (${standard}) should be > anonymous (${anonymous})`);
  });

  it('returns highest limit for premium tier', () => {
    const standard = getLimitForTier('standard');
    const premium = getLimitForTier('premium');
    assert.ok(premium > standard, `premium (${premium}) should be > standard (${standard})`);
  });

  it('returns anonymous limit for undefined tier', () => {
    const limit = getLimitForTier(undefined);
    const anonymous = getLimitForTier('anonymous');
    assert.strictEqual(limit, anonymous);
  });
});
