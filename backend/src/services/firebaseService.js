import { initializeApp, cert, getApps } from 'firebase-admin/app';
import { getMessaging } from 'firebase-admin/messaging';
import { readFileSync, existsSync } from 'fs';
import { resolve } from 'path';
import config from '../config.js';
import logger from '../logger.js';

const log = logger.child({ component: 'firebase' });

let messaging = null;
let initialized = false;

/**
 * Initialize Firebase Admin SDK from a service account file.
 * Safe to call multiple times; only initializes once.
 */
export function initFirebase() {
  if (initialized) return;

  const saPath = resolve(config.firebase.serviceAccountPath);

  if (!existsSync(saPath)) {
    log.warn({ path: saPath }, 'Service account file not found — push notifications disabled');
    initialized = true;
    return;
  }

  try {
    const serviceAccount = JSON.parse(readFileSync(saPath, 'utf-8'));

    if (getApps().length === 0) {
      initializeApp({ credential: cert(serviceAccount) });
    }

    messaging = getMessaging();
    initialized = true;
    log.info('Admin SDK initialized successfully');
  } catch (err) {
    log.error({ err }, 'Failed to initialize');
    initialized = true;
  }
}

/**
 * Get the Firebase Messaging instance (null if not initialized).
 * @returns {import('firebase-admin/messaging').Messaging | null}
 */
export function getFirebaseMessaging() {
  return messaging;
}

/**
 * Check whether Firebase Messaging is ready to send notifications.
 * @returns {boolean}
 */
export function isFirebaseReady() {
  return messaging !== null;
}
