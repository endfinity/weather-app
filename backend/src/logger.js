import pino from 'pino';

const isTest = process.env.NODE_ENV === 'test';
const isDev = process.env.NODE_ENV === 'development';
const level = process.env.LOG_LEVEL || (isTest ? 'silent' : 'info');

const options = {
  level,
  timestamp: pino.stdTimeFunctions.isoTime,
  formatters: {
    level(label) {
      return { level: label };
    },
  },
};

if (isDev) {
  options.transport = {
    target: 'pino-pretty',
    options: { colorize: true },
  };
}

const logger = pino(options);

export default logger;
