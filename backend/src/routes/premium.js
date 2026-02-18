import { Router } from 'express';
import { getDb } from '../db/database.js';
import { validatePremiumVerify, validateDeviceId } from '../validation.js';

const router = Router();

/**
 * @openapi
 * /api/premium/verify:
 *   post:
 *     tags: [Premium]
 *     summary: Verify a premium purchase
 *     description: Verifies and stores a Google Play purchase token to activate premium status.
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [deviceId, purchaseToken, productId]
 *             properties:
 *               deviceId:
 *                 type: string
 *                 description: Unique device identifier
 *               purchaseToken:
 *                 type: string
 *                 description: Google Play purchase token
 *               productId:
 *                 type: string
 *                 description: Product SKU
 *     responses:
 *       200:
 *         description: Purchase verified and premium activated
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 data:
 *                   type: object
 *                   properties:
 *                     premium:
 *                       type: boolean
 *                     productId:
 *                       type: string
 *       400:
 *         description: Missing required fields
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
// POST /api/premium/verify — verify and store a Google Play purchase token
router.post('/verify', async (req, res) => {
  const { deviceId, purchaseToken, productId } = validatePremiumVerify(req.body);

  // In production, verify the purchase token with Google Play Developer API:
  // https://developers.google.com/android-publisher/api-ref/rest/v3/purchases.products/get
  // For now, we trust the client-supplied token (suitable for development/testing).

  const db = getDb();

  const existing = db.prepare('SELECT id FROM premium_users WHERE purchase_token = ?').get(purchaseToken);

  if (existing) {
    db.prepare('UPDATE premium_users SET active = 1, device_id = ? WHERE purchase_token = ?').run(
      deviceId,
      purchaseToken,
    );
  } else {
    db.prepare(
      'INSERT INTO premium_users (device_id, purchase_token, product_id, purchase_time) VALUES (?, ?, ?, ?)',
    ).run(deviceId, purchaseToken, productId, Date.now());
  }

  res.json({
    success: true,
    data: { premium: true, productId },
  });
});

/**
 * @openapi
 * /api/premium/status:
 *   get:
 *     tags: [Premium]
 *     summary: Check premium subscription status
 *     description: Returns whether the given device has an active premium subscription.
 *     parameters:
 *       - name: device_id
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *         description: Device identifier to check
 *     responses:
 *       200:
 *         description: Premium status
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 data:
 *                   type: object
 *                   properties:
 *                     premium:
 *                       type: boolean
 *                     productId:
 *                       type: string
 *                       nullable: true
 *                     purchaseTime:
 *                       type: integer
 *                       nullable: true
 *       400:
 *         description: Missing device_id
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
// GET /api/premium/status — check if a device has premium
router.get('/status', (req, res) => {
  const { device_id } = req.query;
  validateDeviceId(device_id);

  const db = getDb();
  const user = db
    .prepare('SELECT product_id, purchase_time FROM premium_users WHERE device_id = ? AND active = 1')
    .get(device_id);

  res.json({
    success: true,
    data: {
      premium: Boolean(user),
      productId: user?.product_id ?? null,
      purchaseTime: user?.purchase_time ?? null,
    },
  });
});

export default router;
