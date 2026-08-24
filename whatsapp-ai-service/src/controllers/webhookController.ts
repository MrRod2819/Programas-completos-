import { Request, Response } from 'express';
import { config } from '../config/env';
import { handleIncomingMessage } from '../services/conversationService';
import { extractTextMessage } from '../services/whatsappParser';
import { WhatsAppWebhookPayload } from '../types';
import { logger } from '../utils/logger';

export function verifyWebhook(req: Request, res: Response): void {
  const mode = req.query['hub.mode'];
  const token = req.query['hub.verify_token'];
  const challenge = req.query['hub.challenge'];

  if (mode === 'subscribe' && token === config.webhookVerifyToken && typeof challenge === 'string') {
    logger.info('webhook verified');
    res.status(200).send(challenge);
    return;
  }

  logger.warn('webhook verification failed', { mode: String(mode) });
  res.sendStatus(403);
}

export function receiveWebhook(req: Request, res: Response): void {
  // Meta retries the delivery if it does not get a 200 quickly, so ack first
  // and process the message afterwards.
  res.sendStatus(200);

  const payload = req.body as WhatsAppWebhookPayload;
  const incoming = extractTextMessage(payload);

  if (!incoming) {
    logger.info('webhook event ignored (status update or unsupported message type)');
    return;
  }

  void handleIncomingMessage(incoming).catch((error: unknown) => {
    logger.error('failed to process incoming message', {
      error: String(error),
      from: incoming.senderPhone,
    });
  });
}
