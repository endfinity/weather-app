import { Router } from 'express';
import { createApiKey, listApiKeys, revokeApiKey, getLimitForTier } from '../services/apiKeyService.js';
import { AppError } from '../errors.js';

const router = Router();

/**
 * @openapi
 * /api/api-keys:
 *   post:
 *     tags: [API Keys]
 *     summary: Create a new API key
 *     description: Generates a new API key with the specified tier for rate limiting.
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name]
 *             properties:
 *               name:
 *                 type: string
 *                 description: Descriptive name for the API key
 *                 example: My Weather App
 *               tier:
 *                 type: string
 *                 enum: [standard, premium]
 *                 default: standard
 *     responses:
 *       201:
 *         description: API key created
 *       400:
 *         description: Invalid request
 */
router.post('/', (req, res) => {
  const { name, tier = 'standard' } = req.body;

  if (!name || typeof name !== 'string' || name.trim().length === 0) {
    throw new AppError('MISSING_PARAMETER', 'name is required');
  }

  if (!['standard', 'premium'].includes(tier)) {
    throw new AppError('MISSING_PARAMETER', 'tier must be "standard" or "premium"');
  }

  const apiKey = createApiKey(name.trim(), tier);

  res.status(201).json({
    success: true,
    data: {
      ...apiKey,
      rateLimit: {
        tier,
        maxRequests: getLimitForTier(tier),
      },
    },
  });
});

/**
 * @openapi
 * /api/api-keys:
 *   get:
 *     tags: [API Keys]
 *     summary: List all API keys
 *     description: Returns all active API keys, optionally filtered by tier.
 *     parameters:
 *       - name: tier
 *         in: query
 *         schema:
 *           type: string
 *           enum: [standard, premium]
 *       - name: include_inactive
 *         in: query
 *         schema:
 *           type: boolean
 *           default: false
 *     responses:
 *       200:
 *         description: List of API keys
 */
router.get('/', (req, res) => {
  const tier = req.query.tier;
  const includeInactive = req.query.include_inactive === 'true';

  const keys = listApiKeys({ tier, includeInactive });

  res.json({
    success: true,
    data: {
      keys,
      tiers: {
        anonymous: getLimitForTier('anonymous'),
        standard: getLimitForTier('standard'),
        premium: getLimitForTier('premium'),
      },
    },
  });
});

/**
 * @openapi
 * /api/api-keys/{id}:
 *   delete:
 *     tags: [API Keys]
 *     summary: Revoke an API key
 *     description: Deactivates an API key so it can no longer be used for authentication.
 *     parameters:
 *       - name: id
 *         in: path
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Key revoked
 *       404:
 *         description: Key not found
 */
router.delete('/:id', (req, res) => {
  const id = parseInt(req.params.id, 10);

  if (Number.isNaN(id)) {
    throw new AppError('MISSING_PARAMETER', 'Invalid key ID');
  }

  const revoked = revokeApiKey(id);

  if (!revoked) {
    throw new AppError('NOT_FOUND', 'API key not found or already revoked');
  }

  res.json({
    success: true,
    data: { message: 'API key revoked' },
  });
});

/**
 * @openapi
 * /api/api-keys/tiers:
 *   get:
 *     tags: [API Keys]
 *     summary: Get rate limit tiers
 *     description: Returns the rate limit configuration for each tier.
 *     responses:
 *       200:
 *         description: Tier information
 */
router.get('/tiers', (_req, res) => {
  res.json({
    success: true,
    data: {
      tiers: {
        anonymous: { maxRequests: getLimitForTier('anonymous'), description: 'No API key (IP-based)' },
        standard: { maxRequests: getLimitForTier('standard'), description: 'Standard API key' },
        premium: { maxRequests: getLimitForTier('premium'), description: 'Premium API key' },
      },
    },
  });
});

export default router;
