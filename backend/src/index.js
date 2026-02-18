import config from './config.js';
import { createApp } from './app.js';
import { initFirebase } from './services/firebaseService.js';
import { getDb, migrateDb, closeDb } from './db/database.js';
import { startWeatherMonitor, stopWeatherMonitor } from './jobs/weatherMonitor.js';
import logger from './logger.js';

const app = createApp();

getDb();
await migrateDb();
initFirebase();

const server = app.listen(config.port, '0.0.0.0', (error) => {
  if (error) {
    logger.fatal({ err: error }, 'Failed to start server');
    process.exit(1);
  }
  logger.info({ port: config.port, env: config.nodeEnv }, 'ClearSky backend running');
  startWeatherMonitor();
});

let isShuttingDown = false;

function gracefulShutdown(signal) {
  if (isShuttingDown) return;
  isShuttingDown = true;

  logger.info({ signal }, 'Shutting down gracefully');
  stopWeatherMonitor();

  const forceTimeout = setTimeout(() => {
    logger.fatal('Graceful shutdown timed out, forcing exit');
    process.exit(1);
  }, config.server.shutdownTimeoutMs);
  forceTimeout.unref();

  server.close(() => {
    logger.info('HTTP server closed');
    closeDb();
    logger.info('Database closed — goodbye');
    process.exit(0);
  });
}

process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
process.on('SIGINT', () => gracefulShutdown('SIGINT'));

process.on('unhandledRejection', (reason) => {
  logger.error({ err: reason }, 'Unhandled promise rejection');
});

process.on('uncaughtException', (err) => {
  logger.fatal({ err }, 'Uncaught exception — forcing shutdown');
  gracefulShutdown('uncaughtException');
});
