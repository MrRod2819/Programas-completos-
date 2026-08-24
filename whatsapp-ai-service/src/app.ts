import express, { Express } from 'express';
import { healthRouter } from './routes/healthRoutes';
import { webhookRouter } from './routes/webhookRoutes';

export function createApp(): Express {
  const app = express();
  app.use(express.json({ limit: '1mb' }));
  app.use(healthRouter);
  app.use(webhookRouter);
  return app;
}
