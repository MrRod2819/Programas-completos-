import { NextFunction, Request, Response } from 'express';
import { config } from '../config/env';
import { isValidSignature } from '../utils/signature';
import { logger } from '../utils/logger';

export function verifyMetaSignature(req: Request, res: Response, next: NextFunction): void {
  if (config.metaAppSecret === '') {
    if (!config.mockMode) {
      logger.warn('META_APP_SECRET is not set, webhook payloads are not authenticated');
    }
    next();
    return;
  }

  const rawBody = req.rawBody ?? Buffer.alloc(0);
  const header = req.get('x-hub-signature-256') ?? undefined;

  if (!isValidSignature(rawBody, header, config.metaAppSecret)) {
    logger.warn('rejected webhook with invalid signature');
    res.sendStatus(401);
    return;
  }

  next();
}
