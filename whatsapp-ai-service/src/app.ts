import express, { Express, Request } from 'express';
import { healthRouter } from './routes/healthRoutes';
import { webhookRouter } from './routes/webhookRoutes';

export function createApp(): Express {
  const app = express();
  app.use(
    express.json({
      limit: '1mb',
      verify: (req: Request, _res, buf: Buffer) => {
        // kept for the X-Hub-Signature-256 HMAC check
        req.rawBody = buf;
      },
    })
  );
  app.use(healthRouter);
  app.use(webhookRouter);
  return app;
}
