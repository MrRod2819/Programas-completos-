import { createApp } from './app';
import { config } from './config/env';
import { runMigrations } from './db/migrate';
import { logger } from './utils/logger';

async function start(): Promise<void> {
  await runMigrations();
  const app = createApp();
  app.listen(config.port, () => {
    logger.info('server listening', { port: config.port, mockMode: config.mockMode });
  });
}

start().catch((error: unknown) => {
  logger.error('server failed to start', { error: String(error) });
  process.exit(1);
});
